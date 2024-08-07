package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.entity.Player

abstract class AbstractPacketManager : PacketManager {
    override val canSetInstantBuild: Boolean
        get() = false

    override fun setInstantBuild(player: Player, instantBuild: Boolean) {
        // Default empty.
    }

}
