plugins {
    kotlin("jvm") version "1.9.24"
    java
    id("org.jetbrains.dokka").version("1.9.20")
    id("com.github.johnrengelman.shadow").version("7.1.2")
}

group = "xyz.alexcrea"
version = "1.5.2"

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

    // ProtocoLib
    maven (url = "https://repo.dmulloy2.net/repository/public/" )

    // EcoEnchants
    maven(url = "https://repo.auxilor.io/repository/maven-public/")
}

dependencies {

    compileOnly(kotlin("stdlib"))

    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

    // Gui library
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.14")

    // Protocolib
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    // EnchantsSquaredRewritten
    compileOnly(files("libs/EnchantsSquared.jar"))

    // EcoEnchants
    compileOnly("com.willfp:EcoEnchants:12.5.1")
    compileOnly("com.willfp:eco:6.70.1")

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

// Shadow recesary dependency
tasks.shadowJar {
    relocate("com.github.stefvanschie.inventoryframework", "xyz.alexcrea.inventoryframework")
}
