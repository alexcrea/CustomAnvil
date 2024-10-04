package io.delilaheve.util

import io.delilaheve.CustomAnvil
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.group.ConflictType
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

    /**
     * Combine 2 sets of enchantments according to our configuration
     */
    fun Map<CAEnchantment, Int>.combineWith(
        other: Map<CAEnchantment, Int>,
        item: ItemStack,
        player: HumanEntity
    ) = mutableMapOf<CAEnchantment, Int>().apply {
        putAll(this@combineWith)

        other.forEach { (enchantment, level) ->
            if(!enchantment.isAllowed(player)) return@forEach

            // Get max level or 255 if player can bypass
            val maxLevel = if (player.hasPermission(CustomAnvil.bypassLevelPermission))
            { 255 } else
            { ConfigOptions.enchantLimit(enchantment) }

            val cappedLevel = min(level, maxLevel)

            // Enchantment not yet in result list
            if (!containsKey(enchantment)) {
                // Add the enchantment if it doesn't have conflicts, or if player is allowed to bypass enchantment restrictions
                this[enchantment] = cappedLevel
                val conflictType =
                    ConfigHolder.CONFLICT_HOLDER.conflictManager.isConflicting(this, item, enchantment)
                if (!player.hasPermission(CustomAnvil.bypassFusePermission) &&
                    (conflictType != ConflictType.NO_CONFLICT)
                ) {
                    CustomAnvil.verboseLog("Enchantment not yet in result list, but there is conflict (${enchantment.key}, conflict: $conflictType)")
                    this.remove(enchantment)
                }

            }
            // Enchantment already in result list
            else {
                val oldLevel = this[enchantment]!! // <- should not be null. (enchantment already in result list)

                // ... and they are conflicting
                val conflictType =
                    ConfigHolder.CONFLICT_HOLDER.conflictManager.isConflicting(this, item, enchantment)
                if ((conflictType != ConflictType.NO_CONFLICT)
                    && !player.hasPermission(CustomAnvil.bypassFusePermission)
                ) {
                    CustomAnvil.verboseLog("Enchantment already in result list, and they are conflicting (${enchantment.key}, conflict: $conflictType)")
                    return@forEach
                }

                // ... and they're not the same level
                if (oldLevel != cappedLevel) {
                    // apply the greater of the two or left one if right is above max
                    this[enchantment] = max(oldLevel, cappedLevel)

                }
                // ... and they're the same level
                else {
                    // We test if it is allowed to merge at this level
                    val maxBeforeDisabled = ConfigOptions.maxBeforeMergeDisabled(enchantment)
                    if((maxBeforeDisabled > 0) && (oldLevel >= maxBeforeDisabled)) return@forEach

                    // Now we increase the enchantment level by 1
                    var newLevel = oldLevel + 1
                    newLevel = max(min(newLevel, maxLevel), oldLevel)
                    this[enchantment] = newLevel

                }
            }
        }
    }

}
