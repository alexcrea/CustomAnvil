import cn.lalaki.pub.BaseCentralPortalPlusExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.util.Node
import groovy.util.NodeList
import io.papermc.hangarpublishplugin.model.HangarPublication
import io.papermc.hangarpublishplugin.model.Platforms
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.1.0"
    java
    id("org.jetbrains.dokka").version("1.9.20")
    id("com.gradleup.shadow").version("9.3.0")
    // Maven publish
    `maven-publish`
    signing
    id("cn.lalaki.central").version("1.2.8")
    // Paper
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" apply false
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
}

group = "xyz.alexcrea"
version = "1.15.9"

val isDevBuild = System.getenv("SMALL_COMMIT_HASH") != null
val isPreRelease = System.getenv("IS_GITHUB_PRERELEASE") == "true"

val effectiveVersion = "$version" +
        (if (isDevBuild) "-dev-${System.getenv("SMALL_COMMIT_HASH")!!}" else "")

repositories {
    // EcoEnchants
    maven(url = "https://repo.auxilor.io/repository/maven-public/")

    // ExcellentEnchants
    maven(url = "https://repo.nightexpressdev.com/releases")
}

val reobfNMS = providers.gradleProperty("subprojects.reobfnms")
    .get().split(",")

dependencies {
    // Spigot api
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

    // minimessage
    implementation("net.kyori:adventure-text-minimessage:4.25.0")

    // Gui library
    val inventoryFramework = "xyz.alexcrea.cuanvil.inventoryframework:IF-CustomAnvil:0.10.18.2"
    implementation(inventoryFramework)
    testRuntimeOnly(inventoryFramework)

    // EnchantsSquaredRewritten
    compileOnly(files("libs/EnchantsSquared.jar"))

    // EcoEnchants
    compileOnly("com.willfp:EcoEnchants:12.11.1")
    compileOnly("com.willfp:eco:6.74.5")
    compileOnly(project(":impl:LegacyEcoEnchant"))

    // ExcellentEnchants
    implementation(project(":impl:ExcellentEnchant5_3"))
    compileOnly("su.nightexpress.excellentenchants:Core:5.1.0") {
        exclude("org.spigotmc")
    }
    compileOnly(files("libs/ExcellentEnchants-4.3.3-striped.jar")) // For pre v5 excellent enchants
    compileOnly(files("libs/ExcellentEnchants-4.1.0-striped.jar")) // For legacy excellent enchants

    // Disenchantment
    compileOnly(files("libs/Disenchantment-6.1.5.jar"))

    // HavenBags
    compileOnly(files("libs/HavenBags-1.31.0.1760.jar"))

    // ToolStats
    compileOnly(files("libs/toolstats-1.9.6-stripped.jar"))

    // AxPlayerWarps
    compileOnly(files("libs/AxPlayerWarps-1.10.3.jar"))

    // Include nms
    implementation(project(":nms:nms-common"))
    implementation(project(":nms:nms-paper"))
    for (nmsPart in reobfNMS) {
        implementation(project(":nms:$nmsPart", configuration = "reobf"))
    }

    // include kotlin for the offline jar
    implementation(kotlin("stdlib"))

    // Test dependency
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.48.0")
    testRuntimeOnly("commons-lang:commons-lang:2.6")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()

        // Spigot repository
        maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

        // Paper repository
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        // Test dependency
        testImplementation(platform("org.junit:junit-bom:5.12.2"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    // Configure used version of kotlin and java
    java {
        disableAutoTargetJvm()
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    // Set target version
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility =
            "16" // We aim for java 16 for minecraft 1.16.5. even if it not really suported by custom anvil.
        targetCompatibility = "16"

        options.encoding = "UTF-8"
    }

    kotlin {
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
            jvmTarget.set(JvmTarget.JVM_16)
        }
    }

}


tasks {

    fun ShadowJar.configureBaseShadow(suffix: String, libraries: Array<String>) {
        val processedSuffix = if(suffix.isEmpty()) "" else "-$suffix"
        val name = "${rootProject.name}-${effectiveVersion}${processedSuffix}.jar"
        archiveFileName.set(name)

        // Shadow necessary dependency
        relocate("com.github.stefvanschie.inventoryframework", "xyz.alexcrea.inventoryframework")

        filesMatching("plugin.yml") {
            expand(
                "version" to effectiveVersion + processedSuffix,
                "libraries" to libraries.joinToString(transform = { "\"$it\"" }),
            )
        }

        // Process resource for plugin.yml
        dependsOn(processResources)
    }

    // Online jar (use of libraries)
    shadowJar {
        configureBaseShadow("",
            arrayOf(
                "org.jetbrains.kotlin:kotlin-stdlib:2.1.0",
                "net.kyori:adventure-text-minimessage:4.25.0",
                "net.kyori:adventure-text-serializer-plain:4.25.0",
                "net.kyori:adventure-text-serializer-legacy:4.25.0",
            ))

        // Exclude kotlin std, annotations and adventure api
        exclude("*kotlin/**")
        exclude("**/annotations/**")
        exclude("net/kyori/**")
    }

    val offlineJar by registering(ShadowJar::class) {
        configureBaseShadow("offline", emptyArray())

        from(sourceSets.main.get().output)
        configurations = listOf(project.configurations.runtimeClasspath.get())
    }

    // Make the online and offline jar on build
    named("build") {
        dependsOn(shadowJar, offlineJar)
    }

}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

val javadocJar by tasks.registering(Jar::class, fun Jar.() {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc JAR"
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaHtml"))
})

signing {
    useGpgCmd()
    val extension = extensions
        .getByName("publishing") as PublishingExtension
    sign(extension.publications)
}

// ------------------------------------
// PUBLISHING TO SONATYPE CONFIGURATION
// ------------------------------------

// The path is recommended to be set to an empty directory
val localMavenRepo = uri(
    project.findProperty("localMavenRepo") as String?
        ?: rootProject.layout.buildDirectory.dir("local-maven-repo").get().asFile.toURI() // Convert to URI
)

centralPortalPlus {
    url = localMavenRepo
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")
    publishingType = BaseCentralPortalPlusExtension.PublishingType.USER_MANAGED // or PublishingType.AUTOMATIC
}

object Meta {
    const val desc = "spigot plugin to control every aspect of the anvil."
    const val license = "GPL-3.0"
    const val githubRepo = "alexcrea/CustomAnvil"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}

val disallowedDependency = HashSet<String>()
disallowedDependency.addAll(reobfNMS)
disallowedDependency.addAll(listOf("nms-common", "nms-paper", "kotlin-stdlib"))

publishing {
    repositories {
        maven {
            url = localMavenRepo // Specify the same local repo path in the configuration.
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

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name.set(project.name)
                description.set(Meta.desc)
                url.set("https://github.com/${Meta.githubRepo}")
                licenses {
                    license {
                        name.set(Meta.license)
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                developers {
                    developer {
                        id.set("alexcrea")
                        name.set("alexcrea")
                        email.set("alexcrea.of@laposte.net")
                        url.set("https://github.com/alexcrea")
                    }
                }
                scm {
                    url.set(
                        "https://github.com/${Meta.githubRepo}.git"
                    )
                    connection.set(
                        "scm:git:git://github.com/${Meta.githubRepo}.git"
                    )
                    developerConnection.set(
                        "scm:git:git://github.com/${Meta.githubRepo}.git"
                    )
                }
                issueManagement {
                    url.set("https://github.com/${Meta.githubRepo}/issues")
                }

                withXml {
                    val dependenciesNode = (asNode().get("dependencies") as NodeList)[0] as Node

                    val toRemove = ArrayList<Node>()
                    for (child in dependenciesNode.children()) {
                        val artifactNode = ((child as Node).get("artifactId") as NodeList)[0] as Node
                        val artifactID = artifactNode.value() as String

                        if(disallowedDependency.contains(artifactID)) {
                            toRemove.add(child)
                        }
                    }

                    for (node in toRemove) {
                        dependenciesNode.remove(node)
                    }
                }
            }
        }
    }
}

// hangar publish

fun executeGitCommand(vararg command: String): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", *command)
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}


fun latestCommitMessage(): String {
    return executeGitCommand("log", "-1", "--pretty=%B")
}

fun changelog(isOnline: Boolean): String {
    var changelog = if(isDevBuild) latestCommitMessage()
    else System.getenv("RELEASE_CHANGELOG")

    if(!isOnline) {
        changelog = "This is an offline version of the plugin. \\\n" +
                "This mean that this plugin libraries are shaded into this plugin \\\n" +
                "You likely want to use the normal version of this plugin\n\n" + changelog
    }

    if(changelog == null || changelog.isEmpty()) {
        changelog = "empty changelog"
    }

    return changelog
}

hangarPublish {

    fun HangarPublication.configure(isOnline: Boolean, devChannel: String, releaseChannel: String) {
        var versionName = effectiveVersion
        if(isPreRelease) versionName+= "-pre"
        if(!isOnline) versionName+= "-offline"

        version.set(versionName)
        channel.set(if (isDevBuild || isPreRelease) devChannel else releaseChannel)

        changelog.set(changelog(isOnline))
        id.set("CustomAnvil")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        platforms {
            register(Platforms.PAPER) {
                // Set the JAR file to upload
                var task = if(isOnline) tasks.shadowJar
                else tasks.named<ShadowJar>("offlineJar")

                jar.set(task.flatMap { it.archiveFile })

                // Set platform versions from gradle.properties file
                val versions: List<String> = (property("paperVersion") as String)
                    .split(",")
                    .map { it.trim() }
                platformVersions.set(versions)

                dependencies {
                    hangar("ProtocolLib") {
                        required.set(false)
                    }
                    url("Disenchantment", "https://modrinth.com/plugin/disenchantment") {
                        required.set(false)
                    }
                    url("ToolStats", "https://modrinth.com/plugin/toolstats") {
                        required.set(false)
                    }
                    url("HavenBags", "https://www.spigotmc.org/resources/havenbags-shulker-like-player-bound-bags-1-17-1-21-4.110420/") {
                        required.set(false)
                    }
                    url("EcoEnchants", "https://www.spigotmc.org/resources/ecoenchants-%E2%AD%95-250-enchantments-%E2%9C%85-create-custom-enchants-%E2%9C%A8-essentials-cmi-support.79573/") {
                        required.set(false)
                    }
                    hangar("EnchantsSquared") {
                        required.set(false)
                    }
                    url("ExcellentEnchants", "https://www.spigotmc.org/resources/excellentenchants-%E2%AD%90-75-vanilla-like-enchantments.61693/") {
                        required.set(false)
                    }
                }
            }
        }
    }

    publications.register("plugin") {
        configure(true, "DevSnapshot", "Release")
    }

    publications.register("offline") {
        configure(false, "OfflineSnapshot", "OfflineRelease")
    }

}
