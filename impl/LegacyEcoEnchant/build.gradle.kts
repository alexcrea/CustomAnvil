import java.net.URI

group = "${rootProject.group}.impl"
version = "1.9.1" // Note: need to be edit only if this module has changed.

plugins {
    kotlin("jvm") version "2.1.0"
}

// Imitate needed class and method to support legacy version of EcoEnchant
dependencies {
    // Spigot api
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

}

object Meta {
    const val desc = "module to try to handle compatibility with some old version of EcoEnchants."
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