package xyz.alexcrea.cuanvil.util

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
import org.bukkit.inventory.view.AnvilView
import org.bukkit.persistence.PersistentDataType
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.group.ConflictType
import kotlin.math.min

object AnvilXpUtil {

    const val EXCLUSIVE_PENALTY_PREFIX = "repair_cost"

    /**
     * Display xp needed for the work on the anvil inventory
     * Use the current view repair cost. used to force our price
     */
    fun setAnvilInvXp(
        view: AnvilView,
        player: HumanEntity,
        ignoreRules: Boolean = false
    ) {
        setAnvilInvXp(view, player, view.repairCost, ignoreRules)
    }

    /**
     * Display xp needed for the work on the anvil inventory
     */
    fun setAnvilInvXp(
        view: AnvilView,
        player: HumanEntity,
        anvilCost: Int,
        ignoreRules: Boolean = false
    ) {
        // Test repair cost limit
        val finalAnvilCost = if (
            !ignoreRules &&
            !ConfigOptions.doRemoveCostLimit &&
            ConfigOptions.doCapCost
        ) {
            min(anvilCost, ConfigOptions.maxAnvilCost)
        } else {
            anvilCost
        }

        val maximumRepairCost =
            if (ConfigOptions.doRemoveCostLimit || ignoreRules) {
                Int.MAX_VALUE
            } else {
                ConfigOptions.maxAnvilCost + 1
            }

        // Try first just in case another plugin, or the test need this
        view.maximumRepairCost = maximumRepairCost
        view.repairCost = finalAnvilCost

        /* Because Minecraft likes to have the final say in the repair cost displayed
            * we need to wait for the event to end before overriding it, this ensures that
            * we have the final say in the process. */
        DependencyManager.scheduler.scheduleOnEntity(
            CustomAnvil.instance, player
        ) {
            // retry after a tick
            view.maximumRepairCost = maximumRepairCost
            view.repairCost = finalAnvilCost

            if (player !is Player) return@scheduleOnEntity

            if (player.gameMode != GameMode.CREATIVE) {
                val bypassToExpensive = (ConfigOptions.doReplaceTooExpensive) &&
                        (finalAnvilCost >= 40) &&
                        finalAnvilCost < view.maximumRepairCost

                DependencyManager.packetManager.setInstantBuild(player, bypassToExpensive)
            }

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