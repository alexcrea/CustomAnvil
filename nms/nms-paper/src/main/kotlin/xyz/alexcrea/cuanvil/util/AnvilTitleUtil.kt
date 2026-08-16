package xyz.alexcrea.cuanvil.util

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.InventoryView
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.dialog.AnvilRenameDialog
import java.util.HashMap
import java.util.UUID

object AnvilTitleUtil {

    private val runTaskMap = HashMap<UUID, ScheduledTask>()

    @Suppress("DEPRECATION")
    private fun actualRename(view: InventoryView, name: String, player: HumanEntity, anvilDialog: AnvilRenameDialog) {
        runTaskMap.remove(player.uniqueId)
        if (view.title == name) return

        // We assume rename impl is used
        if (anvilDialog.isOpenFor(player)) return

        view.title = name
    }

    // We don't want to rename instantly it is causing issue with rename text
    // especially as it can "override" current ui when it is rename ui time but rename ui also need some delay
    fun rename(view: InventoryView, name: String, player: HumanEntity, anvilDialog: AnvilRenameDialog, plugin: Plugin) {
        runTaskMap.remove(player.uniqueId)?.cancel()

        val task = player.scheduler.runDelayed(
            plugin,
            { _ ->
                run { actualRename(view, name, player, anvilDialog) }
            },
            {
                runTaskMap.remove(player.uniqueId)
            },
            2
        )

        if (task == null) return
        runTaskMap[player.uniqueId] = task
    }

}