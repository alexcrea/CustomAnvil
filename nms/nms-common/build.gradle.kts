group = rootProject.group
version = rootProject.version

repositories {
    // ProtocoLib
    maven (url = "https://repo.dmulloy2.net/repository/public/" )

}

dependencies {
    // Protocolib
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

}