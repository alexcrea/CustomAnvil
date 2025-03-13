import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

group = "${rootProject.group}.nms"
version = "1.9.1" // Note: need to be edit only if this nms module has changed.

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

object Meta {
    const val desc = "nms hook for 1.17R1 spigot/craftbukkit version."
}

publishing {
    repositories {
        maven {
            url = project.extra["localMavenRepoURL"] as URI // Specify the same local repo path in the configuration.
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            val githubRepo = project.extra["gitRepo"] as String
            pom {
                name.set(project.name)
                description.set(Meta.desc)
                url.set("https://${githubRepo}")
                licenses {
                    license {
                        name.set(project.extra["license"] as String)
                        url.set( project.extra["licenseLink"] as String)
                    }
                }
                developers {
                    for (developerData in project.extra["developers"] as List<Map<String, String>>) {
                        developer {
                            id.set(developerData["id"])
                            name.set(developerData["name"])
                            email.set(developerData.getOrDefault("email",null))
                            url.set(developerData.getOrDefault("url",null))
                        }
                    }
                }
                scm {
                    url.set(
                        "https://${githubRepo}.git"
                    )
                    connection.set(
                        "scm:git:git://${githubRepo}.git"
                    )
                    developerConnection.set(
                        "scm:git:git://${githubRepo}.git"
                    )
                }
                issueManagement {
                    url.set("https://${githubRepo}/issues")
                }
            }
        }
    }
}