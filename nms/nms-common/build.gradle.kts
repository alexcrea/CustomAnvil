group = rootProject.group
version = rootProject.version

repositories {
    // ProtocoLib
    maven (url = "https://repo.dmulloy2.net/repository/public/" )

}

dependencies {
    // Spigot api
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

    // Protocolib
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
}