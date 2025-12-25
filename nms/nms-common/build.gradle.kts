import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = rootProject.group
version = rootProject.version

plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    // Used for nms
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")

    // Protocolib
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
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
