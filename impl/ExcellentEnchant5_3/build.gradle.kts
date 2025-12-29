group = rootProject.group
version = rootProject.version

plugins {
    kotlin("jvm") version "2.1.0"
}

repositories {
    // ExcellentEnchants
    maven(url = "https://repo.nightexpressdev.com/releases")
}

dependencies {
    // Spigot api
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

    // Excellent Enchant
    compileOnly("su.nightexpress.excellentenchants:Core:5.3.0") {
        exclude("org.spigotmc")
    }
}