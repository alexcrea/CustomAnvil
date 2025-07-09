package xyz.alexcrea.cuanvil.listener

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.AnvilInventory
import xyz.alexcrea.cuanvil.dependency.packet.PacketManagerBase

class AnvilCloseListener(private val packetManager: PacketManagerBase) : Listener {

    @EventHandler
    fun onAnvilClose(event: InventoryCloseEvent) {
        val player = event.player
        if (event.inventory !is AnvilInventory) return
        if (player is Player && GameMode.CREATIVE != player.gameMode) {
            packetManager.setInstantBuild(player, false)
        }

    }

}