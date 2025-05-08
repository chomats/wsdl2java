plugins {
    id("groovy")
    id("maven-publish")
    id("idea")
    id("eclipse")
}

group = "com.yupzip"
version = "4.0.0"

// stay compatible with the crowd
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

defaultTasks("clean", "build")

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.5.2")
    testImplementation(gradleTestKit())

    runtimeOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

//gradlePlugin {
//    plugins {
//        create("wsdl2java") {
//            id = "com.yupzip.wsdl2java"
//            implementationClass = "com.yupzip.wsdl2java.Wsdl2JavaPlugin"
//            displayName = "com.yupzip.wsdl2java"
//            description = "Generate java code form wsdl/xsd with cxf"
//        }
//    }
//}

//pluginBundle {
//    website = "https://github.com/yupzip/wsdl2java"
//    vcsUrl = "https://github.com/yupzip/wsdl2java"
//    tags = listOf("wsdl2java")
//}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.14"
}