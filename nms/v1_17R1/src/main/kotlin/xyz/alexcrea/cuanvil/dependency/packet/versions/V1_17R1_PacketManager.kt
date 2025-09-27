package xyz.alexcrea.cuanvil.dependency.packet.versions

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
import net.minecraft.world.entity.player.Abilities
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.PacketManagerBase

class V1_17R1_PacketManager : PacketManagerBase(), PacketManager {
    override val canSetInstantBuild: Boolean
        get() = true

    override fun setInstantBuild(player: Player, instantBuild: Boolean) {
        val nmsPlayer = (player as CraftPlayer).handle
        val playerAbilities = nmsPlayer.abilities
        val sendedAbilities: Abilities
        if (playerAbilities.instabuild == instantBuild) {
            sendedAbilities = playerAbilities
        } else {
            sendedAbilities = Abilities()
            sendedAbilities.invulnerable = playerAbilities.invulnerable
            sendedAbilities.flying = playerAbilities.flying
            sendedAbilities.mayfly = playerAbilities.mayfly
            sendedAbilities.instabuild = instantBuild
            sendedAbilities.mayBuild = playerAbilities.mayBuild
            sendedAbilities.flyingSpeed = playerAbilities.flyingSpeed
            sendedAbilities.walkingSpeed = playerAbilities.walkingSpeed
        }
        val packet = ClientboundPlayerAbilitiesPacket(sendedAbilities)
        nmsPlayer.connection.send(packet)
    }
}
