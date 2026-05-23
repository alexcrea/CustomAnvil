package io.delilaheve.util

import org.bukkit.Material
import org.bukkit.Material.ENCHANTED_BOOK
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import xyz.alexcrea.cuanvil.update.UpdateUtils
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.util.MaterialUtil.customType
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

    private fun maxDamage(type: Material, damageable: Damageable): Int {
        val ver = UpdateUtils.currentMinecraftVersion()
        if(ver.major <= 1 && ver.minor <= 20 && ver.patch < 5) return type.maxDurability.toInt()

        return if(damageable.hasMaxDamage()) damageable.maxDamage else type.maxDurability.toInt()
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
        val meta = itemMeta
        if(meta !is Damageable) return false

        val maxDamage = maxDamage(type, meta)
        val damage = (first.itemMeta as? Damageable)?.damage ?: 0
        if (damage == 0) return false

        val firstDurability = maxDamage - damage
        val secondDamage = (second.itemMeta as? Damageable)?.damage ?: 0
        val secondDurability = maxDamage - secondDamage
        val combinedDurability = firstDurability + secondDurability
        val newDurability = min(combinedDurability, maxDamage)
            val maxDamage = if(it.hasMaxDamage()) it.maxDamage else Int.MAX_VALUE

        meta.damage = min(maxDamage - newDurability, maxDamage)
        this.itemMeta = meta
        return true
    }

    fun ItemStack.unitRepair(
        unitAmount: Int,
        percentPerUnit: Double
    ): Int {
        (itemMeta as? Damageable)?.let {
            val durability = type.maxDurability.toInt()
            val firstDamage = it.damage
            if (firstDamage == 0) return 0
            var unitCount = 0
            var damage = firstDamage
            while ((unitCount < unitAmount) && (damage > 0)) {
                unitCount++
                damage = ceil(firstDamage - durability * percentPerUnit * unitCount).toInt()
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
    ) = (other != null) && (customType == other.customType || (other.isEnchantedBook()))
}
