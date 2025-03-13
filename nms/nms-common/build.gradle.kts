import java.net.URI

group = "${rootProject.group}.nms"
version = "1.9.1" // Note: need to be edit only if this nms module has changed.

repositories {
    // ProtocoLib
    maven (url = "https://repo.dmulloy2.net/repository/public/" )

}

dependencies {
    // Spigot api
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

    // Protocolib
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
}

object Meta {
    const val desc = "Abstraction and shared code for CustomAnvil nms sub projects."
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