package xyz.alexcrea.cuanvil.group

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment

class EnchantConflictGroup(
    private val name: String,
    private val cantConflict: AbstractMaterialGroup,
    var minBeforeBlock: Int
) {

    private val enchantments = HashSet<WrappedEnchantment>()

    fun addEnchantment(enchant: WrappedEnchantment) {
        enchantments.add(enchant)
    }

    fun allowed(enchants: Set<WrappedEnchantment>, mat: Material): Boolean {
        if (enchantments.size < minBeforeBlock) {
            CustomAnvil.verboseLog("Conflicting bc of to many enchantments")
            return true
        }

        if (cantConflict.contain(mat)) {
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

    fun getCantConflictGroup(): AbstractMaterialGroup {
        return this.cantConflict
    }

    fun getEnchants(): HashSet<WrappedEnchantment> {
        return enchantments
    }

    fun setEnchants(enchants: Set<WrappedEnchantment>) {
        enchantments.clear()
        enchantments.addAll(enchants)
    }

    fun getRepresentativeMaterial(): Material {
        val groups = getCantConflictGroup().getGroups()
        val groupIterator = groups.iterator()
        while (groupIterator.hasNext()) {
            val mat = groupIterator.next().getRepresentativeMaterial()
            if (mat != Material.ENCHANTED_BOOK) return mat

        }
        return Material.ENCHANTED_BOOK
    }

    override fun toString(): String {
        return name
    }

}