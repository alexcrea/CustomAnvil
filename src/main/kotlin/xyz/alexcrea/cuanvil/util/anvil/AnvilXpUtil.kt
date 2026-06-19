package xyz.alexcrea.cuanvil.util.anvil

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.EnchantmentUtil.enchantmentName
import io.delilaheve.util.ItemUtil.findEnchantments
import io.delilaheve.util.ItemUtil.isEnchantedBook
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Repairable
import org.bukkit.persistence.PersistentDataType
import xyz.alexcrea.cuanvil.anvil.AnvilCost
import xyz.alexcrea.cuanvil.anvil.AnvilUseType
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.economy.EconomyManager
import xyz.alexcrea.cuanvil.group.ConflictType
import xyz.alexcrea.cuanvil.util.AnvilTitleUtil
import xyz.alexcrea.cuanvil.util.dialog.AnvilRenameDialogUtil

object AnvilXpUtil {

    const val EXCLUSIVE_PENALTY_PREFIX = "repair_cost"

    /**
     * Display the required cost (either as xp or as )
     */
    fun setAnvilInvCost(
        inventory: AnvilInventory,
        view: InventoryView,
        player: Player,
        cost: AnvilCost,
        ignoreRules: Boolean = false
    ) {
        if (ConfigOptions.shouldUseMoney(player)) {
            cost.isMonetary = true
            setAnvilPrice(inventory, view, player, cost)
        } else
            setAnvilInvXp(inventory, view, player, cost.filteredXpCost(ignoreRules), ignoreRules)
    }

    fun maximumXpCost(ignoreRules: Boolean = false): Int {
        return if (ConfigOptions.doRemoveCostLimit || ignoreRules) {
            Int.MAX_VALUE
        } else {
            ConfigOptions.maxAnvilCost + 1
        }
    }

    /**
     * Display xp needed for the work on the anvil inventory
     */
    private fun setAnvilInvXp(
        inventory: AnvilInventory,
        view: InventoryView,
        player: HumanEntity,
        anvilCost: Int,
        ignoreRules: Boolean = false
    ) {
        val maximumRepairCost = maximumXpCost(ignoreRules)

        // Try first just in case another plugin, or the test need this
        inventory.maximumRepairCost = maximumRepairCost
        inventory.repairCost = anvilCost
        // TODO for 2.x.x use anvil view & set directly there

        /* Because Minecraft likes to have the final say in the repair cost displayed
            * we need to wait for the event to end before overriding it, this ensures that
            * we have the final say in the process. */
        DependencyManager.scheduler.scheduleOnEntity(
            CustomAnvil.instance, player
        ) {
            // retry after a tick
            inventory.maximumRepairCost = maximumRepairCost
            inventory.repairCost = anvilCost
            // TODO for 2.x.x use anvil view & set directly there

            if (player !is Player) return@scheduleOnEntity

            if (player.gameMode != GameMode.CREATIVE) {
                val bypassToExpensive = (ConfigOptions.doReplaceTooExpensive) &&
                        (anvilCost >= 40) &&
                        anvilCost < inventory.maximumRepairCost

                DependencyManager.packetManager.setInstantBuild(player, bypassToExpensive)
            }

            player.updateInventory()
        }
    }

    /**
     * Display monetary cost needed for the work on the anvil inventory
     */
    private fun setAnvilPrice(
        inventory: AnvilInventory,
        view: InventoryView,
        player: Player,
        cost: AnvilCost,
    ) {
        val finalCost = cost.asMonetaryCost()

        val has = player.gameMode == GameMode.CREATIVE ||
                EconomyManager.economy!!.has(player, finalCost)

        val text = "Cost: " + (if (has) "§2" else "§4") +
                EconomyManager.economy!!.format(finalCost)
        AnvilTitleUtil.rename(
            view, text,
            player,
            AnvilRenameDialogUtil.anvilRenameDialog,
            CustomAnvil.instance
        )

        clearAnvilXpCost(inventory, view, player)
    }

    private fun clearAnvilXpCost(
        inventory: AnvilInventory,
        view: InventoryView,
        player: HumanEntity,
    ) {
        // TODO for 2.x.x use anvil view & set directly there
        inventory.repairCost = 0

        // retry after a tick
        DependencyManager.scheduler.scheduleOnEntity(
            CustomAnvil.instance, player
        ) {
            inventory.repairCost = 0

            if (player !is Player) return@scheduleOnEntity
            player.updateInventory()
        }
    }

    /**
     * Function to calculate work penalty of anvil work
     * Also change result work penalty if right item is not null
     */
    fun calculatePenalty(left: ItemStack, right: ItemStack?, result: ItemStack, useType: AnvilUseType): Int {
        // Extracted From https://minecraft.wiki/w/Anvil_mechanics#Enchantment_equation
        // Calculate work penalty
        val penaltyType = ConfigOptions.workPenaltyPart(useType)
        val leftPenalty = (left.itemMeta as? Repairable)?.repairCost ?: 0
        val leftExclusivePenalty = findExclusivePenalty(left, useType)

        val rightPenalty =
            if (right == null) 0
            else (right.itemMeta as? Repairable)?.repairCost ?: 0
        val rightExclusivePenalty = findExclusivePenalty(right, useType)

        // Increase penalty on fusing or unit repair
        if (penaltyType.penaltyIncrease) {
            result.itemMeta?.let {
                (it as? Repairable)?.repairCost = leftPenalty.coerceAtLeast(rightPenalty) * 2 + 1
                result.itemMeta = it
            }
        }
        if (penaltyType.exclusivePenaltyIncrease) {
            val resultPenalty = leftExclusivePenalty.coerceAtLeast(rightExclusivePenalty) * 2 + 1
            setExclusivePenalty(result, resultPenalty, useType)
        }

        CustomAnvil.log(
            "Calculated penalty: " +
                    "leftPenalty: $leftPenalty, " +
                    "rightPenalty: $rightPenalty, " +
                    "result penalty: ${(result.itemMeta as? Repairable)?.repairCost ?: "none"}"
        )

        var resultSum = 0
        if (penaltyType.penaltyAdditive) {
            resultSum += leftPenalty + rightPenalty
        }
        if (penaltyType.exclusivePenaltyAdditive) {
            resultSum += leftExclusivePenalty + rightExclusivePenalty
        }

        return resultSum
    }

    fun onNoResult(player: HumanEntity, view: InventoryView) {
        if (ConfigOptions.shouldUseMoney(player))
            AnvilTitleUtil.rename(
                view, "Repair & Name",
                player,
                AnvilRenameDialogUtil.anvilRenameDialog,
                CustomAnvil.instance
            )
    }

    private fun exclusivePenaltyKey(useType: AnvilUseType): NamespacedKey {
        return NamespacedKey(CustomAnvil.instance, "${EXCLUSIVE_PENALTY_PREFIX}_${useType.typeName}")
    }

    private fun setExclusivePenalty(
        result: ItemStack,
        resultPenalty: Int,
        useType: AnvilUseType
    ) {
        val key = exclusivePenaltyKey(useType)

        val meta = result.itemMeta!!
        meta.persistentDataContainer.set(key, PersistentDataType.INTEGER, resultPenalty)
        result.itemMeta = meta
    }

    private fun findExclusivePenalty(
        item: ItemStack?,
        useType: AnvilUseType
    ): Int {
        if (item == null || !item.hasItemMeta()) return 0
        val key = exclusivePenaltyKey(useType)

        val meta = item.itemMeta!!
        return meta.persistentDataContainer.get(key, PersistentDataType.INTEGER) ?: return 0
    }

    /**
     * Function to calculate right enchantment values
     * it include enchantment placed on final item and conflicting enchantment
     */
    fun getRightValues(right: ItemStack, result: ItemStack, cost: AnvilCost) {
        // Calculate right value and illegal enchant penalty

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
                    cost.illegalPenalty += ConfigOptions.sacrificeIllegalCost
                    CustomAnvil.verboseLog("Big conflict. Adding illegal price penalty")
                }
                continue
            }
            // We know "enchantment.key in resultEnchs" true
            val resultLevel = resultEnchs[enchantment.key]!!

            val enchantmentMultiplier = ConfigOptions.enchantmentValue(enchantment.key, rightIsFormBook)
            val value = resultLevel * enchantmentMultiplier
            CustomAnvil.log("Value for ${enchantment.key.enchantmentName} level ${enchantment.value} is $value ($resultLevel * $enchantmentMultiplier)")
            cost.enchantment += value

        }
        CustomAnvil.log(
            "Calculated right values: " +
                    "rightValue: ${cost.enchantment}, " +
                    "illegalPenalty: ${cost.illegalPenalty}"
        )
    }

    /**
     * Calculate the maximum level reachable with this amount of `xp`
     * This is equivalent of the displayed level on client
     * @author provided by kFor
     */
    fun calculateLevelForXp(xp: Int): Int {
        return when {
            xp <= 352 -> (Math.sqrt((xp + 9).toDouble()) - 3).toInt()
            xp <= 1507 -> {
                val inner = (2.0 / 5.0) * (xp - 7839.0 / 40.0)
                (81.0 / 10.0 + Math.sqrt(inner)).toInt()
            }

            else -> {
                val inner = (2.0 / 9.0) * (xp - 54215.0 / 72.0)
                (325.0 / 18.0 + Math.sqrt(inner)).toInt()
            }
        }
    }

    /**
     * Calculate the minimum level necessary to have at least `xp`
     */
    fun calculateMinimumLevelForXp(xp: Int): Int {
        return calculateLevelForXp(xp - 1) + 1
    }

    /**
     * Calculate the minimum amount of xp necessary to reach `level`
     * @author provided by kFor
     */
    fun calculateXpForLevel(level: Int): Int {
        return when {
            level <= 16 -> (level * level + 6 * level)
            level <= 31 -> (2.5 * level * level - 40.5 * level + 360).toInt()
            else -> (4.5 * level * level - 162.5 * level + 2220).toInt()
        }
    }

}