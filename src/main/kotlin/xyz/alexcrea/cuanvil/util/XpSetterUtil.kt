package xyz.alexcrea.cuanvil.util

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.InventoryView.Property.REPAIR_COST
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import kotlin.math.min

object XpSetterUtil {

    /**
     * Display xp needed for the work on the anvil inventory
     */
    fun setAnvilInvXp(
        inventory: AnvilInventory,
        view: InventoryView,
        anvilCost: Int,
        ignoreRules: Boolean = false
    ) {
        // Test repair cost limit
        val finalAnvilCost = if (
            !ignoreRules &&
            !ConfigOptions.doRemoveCostLimit &&
            ConfigOptions.doCapCost) {
            min(anvilCost, ConfigOptions.maxAnvilCost)
        } else {
            anvilCost
        }

        /* Because Minecraft likes to have the final say in the repair cost displayed
            * we need to wait for the event to end before overriding it, this ensures that
            * we have the final say in the process. */
        CustomAnvil.instance
            .server
            .scheduler
            .runTask(CustomAnvil.instance, Runnable {
                inventory.maximumRepairCost =
                    if (ConfigOptions.doRemoveCostLimit || ignoreRules)
                    { Int.MAX_VALUE }
                    else
                    { ConfigOptions.maxAnvilCost + 1 }

                val player = view.player

                inventory.repairCost = finalAnvilCost
                view.setProperty(REPAIR_COST, finalAnvilCost)
                player.openInventory.setProperty(REPAIR_COST, finalAnvilCost)

                if(player is Player){
                    if(player.gameMode != GameMode.CREATIVE ){
                        val bypassToExpensive = (ConfigOptions.doReplaceTooExpensive) &&
                                (finalAnvilCost >= 40) &&
                                finalAnvilCost < inventory.maximumRepairCost

                        DependencyManager.packetManager.setInstantBuild(player, bypassToExpensive)
                    }

                    player.updateInventory()
                }
            })
    }

}