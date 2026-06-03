package xyz.alexcrea.cuanvil.listener

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.view.AnvilView
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager
import xyz.alexcrea.cuanvil.util.dialog.AnvilRenameDialogUtil

@Suppress("UnstableApiUsage")
class AnvilCloseListener(private val packetManager: PacketManager) : Listener {

    @EventHandler
    fun onAnvilClose(event: InventoryCloseEvent) {
        val player = event.player
        if (event.view !is AnvilView) return
        if (player is Player && GameMode.CREATIVE != player.gameMode) {
            packetManager.setInstantBuild(player, false)
        }

        AnvilRenameDialogUtil.anvilRenameDialog.closeInventory(player)
    }

}