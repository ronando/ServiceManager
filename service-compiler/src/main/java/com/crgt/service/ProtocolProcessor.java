package com.crgt.service;

import com.google.auto.service.AutoService;
import com.crgt.service.annotation.ServiceImpl;
import com.crgt.service.utils.Constants;
import com.crgt.service.utils.Logger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ProtocolProcessor extends AbstractProcessor {

    private String moduleName;

    private Filer mFiler;
    private Logger logger;
    private TypeMirror iCollector;
    private TypeMirror iServiceMirror;
    private Types types;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        logger = new Logger(processingEnvironment.getMessager());
        types = processingEnvironment.getTypeUtils();
        Elements elementUtil = processingEnvironment.getElementUtils();
        iCollector = elementUtil.getTypeElement(ModuleServiceCollector.class.getName()).asType();
        iServiceMirror = elementUtil.getTypeElement(IService.class.getName()).asType();

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (!(options == null || options.isEmpty())) {
            moduleName = options.get("moduleName");
        }

        if (!(moduleName == null || moduleName.length() == 0)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
        } else {
            logger.error("no module name, at 'build.gradle', like :\n" +
                    "apt {\n" +
                    "    arguments {\n" +
                    "        moduleName project.getName();\n" +
                    "    }\n" +
                    "}\n");
            throw new RuntimeException("service-compiler >>> No module name, for more information, look at gradle log.");
        }
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>(1);
        types.add(ServiceImpl.class.getName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        log("annotation processing...");
        Set<? extends Element> services = roundEnvironment.getElementsAnnotatedWith(ServiceImpl.class);
        try {
            parseServices(services);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void parseServices(Set<? extends Element> services) throws IOException {
        if (services == null || services.isEmpty()) {
            return;
        }

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_COLLECT)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ServiceMap.class, Constants.COLLECT_METHOD_PARAMETER_MAP);

        for (Element element : services) {
//            logger.info("service impl: " + ((TypeElement)element).getQualifiedName().toString());
            List<String> serviceNames = verifyAndGetServiceName(element);
            if (serviceNames != null && !serviceNames.isEmpty()) {
                for (String serviceName : serviceNames) {
//                    logger.info("match service: " + serviceName);
                    methodBuilder.addStatement("$N.put($S, $S)", Constants.COLLECT_METHOD_PARAMETER_MAP, serviceName, ClassName.get((TypeElement) element));
                }
            }
        }
        TypeSpec protocolCallback = TypeSpec.classBuilder(Constants.SERVICE_COLLECT_PREFIX + Constants.SEPARATOR + moduleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get(iCollector))
                .addMethod(methodBuilder.build())
                .build();

        JavaFile.builder(Constants.OUTPUT_DIRECTORY, protocolCallback).build().writeTo(mFiler);
    }


    private List<String> verifyAndGetServiceName(Element element) {
        ServiceImpl serviceImpl = element.getAnnotation(ServiceImpl.class);
        if (serviceImpl == null) {
            logger.error(element.getSimpleName() + " is not annotated with ServiceImpl");
            return null;
        }
        List<? extends TypeMirror> interfaceMirrors = ((TypeElement) element).getInterfaces();
        if (interfaceMirrors == null) {
            logger.error(element.getSimpleName() + " didn't implements any interfaces");
            return null;
        }
        List<TypeElement> candaditeElementTypes = new ArrayList<>();
        for (TypeMirror mirror : interfaceMirrors) {
            if (mirror instanceof DeclaredType) {
                DeclaredType declared = (DeclaredType)mirror;
                Element interfaceElement = declared.asElement();
                if (interfaceElement instanceof TypeElement) {
                    candaditeElementTypes.add((TypeElement)interfaceElement);
                    String interfaceQualifiedName = ((TypeElement) interfaceElement).getQualifiedName().toString();
//                    logger.info("first level " + ((TypeElement) element).getQualifiedName() + " implements interface " + interfaceQualifiedName);
                }
            }
        }
        if (candaditeElementTypes.isEmpty()) {
            logger.error(element.getSimpleName() + " didn't implement any validate interfaces");
            return null;
        }
        List<String> serviceNames = new ArrayList<>();
        for (TypeElement typeElement : candaditeElementTypes) {
            serviceNames.add(typeElement.getQualifiedName().toString());
        }
//        for (TypeElement typeElement : candaditeElementTypes) {
//            List<? extends TypeMirror> secLevelInterfaceMirrors = typeElement.getInterfaces();
//            // Service接口和实现类如果不在同一个模块就不能拿到Service的信息，只能获取到Service的name，一次下面这一段代码不能用.
//            for (TypeMirror mirror : secLevelInterfaceMirrors) {
//                if (mirror.equals(iServiceMirror)) {
//                    serviceNames.add(typeElement.getQualifiedName().toString());
//                    logger.info("second level " + typeElement.getQualifiedName().toString() + " implements interface IService");
//                    break;
//                }
//            }
//        }
//        if (serviceNames.isEmpty()) {
//            logger.error("No interface of " + element.getSimpleName() + " extends IService");
//            return null;
//        }
        return serviceNames;
    }

    private static void log(String msg) {
        System.out.println("ProtocolProcessor" + ": " + msg);
    }
}
