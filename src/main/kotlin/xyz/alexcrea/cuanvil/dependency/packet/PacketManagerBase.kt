package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.entity.Player

open class PacketManagerBase {

    open val canSetInstantBuild: Boolean
        get() = false

    open fun setInstantBuild(player: Player, instantBuild: Boolean) {
        // Default implementation is empty.
    }


}