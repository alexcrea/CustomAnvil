package io.delilaheve.util

import io.delilaheve.UnsafeEnchants
import org.bukkit.Material.ENCHANTED_BOOK
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Item manipulation utilities
 */
object ItemUtil {

    /**
     * Check if this [ItemStack] is an [ENCHANTED_BOOK]
     */
    fun ItemStack.isEnchantedBook() = type == ENCHANTED_BOOK

    /**
     * Find the enchantment map for this [ItemStack] and return it as a [MutableMap]
     */
    fun ItemStack.findEnchantments() = if (isEnchantedBook()) {
        (itemMeta as? EnchantmentStorageMeta)?.storedEnchants ?: emptyMap()
    } else {
        itemMeta?.enchants ?: emptyMap()
    }

    /**
     * Apply an [enchantments] map to this [ItemStack]
     */
    fun ItemStack.setEnchantmentsUnsafe(enchantments: Map<Enchantment, Int>) {
        if (isEnchantedBook()) {
            /* For some god-forsaken reason, item meta is not mutable
             * so, we have to get the instance, modify it, then set it
             * back to the item... #BecauseMinecraft */
            val bookMeta = (itemMeta as? EnchantmentStorageMeta)
            bookMeta?.replaceEnchants(enchantments)
            itemMeta = bookMeta
        } else {
            itemMeta?.enchants?.forEach { (enchant, _) ->
                removeEnchantment(enchant)
            }
            addUnsafeEnchantments(enchantments)
        }
    }

    /**
     * Apply an [enchantments] map to this book
     */
    private fun EnchantmentStorageMeta.replaceEnchants(
        enchantments: Map<Enchantment, Int>
    ) {
        storedEnchants.forEach { (enchant, _) ->
            removeStoredEnchant(enchant)
        }
        enchantments.forEach { (enchant, level) ->
            val added = addStoredEnchant(enchant, level, true)
            UnsafeEnchants.log("${enchant.key} added to item? $added")
        }
    }

    /**
     * Set this [ItemStack]s durability from a combination of the
     * [first] and [second] item's durability values
     * @return if the item was repaired
     */
    fun ItemStack.repairFrom(
        first: ItemStack,
        second: ItemStack
    ): Boolean {
        (itemMeta as? Damageable)?.let {
            val durability = type.maxDurability.toInt()
            val firstDamage = (first.itemMeta as? Damageable)?.damage ?: 0
            if( firstDamage == 0) return false

            val firstDurability = durability - firstDamage
            val secondDamage = (second.itemMeta as? Damageable)?.damage ?: 0
            val secondDurability = durability - secondDamage
            val combinedDurability = firstDurability + secondDurability
            val newDurability = min(combinedDurability, durability)
            it.damage = durability - newDurability
            itemMeta = it
            return true
        }
        return false
    }

    fun ItemStack.unitRepair(
        unitAmount: Int,
        percentPerUnit: Double
    ): Int {
        (itemMeta as? Damageable)?.let {
            val durability = type.maxDurability.toInt()
            val firstDamage = it.damage
            if( firstDamage == 0) return 0
            var unitCount = 0
            var damage = firstDamage
            while((unitCount < unitAmount) && (damage > 0)){
                unitCount++
                damage = ceil(firstDamage - durability*percentPerUnit*unitCount).toInt()
            }

            it.damage = max(damage, 0)
            itemMeta = it
            return unitCount
        }
        return 0
    }

    /**
     * Check that this [ItemStack] can merge with the [other]
     *
     * The two items should either be the same type, or, the [other] is a book
     */
    fun ItemStack.canMergeWith(
        other: ItemStack?
    ) = (other != null) && (type == other.type || (other.isEnchantedBook()))
}
