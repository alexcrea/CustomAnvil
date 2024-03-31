package xyz.alexcrea.cuanvil.group

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class EnchantConflictGroup(
    val name: String,
    private val cantConflict: AbstractMaterialGroup,
    val minBeforeBlock: Int
) {

    private val enchantments = HashSet<Enchantment>()

    fun addEnchantment(enchant: Enchantment) {
        enchantments.add(enchant)
    }

    fun allowed(enchants: Set<Enchantment>, mat: Material): Boolean {
        if (enchantments.size < minBeforeBlock) {
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
                return false
            }

        }
        return true
    }

    fun getCantConflictGroup(): AbstractMaterialGroup {
        return this.cantConflict
    }

    fun getEnchants(): HashSet<Enchantment> {
        return enchantments
    }

    fun setEnchants(enchants: Set<Enchantment>) {
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

}