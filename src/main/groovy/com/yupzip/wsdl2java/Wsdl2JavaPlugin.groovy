package com.yupzip.wsdl2java

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

class Wsdl2JavaPlugin implements Plugin<Project> {
    public static final String WSDL2JAVA = "wsdl2java"
    public static final String WSDL2JAVA_TASK = "wsdl2javaTask"

    private static final JAVA_9_DEPENDENCIES = [
            "javax.xml.bind:jaxb-api:2.3.1",
            "javax.xml.ws:jaxws-api:2.3.1",
            "org.glassfish.jaxb:jaxb-runtime:2.3.2",
            "org.glassfish.main.javaee-api:javax.jws:3.1.2.2",
            "com.sun.xml.messaging.saaj:saaj-impl:1.5.1"
    ]

    void apply(Project project) {
        project.apply(plugin: "java")

        def extension = project.extensions.create(WSDL2JAVA, Wsdl2JavaPluginExtension.class)
        def cxfVersion = project.provider { extension.cxfVersion }
        def cxfPluginVersion = project.provider { extension.cxfPluginVersion }

        // Add new configuration for our plugin and add required dependencies to it later.
        def wsdl2javaConfiguration = project.configurations.maybeCreate(WSDL2JAVA)

        // Get implementation configuration and add Java 9+ dependencies if required.
        project.configurations.named("implementation").configure {
            it.withDependencies {
                if (JavaVersion.current().isJava9Compatible() && extension.includeJava8XmlDependencies) {
                    JAVA_9_DEPENDENCIES.each { dep -> it.add(project.dependencies.create(dep)) }
                }
            }
        }

        wsdl2javaConfiguration.withDependencies {
            it.add(project.dependencies.create("org.apache.cxf:cxf-tools-wsdlto-databinding-jaxb:${cxfVersion.get()}"))
            it.add(project.dependencies.create("org.apache.cxf:cxf-tools-wsdlto-frontend-jaxws:${cxfVersion.get()}"))
            if (project.wsdl2java.wsdlsToGenerate.any { it.contains('-xjc-Xts') }) {
                it.add(project.dependencies.create("org.apache.cxf.xjcplugins:cxf-xjc-ts:${cxfPluginVersion.get()}"))
            }
            if (project.wsdl2java.wsdlsToGenerate.any { it.contains('-xjc-Xbg') }) {
                it.add(project.dependencies.create("org.apache.cxf.xjcplugins:cxf-xjc-boolean:${cxfPluginVersion.get()}"))
            }

            if (JavaVersion.current().isJava9Compatible() && extension.includeJava8XmlDependencies) {
                JAVA_9_DEPENDENCIES.each { dep -> it.add(project.dependencies.create(dep)) }
            }
        }

        def wsdl2JavaTask = project.tasks.register(WSDL2JAVA_TASK, Wsdl2JavaTask.class) { task ->
            task.group = "Wsdl2Java"
            task.description = "Generate java source code from WSDL files."
            task.classpath = wsdl2javaConfiguration
            task.extension = extension
        }

        project.tasks.named("compileJava").configure {
            it.dependsOn wsdl2JavaTask
        }

        if (project.tasks.findByName("compileKotlin") != null) {
            project.tasks.named("compileKotlin").configure {
                it.dependsOn wsdl2JavaTask
            }
        }

        project.sourceSets {
            main.java.srcDirs += extension.generatedWsdlDir
        }
    }
}
