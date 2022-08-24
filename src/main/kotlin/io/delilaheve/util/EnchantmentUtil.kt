package io.delilaheve.util

import io.delilaheve.UnsafeEnchants
import org.bukkit.enchantments.Enchantment
import kotlin.math.max
import kotlin.math.min

/**
 * Enchantment manipulation utilities
 */
object EnchantmentUtil {

    /**
     * Enchantment name without namespace
     */
    val Enchantment.enchantmentName: String
        get() = key.key

    /**
     * Combine 2 sets of enchantments according to our configuration
     */
    fun Map<Enchantment, Int>.combineWith(
        other: Map<Enchantment, Int>
    ) = mutableMapOf<Enchantment, Int>().apply {
        putAll(this@combineWith)
        other.forEach { (enchantment, level) ->
            when {
                // Enchantment not yet in result list
                !containsKey(enchantment) -> {
                    // Add the enchantment if it doesn't have conflicts, or, if we're allowing unsafe enchantments
                    if (!keys.any { enchantment.conflictsWith(it) } || ConfigOptions.allowUnsafe) {
                        this[enchantment] = level
                    }
                }
                // Enchantment already in result list...
                else -> when {
                    // ... and they're not the same level
                    this[enchantment] != other[enchantment] -> {
                        val newLevel = max(this[enchantment] ?: 0, other[enchantment] ?: 0)
                        // apply the greater of the two if non-zero
                        if (newLevel > 0) { this[enchantment] = newLevel }
                    }
                    // ... and they're the same level
                    else -> {
                        // try to increase the enchantment level by 1
                        var newLevel = this[enchantment]?.plus(1) ?: 0
                        val maxLevel = ConfigOptions.enchantLimit(enchantment)
                        newLevel = min(newLevel, maxLevel)
                        if (newLevel > 0) { this[enchantment] = newLevel }
                    }
                }
            }
        }
    }

    /**
     * Check if a set of enchantments has any conflicts
     */
    fun Map<Enchantment, Int>.hasConflicts() : Boolean {
        forEach { (enchantment1, _) ->
            val hasConflict = any { (enchantment2, _) ->
                enchantment2.conflictsWith(enchantment1)
            }
            if (hasConflict) {
                return hasConflict
            }
        }
        return false
    }

    /**
     * Calculate the value of a set of enchantments
     */
    fun Map<Enchantment, Int>.calculateValue(
        fromBook: Boolean
    ) = entries.sumOf { (enchantment, level) ->
        val enchantmentMultiplier = ConfigOptions.enchantmentValue(enchantment, fromBook)
        val value = level * enchantmentMultiplier
        UnsafeEnchants.log("Value for ${enchantment.enchantmentName} is $value")
        value
    }

}
