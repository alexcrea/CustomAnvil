package xyz.alexcrea.cuanvil.dependency.packet.versions

import net.minecraft.network.protocol.game.PacketPlayOutAbilities
import net.minecraft.world.entity.player.PlayerAbilities
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.PacketManagerBase

class V1_21R4_PacketManager : PacketManagerBase(), PacketManager {
    override val canSetInstantBuild: Boolean
        get() = true

    override fun setInstantBuild(player: Player, instantBuild: Boolean) {
        val nmsPlayer = (player as CraftPlayer).handle
        val playerAbilities: PlayerAbilities = nmsPlayer.gk()
        val sendedAbilities: PlayerAbilities

        if (playerAbilities.d == instantBuild) {
            sendedAbilities = playerAbilities
        } else {
            sendedAbilities = PlayerAbilities()
            sendedAbilities.a = playerAbilities.a
            sendedAbilities.b = playerAbilities.b
            sendedAbilities.c = playerAbilities.c
            sendedAbilities.d = instantBuild
            sendedAbilities.e = playerAbilities.e
            sendedAbilities.m = playerAbilities.m
            sendedAbilities.n = playerAbilities.n
        }
        val packet = PacketPlayOutAbilities(sendedAbilities)
        nmsPlayer.f.sendPacket(packet)
    }
}
