package io.delilaheve.util

import io.delilaheve.CustomAnvil
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.group.ConflictType
import xyz.alexcrea.cuanvil.group.EnchantConflictManager
import xyz.alexcrea.cuanvil.util.MaterialUtil.customType
import kotlin.math.max
import kotlin.math.min

/**
 * Enchantment manipulation utilities
 */
object EnchantmentUtil {

    /**
     * Enchantment name without namespace
     */
    val CAEnchantment.enchantmentName: String
        get() = key.key


    private fun maxLevel(enchantment: CAEnchantment, bypassLevel: Boolean): Int {
        val max = if(bypassLevel) {
            255
        } else {
            ConfigOptions.enchantLimit(enchantment)
        }

        CustomAnvil.verboseLog("Max level of ${enchantment.key} is $max (bypassLevel is $bypassLevel)")
        return max
    }

    /**
     * Combine 2 sets of enchantments according to our configuration
     */
    fun Map<CAEnchantment, Int>.combineWith(
        other: Map<CAEnchantment, Int>,
        item: ItemStack,
        player: HumanEntity,
    ) = mutableMapOf<CAEnchantment, Int>().apply {
        putAll(this@combineWith)

        CustomAnvil.verboseLog("Testing merge")
        val bypassFuse = player.hasPermission(CustomAnvil.bypassFusePermission)
        val bypassLevel = player.hasPermission(CustomAnvil.bypassLevelPermission)

        var maxEnchantCount = ConfigOptions.getEnchantCountLimit(item.customType)
        if(maxEnchantCount == null || maxEnchantCount < 0) maxEnchantCount = Int.MAX_VALUE

        val allowed = other.filter {(enchantment, _) -> enchantment.isAllowed(player)}
        val new = allowed.filter {(enchantment, _) -> !containsKey(enchantment)}
        val old = allowed.filter {(enchantment, _) -> containsKey(enchantment)}

        ConfigHolder.CONFLICT.read.use {lock ->
            val conflictManager = lock.get().conflictManager
            old.forEach {(enchantment, level) ->
                val failed = processOldEnchantment(
                    conflictManager, enchantment,
                    level, bypassLevel,
                    bypassFuse, item
                )
                if(failed) return@forEach
            }

            // Try to add new now
            new.forEach {(enchantment, level) ->
                val failed = processNewEnchantments(
                    conflictManager, enchantment,
                    level, bypassLevel,
                    bypassFuse, item,
                    maxEnchantCount
                )
                if(failed) return@forEach
            }
        }

    }

    private fun MutableMap<CAEnchantment, Int>.processOldEnchantment(
        conflictManager: EnchantConflictManager,
        enchantment: CAEnchantment,
        level: Int,
        bypassLevel: Boolean,
        bypassFuse: Boolean,
        item: ItemStack,
    ): Boolean {
        // Get max level or 255 if player can bypass
        val maxLevel = maxLevel(enchantment, bypassLevel)
        val cappedLevel = min(level, maxLevel)

        val oldLevel = this[enchantment]!! // <- should not be null. (enchantment already in result list)

        // ... and they're not the same level
        if(oldLevel != cappedLevel) {
            // apply the greater of the two or left one if right is above max
            this[enchantment] = max(oldLevel, cappedLevel)
        }
        // ... and they're the same level
        else {
            if(sameLevelMerge(bypassLevel, enchantment, oldLevel, maxLevel)) return true
        }

        if(bypassFuse) {
            CustomAnvil.verboseLog("Bypassed conflict check for ${enchantment.key}")
        } else {
            val conflictType = conflictManager
                .isConflicting(this, item, enchantment)

            // ... and they are conflicting
            if(conflictType != ConflictType.NO_CONFLICT) {
                CustomAnvil.verboseLog(
                    "Enchantment already in result list, and they are conflicting (${enchantment.key}, conflict: $conflictType)"
                )
                this[enchantment] = oldLevel
                return true
            }
        }
        return false
    }

    private fun MutableMap<CAEnchantment, Int>.processNewEnchantments(
        conflictManager: EnchantConflictManager,
        enchantment: CAEnchantment,
        level: Int,
        bypassLevel: Boolean,
        bypassFuse: Boolean,
        item: ItemStack,
        maxEnchantCount: Int,
    ): Boolean {
        // Get max level or 255 if player can bypass
        val maxLevel = maxLevel(enchantment, bypassLevel)
        val cappedLevel = min(level, maxLevel)

        // Do not allow new enchantment if above maximum
        if(this.size >= maxEnchantCount) return true

        // Add the enchantment if it doesn't have conflicts, or if player is allowed to bypass enchantment restrictions
        this[enchantment] = cappedLevel
        if(bypassFuse) {
            CustomAnvil.verboseLog("Bypassed conflict check for ${enchantment.key}")
            return true
        }

        val conflictType = conflictManager.isConflicting(this, item, enchantment)

        if(conflictType != ConflictType.NO_CONFLICT) {
            CustomAnvil.verboseLog("Enchantment not yet in result list, but there is conflict (${enchantment.key}, conflict: $conflictType)")
            this.remove(enchantment)
        }
        return false
    }

    private fun MutableMap<CAEnchantment, Int>.sameLevelMerge(
        bypassLevel: Boolean,
        enchantment: CAEnchantment,
        oldLevel: Int,
        maxLevel: Int,
    ): Boolean {
        // We test if it is allowed to merge at this level
        if(!bypassLevel) {
            val maxBeforeDisabled = ConfigOptions.maxBeforeMergeDisabled(enchantment)
            if((maxBeforeDisabled > 0) && (oldLevel >= maxBeforeDisabled)) {
                CustomAnvil.verboseLog(
                    "Reached max merge before disable for ${enchantment.key}: $oldLevel/$maxBeforeDisabled)"
                )
                return true
            }
        }

        // Now we increase the enchantment level by 1
        var newLevel = oldLevel + 1
        newLevel = max(min(newLevel, maxLevel), oldLevel)
        this[enchantment] = newLevel
        return false
    }
}

