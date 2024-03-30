plugins {
    kotlin("jvm") version "1.6.21"
    java
}

group = "xyz.alexcrea"
version = "1.3.2-A2"

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {

    compileOnly(kotlin("stdlib"))

    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

    // Gui library
    compileOnly("com.github.stefvanschie.inventoryframework:IF:0.10.13")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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