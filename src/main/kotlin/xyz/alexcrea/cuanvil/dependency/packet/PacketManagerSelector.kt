package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.Bukkit
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.MinecraftVersionUtil
import xyz.alexcrea.cuanvil.dependency.packet.versions.*
import xyz.alexcrea.cuanvil.update.UpdateUtils

object PacketManagerSelector {

    private const val PAPER_CRAFT_PLAYER_CLASS = "org.bukkit.craftbukkit.entity.CraftPlayer"

    fun selectPacketManager(forceProtocolib: Boolean): PacketManager {
        // Try to find version
        if(DependencyManager.inTesting)
            return NoPacketManager()

        return if (forceProtocolib)
            protocolibIfPresent
        else {
            try {
                Class.forName(PAPER_CRAFT_PLAYER_CLASS)

                return PaperPacketManager()
            } catch (_: ClassNotFoundException) {
                return reobfPacketManager ?: protocolibIfPresent
            }
        }
    }

    private val protocolibIfPresent: PacketManager
        get() =
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
                ProtocoLibWrapper()
            else
                NoPacketManager()

    // Reobfuscated packet manager for spigot or paper as it remap
    private val reobfPacketManager: PacketManagerBase?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersion()
            if (versionParts.major != 1) return null

            try {
                val clazz = Class.forName("xyz.alexcrea.cuanvil.dependency.packet.versions." +
                        "V${MinecraftVersionUtil.craftbukkitVersion}_PacketManager")

                val manager = clazz.getConstructor().newInstance()
                return manager as PacketManagerBase
            } catch (_: ClassNotFoundException) {
                return null
            }
        }
}
