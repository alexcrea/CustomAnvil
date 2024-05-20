package xyz.alexcrea.cuanvil.packet

import org.bukkit.entity.Player

class NoProtocoLib: PacketManager {
    override val isProtocoLibInstalled: Boolean
        get() = false

    // ProtocoLib not installed: We do nothing

    override fun setInstantBuild(player: Player, instantBuild: Boolean) {}

}
