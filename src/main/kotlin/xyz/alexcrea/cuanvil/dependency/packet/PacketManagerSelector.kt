package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.Bukkit
import xyz.alexcrea.cuanvil.dependency.packet.versions.V1_18R1_Manager
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
    private val versionSpecificManager: PacketManager?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersion()
            if (versionParts[0] != 1) return null;

            return when (versionParts[1]) {
                16 -> when (versionParts[2]) {
                    4, 5 -> null // TODO V1_16R3 (if possible)
                    else -> null
                }

                17 -> when (versionParts[2]) {
                    0, 1 -> null // TODO V1_17R1 (if possible)
                    else -> null
                }

                18 -> when (versionParts[2]) {
                    0, 1 -> V1_18R1_Manager()
                    2 -> null // TODO V1_18R2
                    else -> null
                }

                19 -> when (versionParts[2]) {
                    0, 1, 2 -> null // TODO V1_19R1
                    3 -> null // TODO V1_19R2
                    4 -> null // TODO V1_19R3
                    else -> null
                }

                20 -> when (versionParts[2]) {
                    0, 1 -> null // TODO V1_20R1
                    2 -> null // TODO V1_20R2
                    3, 4 -> null // TODO V1_20R3
                    5, 6 -> null // TODO V1_20R4
                    else -> null
                }

                21 -> when (versionParts[2]) {
                    0 -> null // TODO V1_21R1
                    else -> null
                }

                else -> null
            }
        }
}
