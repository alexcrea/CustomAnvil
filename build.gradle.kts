import cn.lalaki.pub.BaseCentralPortalPlusExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.21"
    java
    id("org.jetbrains.dokka").version("1.9.20")
    id("com.gradleup.shadow").version("8.3.3")
    // Maven publish
    `maven-publish`
    signing
    id("cn.lalaki.central").version("1.2.5")
    id("io.papermc.paperweight.userdev") version "1.7.3" apply false
}

group = "xyz.alexcrea"
version = "1.6.6"

repositories {
    // EcoEnchants
    maven(url = "https://repo.auxilor.io/repository/maven-public/")

}

dependencies {
    // Spigot api
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

    // Gui library
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.17")

    // EnchantsSquaredRewritten
    compileOnly(files("libs/EnchantsSquared.jar"))

    // EcoEnchants
    compileOnly("com.willfp:EcoEnchants:12.5.1")
    compileOnly("com.willfp:eco:6.70.1")

    // ExcellentEnchants
    compileOnly(files("libs/nightcore-2.6.4.jar"))
    compileOnly(files("libs/ExcellentEnchants-4.2.2.jar"))

    // Disenchantment
    compileOnly("cz.kominekjan:Disenchantment:v5.4.0")

    // Include nms
    implementation(project(":nms:nms-common"))
    implementation(project(":nms:v1_17R1", configuration = "reobf"))
    implementation(project(":nms:v1_18R1", configuration = "reobf"))
    implementation(project(":nms:v1_18R2", configuration = "reobf"))
    implementation(project(":nms:v1_19R1", configuration = "reobf"))
    implementation(project(":nms:v1_19R2", configuration = "reobf"))
    implementation(project(":nms:v1_19R3", configuration = "reobf"))
    implementation(project(":nms:v1_20R1", configuration = "reobf"))
    implementation(project(":nms:v1_20R2", configuration = "reobf"))
    implementation(project(":nms:v1_20R3", configuration = "reobf"))
    implementation(project(":nms:v1_20R4", configuration = "reobf"))
    implementation(project(":nms:v1_21R1", configuration = "reobf"))

    // include kotlin for the offline jar
    implementation(kotlin("stdlib"))
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

        // Currently not used. but it would be useful to test.
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    // Configure used version of kotlin and java
    java {
        disableAutoTargetJvm()
        toolchain.languageVersion.set(JavaLanguageVersion.of(20))
    }

    // Set target version
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "16" // We aim for java 16 for minecraft 1.16.5. even if it not really suported by custom anvil.
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

// Fat-jar builder
val fatJar = tasks.register<Jar>("fatJar") {
    manifest {
        attributes.apply { put("Main-Class", "io.delilaheve.CustomAnvil") }
    }
    archiveFileName.set("${rootProject.name}-${project.version}.jar")
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    duplicatesStrategy = DuplicatesStrategy.WARN
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    // Online jar (use of libraries)
    shadowJar {
        // No suffix for this jar
        archiveClassifier.set("")

        // Exclude kotlin std and its annotation
        exclude("**/kotlin-stdlib*.jar")
        exclude("**/annotations*.jar")

        // Shadow necessary dependency
        relocate("com.github.stefvanschie.inventoryframework", "xyz.alexcrea.inventoryframework")

        // Replace version and example fields in plugin.yml
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version,
                "libraries" to " \"org.jetbrains.kotlin:kotlin-stdlib:2.0.21\" "
            )
        }

        // Process resource for plugin.yml
        dependsOn(processResources)
    }

    // Offline jar (include kotlin std in the final jar fine)
    val offlineJar by creating(ShadowJar::class) {
        archiveClassifier.set("offline")

        // Shadow necessary dependency
        relocate("com.github.stefvanschie.inventoryframework", "xyz.alexcrea.inventoryframework")

        filesMatching("plugin.yml") {
            expand(
                "version" to "${project.version}-offline",
                "libraries" to ""
            )
        }

        // Include all project other dependencies
        from(project.configurations.runtimeClasspath)

        // Add custom anvil compiled
        from(sourceSets.main.get().output)

        dependsOn(processResources)
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

val javadocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc JAR"
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaHtml"))
}

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
            }
        }
    }
}
