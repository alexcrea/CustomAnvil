import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = rootProject.group
version = rootProject.version

plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":nms:nms-common"))

    // Used for nms
    paperweight.paperDevBundle("1.21.5-no-moonrise-SNAPSHOT")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

    mavenLocal()//TODO remove when paperDevBundle 1.21.5 release
}

// minecraft 1.21 java version is 21.

// Configure used version of kotlin and java
java {
    disableAutoTargetJvm()
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

// Set target version
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "21"
    targetCompatibility = "21"

    options.encoding = "UTF-8"
}

kotlin {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
