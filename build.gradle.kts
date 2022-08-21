plugins {
    kotlin("jvm") version "1.6.21"
    java
}

group = "io.delilaheve"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {

    implementation(kotlin("stdlib"))

    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

// Fat-jar builder
val fatJar = tasks.register<Jar>("fatJar") {
    manifest {
        attributes.apply { put("Main-Class", "io.delilaheve.UnsafeEnchants") }
    }
    archiveFileName.set("${rootProject.name}-${version}.jar")
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    duplicatesStrategy = DuplicatesStrategy.WARN
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

// Ensure fatJar and copyJar are run
tasks.getByName("build") {
    dependsOn(fatJar)
}