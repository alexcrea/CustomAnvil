package xyz.alexcrea.cuanvil.dependency.protocolib

import org.bukkit.entity.Player

interface PacketManager {

    val isProtocoLibInstalled: Boolean

    fun setInstantBuild(player: Player, instantBuild: Boolean)

}
