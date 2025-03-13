apply(plugin = "maven-publish")

// The path is recommended to be set to an empty directory
val localMavenRepo = uri(
    project.findProperty("localMavenRepo") as String?
        ?: rootProject.layout.buildDirectory.dir("local-maven-repo").get().asFile.toURI() // Convert to URI
)

val developers = listOf(
    mapOf(
        "id" to "alexcrea",
        "name" to "alexcrea",
        "email" to "alexcrea.of@laposte.net",
        "url" to "https://github.com/alexcrea")
)

project.extra["localMavenRepoURL"] = localMavenRepo
project.extra["license"] = "GPL-3.0"
project.extra["licenseLink"] = "https://www.gnu.org/licenses/gpl-3.0.en.html"
project.extra["gitRepo"] = "github.com/alexcrea/CustomAnvil"
project.extra["developers"] = developers