package xyz.alexcrea.cuanvil.group

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class EnchantConflictGroup(private val cantConflict: AbstractMaterialGroup, private val minBeforeBlock: Int){

    private val enchantments = HashSet<Enchantment>()

    fun addEnchantment(ench: Enchantment){
        enchantments.add(ench)
    }
    fun allowed(enchants: Set<Enchantment>, mat: Material) : Boolean{
        if(enchantments.size < minBeforeBlock){
            return true
        }

        if(cantConflict.contain(mat)){
            return true
        }

        // Count the amount of enchantment that are in the list
        var enchantAmount = 0
        for (enchantment in enchants) {
            if(enchantment !in enchantments) continue
            if(++enchantAmount > minBeforeBlock){
                return false
            }

        }
        return true
    }

    fun getEnchants(): HashSet<Enchantment> {
        return enchantments
    }

}