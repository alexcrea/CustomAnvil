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
    // Excellent Enchant
    compileOnly("su.nightexpress.excellentenchants:Core:5.4.1")
    compileOnly("su.nightexpress.nightcore:main:2.14.1")
}