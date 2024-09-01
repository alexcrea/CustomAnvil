package xyz.alexcrea.cuanvil.util

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.EnchantmentUtil.enchantmentName
import io.delilaheve.util.ItemUtil.findEnchantments
import io.delilaheve.util.ItemUtil.isEnchantedBook
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.InventoryView.Property.REPAIR_COST
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Repairable
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.group.ConflictType
import kotlin.math.min

object AnvilXpUtil {

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
    /**
     * Function to calculate work penalty of anvil work
     * Also change result work penalty if right item is not null
     */
    fun calculatePenalty(left: ItemStack, right: ItemStack?, result: ItemStack): Int {
        return calculatePenalty(left, right, result, false)
    }

    /**
     * Function to calculate work penalty of anvil work
     * Also change result work penalty if right item is not null
     */
    fun calculatePenalty(left: ItemStack, right: ItemStack?, result: ItemStack, unitRepair: Boolean): Int {
        // Extracted From https://minecraft.fandom.com/wiki/Anvil_mechanics#Enchantment_equation
        // Calculate work penalty
        val penaltyType = ConfigOptions.workPenaltyType;
        val leftPenalty = (left.itemMeta as? Repairable)?.repairCost ?: 0


        val rightPenalty =
            if (right == null) 0
            else (right.itemMeta as? Repairable)?.repairCost ?: 0


        // Increase penalty on fusing or unit repair
        if(penaltyType.isPenaltyIncreasing && (right != null || unitRepair)){
            result.itemMeta?.let {
                (it as? Repairable)?.repairCost = leftPenalty * 2 + 1
                result.itemMeta = it

            }
        }

        CustomAnvil.log(
            "Calculated penalty: " +
                    "leftPenalty: $leftPenalty, " +
                    "rightPenalty: $rightPenalty, " +
                    "result penalty: ${(result.itemMeta as? Repairable)?.repairCost ?: "none"}"
        )

        if(!penaltyType.isPenaltyAdditive) return 0

        return leftPenalty + rightPenalty
    }

    /**
     * Function to calculate right enchantment values
     * it include enchantment placed on final item and conflicting enchantment
     */
    fun getRightValues(right: ItemStack, result: ItemStack): Int {
        // Calculate right value and illegal enchant penalty
        var illegalPenalty = 0
        var rightValue = 0

        val rightIsFormBook = right.isEnchantedBook()
        val resultEnchs = result.findEnchantments()
        val resultEnchsKeys = HashMap(resultEnchs)

        for (enchantment in right.findEnchantments()) {
            // count enchant as illegal enchant if it conflicts with another enchant or not in result
            if ((enchantment.key !in resultEnchsKeys)) {
                resultEnchsKeys[enchantment.key] = enchantment.value
                val conflictType = ConfigHolder.CONFLICT_HOLDER.conflictManager.isConflicting(
                    resultEnchsKeys,
                    result,
                    enchantment.key
                )
                resultEnchsKeys.remove(enchantment.key)

                if (ConflictType.ENCHANTMENT_CONFLICT == conflictType) {
                    illegalPenalty += ConfigOptions.sacrificeIllegalCost
                    CustomAnvil.verboseLog("Big conflict. Adding illegal price penalty")
                }
                continue
            }
            // We know "enchantment.key in resultEnchs" true
            val resultLevel = resultEnchs[enchantment.key]!!

            val enchantmentMultiplier = ConfigOptions.enchantmentValue(enchantment.key, rightIsFormBook)
            val value = resultLevel * enchantmentMultiplier
            CustomAnvil.log("Value for ${enchantment.key.enchantmentName} level ${enchantment.value} is $value ($resultLevel * $enchantmentMultiplier)")
            rightValue += value

        }
        CustomAnvil.log(
            "Calculated right values: " +
                    "rightValue: $rightValue, " +
                    "illegalPenalty: $illegalPenalty"
        )

        return rightValue + illegalPenalty
    }

}