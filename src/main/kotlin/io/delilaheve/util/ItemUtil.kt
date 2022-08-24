package io.delilaheve.util

import io.delilaheve.UnsafeEnchants
import io.delilaheve.util.EnchantmentUtil.calculateValue
import io.delilaheve.util.ItemUtil.isBook
import org.bukkit.Material.BOOK
import org.bukkit.Material.ENCHANTED_BOOK
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.Repairable
import kotlin.math.min

/**
 * Item manipulation utilities
 */
object ItemUtil {

    /**
     * Determine the value of an item for repair
     *
     * ToDo - use native repair cost
     */
    val ItemStack.repairCost: Int
        get() {
            val nativeCost = (itemMeta as? Repairable)?.repairCost
            val pluginCost = findEnchantments().calculateValue(isBook())
            UnsafeEnchants.log("Native Cost: $nativeCost; Plugin Cost: $pluginCost")
            return nativeCost.takeIf { it != 0 } ?: pluginCost
        }

    /**
     * Check if this [ItemStack] is a [BOOK] or [ENCHANTED_BOOK]
     */
    fun ItemStack.isBook() = type in listOf(BOOK, ENCHANTED_BOOK)

    /**
     * Check if this [ItemStack] is an [ENCHANTED_BOOK]
     */
    private fun ItemStack.isEnchantedBook() = type == ENCHANTED_BOOK

    /**
     * Determine if this [ItemStack] can hold enchants, this should be sufficient for
     * detecting if an item is a tool/armour/etc... and not a carrot/potato/etc...
     */
    private fun ItemStack.canHoldEnchants() = Enchantment.values()
        .any { it.canEnchantItem(this) }

    /**
     * Find the enchantment map for this [ItemStack] and return it as a [MutableMap]
     */
    fun ItemStack.findEnchantments() = if (isBook()) {
        (itemMeta as? EnchantmentStorageMeta)?.storedEnchants ?: emptyMap()
    } else {
        itemMeta?.enchants ?: emptyMap()
    }

    /**
     * Apply an [enchantments] map to this [ItemStack]
     */
    fun ItemStack.setEnchantmentsUnsafe(enchantments: Map<Enchantment, Int>) {
        if (isBook()) {
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
     */
    fun ItemStack.repairFrom(
        first: ItemStack,
        second: ItemStack
    ) {
        (itemMeta as? Damageable)?.let {
            val maxDurability = type.maxDurability.toInt()
            val firstDurability = (first.itemMeta as? Damageable)?.damage ?: 0
            val secondDurability = (second.itemMeta as? Damageable)?.damage ?: 0
            var newDurability = firstDurability + secondDurability
            newDurability = min(maxDurability, newDurability)
            it.damage = newDurability
            itemMeta = it as ItemMeta
        }
    }

    /**
     * Check that this [ItemStack] can merge with the [other]
     *
     * The two items should either be the same type, or, the [other] is a book
     */
    fun ItemStack.canMergeWith(
        other: ItemStack
    ) = type == other.type || (canHoldEnchants() && other.isEnchantedBook())
}
