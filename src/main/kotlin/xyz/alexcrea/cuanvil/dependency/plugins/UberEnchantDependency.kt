package xyz.alexcrea.cuanvil.dependency.plugins

import me.sciguymjm.uberenchant.api.UberEnchantment
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CAUberEnchantment

object UberEnchantDependency {

    fun registerAll() {
        for(enchantment in UberEnchantment.getRegisteredEnchantments()) {
            try {
                // Some enchantment do not work for some reason. this sorts them out
                enchantment.displayName
                EnchantmentApi.registerEnchantment(CAUberEnchantment(enchantment))
            } catch(_: Exception) { }
        }
    }


}