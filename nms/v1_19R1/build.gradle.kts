import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = rootProject.group
version = rootProject.version

plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":nms:nms-common"))

    // Used for nms
    paperweight.paperDevBundle("1.19.2-R0.1-SNAPSHOT")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

}

// I do not know minecraft 1.19 recommended java version. assumed 17 is good enough

// Configure used version of kotlin and java
java {
    disableAutoTargetJvm()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

// Set target version
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"

    options.encoding = "UTF-8"
}

kotlin {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
