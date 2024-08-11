package xyz.alexcrea.cuanvil.dependency.packet

import org.bukkit.entity.Player

class NoPacketManager: PacketManager {

    override val canSetInstantBuild: Boolean
        get() = false

    override fun setInstantBuild(player: Player, instantBuild: Boolean) {
        // ProtocoLib not installed and not in a supported version: We do nothing
    }

}
