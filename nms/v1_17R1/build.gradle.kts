import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = rootProject.group
version = rootProject.version

plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":nms:nms-common"))

    // Used for nms
    paperweight.paperDevBundle("1.17.1-R0.1-SNAPSHOT")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

}

// As minecraft 1.17 recommended java version is 1.16. we set language version to 1.16

// Configure used version of kotlin and java
java {
    disableAutoTargetJvm()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

// Set target version
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "16"
    targetCompatibility = "16"

    options.encoding = "UTF-8"
}

kotlin {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(JvmTarget.JVM_16)
    }
}
