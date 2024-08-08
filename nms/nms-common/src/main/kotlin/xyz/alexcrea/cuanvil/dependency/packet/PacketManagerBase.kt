package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.entity.Player
import org.bukkit.event.Listener

abstract class PacketManagerBase() : PacketManager, Listener {

    override val canSetInstantBuild: Boolean
        get() = false

    override fun setInstantBuild(player: Player, instantBuild: Boolean) {
        // Default implementation is empty.
    }


}
