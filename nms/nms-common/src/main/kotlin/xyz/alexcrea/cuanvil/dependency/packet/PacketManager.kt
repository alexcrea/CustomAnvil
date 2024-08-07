package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.entity.Player

interface PacketManager {

    /**
     * If the provided packet manager if able to set instant build.
     */
    val canSetInstantBuild: Boolean

    /**
     * Try to set instant build properties
     */
    fun setInstantBuild(player: Player, instantBuild: Boolean)

}
