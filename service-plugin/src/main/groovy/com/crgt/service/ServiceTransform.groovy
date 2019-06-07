package com.crgt.service

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.CtClass
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class ServiceTransform extends Transform implements Plugin<Project> {

    Project project
    static File sCollectorFile
    static List<String> sServiceCollectorNames = new ArrayList<>()

    ServiceTransform() {
        sCollectorFile = null
        sServiceCollectorNames.clear()
    }

    @Override
    void apply(Project project) {
        this.project = project
        def android = project.extensions.getByType(AppExtension)
        //register this plugin
        android.registerTransform(this)
    }

    @Override
    String getName() {
        return "Service"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        println "====== start of service transform ======"
        long startTime = System.currentTimeMillis()
        boolean leftSlash = File.separator == '/'

        inputs.each { TransformInput input ->

            // scan all jars
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.name
                // rename jar files
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }
                // input file
                File src = jarInput.file
                // output file
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                scanJar(src, dest)
                FileUtils.copyFile(src, dest)
            }
            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator
                directoryInput.file.eachFileRecurse { File file ->
                    def path = file.absolutePath.replace(root, '')
                    if (!leftSlash) {
                        path = path.replaceAll("\\\\", "/")
                    }
                    if (file.isFile()) {
                        if (path.startsWith(TransformConsts.SERVICE_MODULE_COLLECTOR_PREFIX)) {
                            scanClass(file)
                        }
                    }
                }
                // copy to dest
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }

        if (sCollectorFile && sServiceCollectorNames.size() > 0) {
            insertCollectNames(sCollectorFile)
        }

        println 'Transform finished, current cost ' + (System.currentTimeMillis() - startTime) + "ms"
        println "====== end of service transform ======"
    }
/**
     * scan jar file
     * @param jarFile All jar files that are compiled into apk
     * @param destFile dest file after this transform
     */
    static void scanJar(File jarFile, File destFile) {
        if (jarFile) {
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                if (entryName.startsWith(TransformConsts.SERVICE_MODULE_COLLECTOR_PREFIX)) {
                    InputStream inputStream = file.getInputStream(jarEntry)
                    scanClass(inputStream)
                    inputStream.close()
                } else if (entryName == TransformConsts.SERVICE_COLLECTOR_CLASS) {
                    // mark jar file contain ServiceCollector.class
                    sCollectorFile = destFile
                }
            }
            file.close()
        }
    }

    static void scanClass(File file) {
        scanClass(new FileInputStream(file))
    }

    static void scanClass(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    private static class ScanClassVisitor extends ClassVisitor {

        ScanClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            if (interfaces != null) {
                interfaces.each { itName ->
                    if (itName == TransformConsts.MODULE_SERVICE_COLLECTOR) {
                        sServiceCollectorNames.add(name)
                    }
                }
            }
        }
    }

    static void insertCollectNames(File jarFile) {
        def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
        if (optJar.exists())
            optJar.delete()
        def file = new JarFile(jarFile)
        Enumeration enumeration = file.entries()
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            String entryName = jarEntry.getName()
            ZipEntry zipEntry = new ZipEntry(entryName)
            InputStream inputStream = file.getInputStream(jarEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (TransformConsts.SERVICE_COLLECTOR_CLASS == entryName) {
//                println 'Insert code to class >> ' + entryName
                jarOutputStream.write(rewriteClass(inputStream))
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()

        if (jarFile.exists()) {
            jarFile.delete()
        }
        optJar.renameTo(jarFile)
    }

    static byte[] rewriteClass(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ClassVisitor cv = new RewriteClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    private static class RewriteClassVisitor extends ClassVisitor {

        RewriteClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc,
                                  String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
            if (name == TransformConsts.SERVICE_COLLECTOR_METHOD) {
                mv = new CollectMethodVisitor(Opcodes.ASM5, mv)
            }
            return mv
        }
    }

    private static class CollectMethodVisitor extends MethodVisitor {

        CollectMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        void visitInsn(int opcode) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                sServiceCollectorNames.each { collectName ->
                    println "insert service collector >> " + collectName
                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitFieldInsn(Opcodes.GETFIELD, TransformConsts.SERVICE_COLLECTOR, TransformConsts.SERVICE_COLLECTOR_FIELD, "Ljava/util/List;")
                    mv.visitTypeInsn(Opcodes.NEW, collectName)
                    mv.visitInsn(Opcodes.DUP)
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, collectName, "<init>", "()V", false)
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
                    mv.visitInsn(Opcodes.POP)
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 3, maxLocals)
        }
    }
}