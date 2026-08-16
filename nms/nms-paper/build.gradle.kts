import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = rootProject.group
version = rootProject.version

plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":nms:nms-common"))

    // Used for nms
    paperweight.paperDevBundle("1.21.7-R0.1-SNAPSHOT")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

}

// Set target version
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "18"
    targetCompatibility = "18"

    options.encoding = "UTF-8"
}

kotlin {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4)
        jvmTarget.set(JvmTarget.JVM_18)
    }
}
