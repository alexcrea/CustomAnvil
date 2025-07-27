package xyz.alexcrea.cuanvil.group

import io.delilaheve.CustomAnvil
import org.bukkit.inventory.ItemType
import xyz.alexcrea.cuanvil.enchant.CAEnchantment

@Suppress("UnstableApiUsage")
class EnchantConflictGroup(
    val name: String,
    private val cantConflict: AbstractItemTypeGroup,
    var minBeforeBlock: Int
) {

    private val enchantments = HashSet<CAEnchantment>()

    fun addEnchantment(enchant: CAEnchantment) {
        enchantments.add(enchant)
    }

    fun addEnchantments(enchants: List<CAEnchantment>) {
        enchantments.addAll(enchants)
    }

    fun allowed(enchants: Set<CAEnchantment>, type: ItemType): Boolean {
        if (enchantments.size < minBeforeBlock) {
            CustomAnvil.verboseLog("Conflicting bc of to many enchantments")
            return true
        }

        if (cantConflict.contain(type)) {
            return true
        }

        // Count the amount of enchantment that are in the list
        var enchantAmount = 0
        for (enchantment in enchants) {
            if (enchantment !in enchantments) continue
            CustomAnvil.verboseLog("Enchant ${enchantment.key} is in: ${enchantAmount + 1}/$minBeforeBlock ")
            if (++enchantAmount > minBeforeBlock) {
                CustomAnvil.verboseLog("it is not allowed bc of to many enchantment in conflict")
                return false
            }

        }
        return true
    }

    fun getCantConflictGroup(): AbstractItemTypeGroup {
        return this.cantConflict
    }

    fun getEnchants(): HashSet<CAEnchantment> {
        return enchantments
    }

    fun setEnchants(enchants: Set<CAEnchantment>) {
        enchantments.clear()
        enchantments.addAll(enchants)
    }

    fun getRepresentativeMaterial(): ItemType {
        val groups = getCantConflictGroup().getGroups()
        val groupIterator = groups.iterator()
        while (groupIterator.hasNext()) {
            val itemType = groupIterator.next().getRepresentativeMaterial()
            if (itemType != ItemType.ENCHANTED_BOOK) return itemType

        }
        return ItemType.ENCHANTED_BOOK
    }

    override fun toString(): String {
        return name
    }

}