package xyz.alexcrea.cuanvil.dependency

import me.athlaeos.enchantssquared.enchantments.CustomEnchant
import me.athlaeos.enchantssquared.managers.CustomEnchantManager
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment
import xyz.alexcrea.cuanvil.enchant.wrapped.EnchantSquaredEnchantment
import java.util.*

class EnchantmentSquaredDependency(private val enchantmentSquaredPlugin: Plugin) {

    fun registerEnchantments(){
        for (enchant in CustomEnchantManager.getInstance().allEnchants.values) {
            WrappedEnchantment.register(EnchantSquaredEnchantment(enchant))
        }

    }

    fun getEnchantmentsSquared(item: ItemStack, enchantments: MutableMap<WrappedEnchantment, Int>) {
        val customEnchants = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(item)

        customEnchants.forEach{
                (enchantment, level ) -> enchantments[getWrappedEnchant(enchantment)] = level
        }

    }

    fun clearEnchantments(item: ItemStack) {
        CustomEnchantManager.getInstance().removeAllEnchants(item)
    }

    fun getKeyFromEnchant(enchant: CustomEnchant): NamespacedKey{
        return NamespacedKey.fromString(enchant.type.lowercase(Locale.getDefault()), this.enchantmentSquaredPlugin)!!
    }
    private fun getWrappedEnchant(enchant: CustomEnchant): WrappedEnchantment{
        return WrappedEnchantment.getByKey(getKeyFromEnchant(enchant))!!
    }

}
