package com.crgt.service

import org.gradle.api.Plugin
import org.gradle.api.Project

class LauncherPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        String versionName = ""
        project.buildscript.configurations.classpath.resolvedConfiguration.firstLevelModuleDependencies.forEach {
            if (it.name.startsWith("com.crgt.android:service-plugin")) {
                versionName = it.moduleVersion
                println "service plugin version = " + versionName
                return
            }
        }
        project.subprojects { Project p ->
            p.afterEvaluate {
                if (it.plugins.hasPlugin("com.android.application")) {
                    it.plugins.apply('com.crgt.serviceimpl')
                }
                if (it.plugins.hasPlugin("com.android.application") || it.plugins.hasPlugin("com.android.library")) {
                    it.dependencies.add('implementation', "com.crgt.android:base-service-api:1.0.0-SNAPSHOT")
                    it.dependencies.add('annotationProcessor', "com.crgt.android:base-service-compiler:1.0.0-SNAPSHOT")
                    it.android.defaultConfig.javaCompileOptions.annotationProcessorOptions.argument('moduleName', it.name)
                }
            }
        }
    }
}