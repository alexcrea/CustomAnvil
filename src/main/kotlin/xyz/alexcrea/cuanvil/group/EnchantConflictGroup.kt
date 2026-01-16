package xyz.alexcrea.cuanvil.group

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import xyz.alexcrea.cuanvil.enchant.CAEnchantment

class EnchantConflictGroup(
    val name: String,
    private val cantConflict: AbstractMaterialGroup,
    var minBeforeBlock: Int,
) {

    private val enchantments = HashSet<CAEnchantment>()
    private val conflictAfterLevel = HashMap<CAEnchantment, Int>()

    fun addEnchantment(enchant: CAEnchantment) {
        enchantments.add(enchant)
    }
    fun addEnchantments(enchants: List<CAEnchantment>) {
        enchantments.addAll(enchants)
    }

    private fun canBypassConflictByLevel(enchants: Map<CAEnchantment, Int>): Boolean {
        // Either there no "conflict after"
        if(conflictAfterLevel.isEmpty()) return false

        // Or we check if any conflict after enchantment is true
        for (entry in conflictAfterLevel) {
            val current = enchants.getOrDefault(entry.key, 0)
            if(current > entry.value)
                return false
        }

        return true
    }

    fun allowed(enchants: Map<CAEnchantment, Int>, mat: Material): Boolean {
        if (enchantments.size < minBeforeBlock) {
            CustomAnvil.verboseLog("Conflicting bc of to many enchantments")
            return true
        }

        if (cantConflict.contain(mat))
            return true

        // If empty we skip. else we
        if(canBypassConflictByLevel(enchants))
            return true

        // Count the amount of enchantment that are in the list
        var enchantAmount = 0
        for (entry in enchants) {
            val enchantment = entry.key

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

    fun getEnchants(): HashSet<CAEnchantment> {
        return enchantments
    }

    fun setEnchants(enchants: Set<CAEnchantment>) {
        enchantments.clear()
        enchantments.addAll(enchants)
    }

    fun getConflictAfters(): HashMap<CAEnchantment, Int> {
        return conflictAfterLevel
    }

    fun putConflictAfterLevel(enchantment: CAEnchantment, level: Int): Boolean {
        return null != (
                if(level < 0) conflictAfterLevel.remove(enchantment)
                else conflictAfterLevel.put(enchantment, level))
    }

    fun setConflictAfterLevel(conflictAfterLevel: HashMap<CAEnchantment, Int>) {
        this.conflictAfterLevel.clear()
        this.conflictAfterLevel.putAll(conflictAfterLevel)
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