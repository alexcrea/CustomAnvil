package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.Bukkit
import xyz.alexcrea.cuanvil.dependency.packet.versions.V1_21R1_PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.versions.V1_21R2_PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.versions.V1_21R3_PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.versions.V1_21R4_PacketManager
import xyz.alexcrea.cuanvil.update.UpdateUtils

object PacketManagerSelector {
    fun selectPacketManager(forceProtocolib: Boolean): PacketManager {
        // Try to find version
        return if (forceProtocolib)
            protocolibIfPresent
        else
            versionSpecificManager ?: protocolibIfPresent
    }

    private val protocolibIfPresent: PacketManager
        get() =
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
                ProtocoLibWrapper()
            else
                NoPacketManager()
    private val versionSpecificManager: PacketManagerBase?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersionArray()
            if (versionParts[0] != 1) return null

            return when (versionParts[1]) {
                // Can't support 1.16.5 bc 1.16.5 paper userdev do not exist

                21 -> when (versionParts[2]) {
                    0, 1 -> V1_21R1_PacketManager()
                    2, 3 -> V1_21R2_PacketManager()
                    4 -> V1_21R3_PacketManager()
                    5 -> V1_21R4_PacketManager()
                    else -> null
                }

                else -> null
            }
        }
}
