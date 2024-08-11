package xyz.alexcrea.cuanvil.dependency.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException

class ProtocoLibWrapper: PacketManager {

    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager();

    override val canSetInstantBuild: Boolean
        get() = true

    override fun setInstantBuild(player: Player, instantBuild: Boolean) {
        val packet = PacketContainer(PacketType.Play.Server.ABILITIES)

        // Set player's properties
        packet.float
            .write(0, player.flySpeed / 2)
            .write(1, player.walkSpeed / 2)

        packet.booleans
            .write(0, player.isInvulnerable)
            .write(1, player.isFlying)
            .write(2, player.allowFlight)
            .write(3, instantBuild)

        // Send packet
        try {
            protocolManager.sendServerPacket(player, packet)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

}
