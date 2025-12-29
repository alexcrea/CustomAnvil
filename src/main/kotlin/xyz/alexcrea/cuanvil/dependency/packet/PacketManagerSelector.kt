package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.Bukkit

object PacketManagerSelector {

    //TODO

    fun selectPacketManager(forceProtocolib: Boolean): PacketManagerBase {
        // Try to find version
        return if (forceProtocolib)
            protocolibIfPresent
        else
            versionSpecificManager
    }

    private val protocolibIfPresent: PacketManagerBase
        get() =
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
                ProtocoLibWrapper()
            else
                PacketManagerBase()
    private val versionSpecificManager: PacketManagerBase
        get() {
            return PacketManager()
        }
}
