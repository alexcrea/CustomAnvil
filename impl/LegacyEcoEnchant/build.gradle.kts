group = rootProject.group
version = rootProject.version

plugins {
    kotlin("jvm") version "2.0.21"
}

// Imitate needed class and method to support legacy version of EcoEnchant
dependencies {
    // Spigot api
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")



}