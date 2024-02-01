package xyz.alexcrea.group

import io.delilaheve.util.ItemUtil.findEnchantments
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class EnchantConflictGroup(val cantConflict: MaterialGroup, val minBeforeBlock: Int){

    private val enchantments = HashSet<Enchantment>()

    fun addEnchantment(ench: Enchantment){
        enchantments.add(ench)
    }

    fun allow(item: ItemStack) : Boolean{
        if(enchantments.size < minBeforeBlock){
            return true
        }

        if(cantConflict.contain(item.type)){
            return true
        }

        // Count the amount of enchantment that are in the list
        var enchantAmount = 0
        for (enchantment in item.findEnchantments().keys) {
            if(enchantment !in enchantments) continue
            if(++enchantAmount > minBeforeBlock){
                return false
            }

        }

        return true
    }

    fun isEnchantEmpty(): Boolean {
        return enchantments.size == 0
    }


}