package xyz.alexcrea.cuanvil.dependency

import me.athlaeos.enchantssquared.managers.CustomEnchantManager
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment
import xyz.alexcrea.cuanvil.enchant.wrapped.EnchantSquaredEnchantment

class EnchantmentSquaredDependency(private val enchantmentSquaredPlugin: Plugin) {

    fun registerEnchantements(){
        for (enchant in CustomEnchantManager.getInstance().allEnchants.values) {
            WrappedEnchantment.register(EnchantSquaredEnchantment(enchant, enchantmentSquaredPlugin))
        }

    }

}
