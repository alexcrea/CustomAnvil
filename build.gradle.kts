import cn.lalaki.pub.BaseCentralPortalPlusExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0"
    java
    id("org.jetbrains.dokka").version("1.9.20")
    id("io.github.goooler.shadow").version("8.1.8") // using fork of com.github.johnrengelman.shadow to support java 1.21. edit I do not need java 1.21 now so should be replaced ?
    // Maven publish
    `maven-publish`
    signing
    id("cn.lalaki.central").version("1.2.5")
}

group = "xyz.alexcrea"
version = "1.5.4-fix"

repositories {
    // EcoEnchants
    maven(url = "https://repo.auxilor.io/repository/maven-public/")

}

dependencies {
    // Gui library
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.14")

    // EnchantsSquaredRewritten
    compileOnly(files("libs/EnchantsSquared.jar"))

    // EcoEnchants
    compileOnly("com.willfp:EcoEnchants:12.5.1")
    compileOnly("com.willfp:eco:6.70.1")

    // Include nms
    implementation(project(":nms:nms-common"))

}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()

        // Spigot repository
        maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        // We assume nms part will not require version specific api.
        // If any issue occur because of this assumption. please fell free to edit.
        compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

        // Currently not used. but it would be useful to test.
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    // Configure used version of kotlin and java
    java {
        disableAutoTargetJvm()
        toolchain.languageVersion.set(JavaLanguageVersion.of(20))
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    // Set target version
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "16" // We aim for java 16 for minecraft 1.16.5. even if it not really suported.
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
    archiveFileName.set("${rootProject.name}-${archiveVersion}.jar")
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    duplicatesStrategy = DuplicatesStrategy.WARN
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

// Ensure fatJar and copyJar are run
tasks.getByName("build") {
    dependsOn(fatJar)
}

// Shadow necessary dependency
tasks.shadowJar {
    relocate("com.github.stefvanschie.inventoryframework", "xyz.alexcrea.inventoryframework")
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

val localMavenRepo = uri("E:\\WorkSpace\\Java\\Maven\\repo") // The path is recommended to be set to an empty directory
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
