package xyz.alexcrea.cuanvil.dependency

import io.delilaheve.CustomAnvil
import org.bukkit.plugin.Plugin
import su.nightexpress.excellentenchants.registry.EnchantRegistry
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEEEnchantment

class ExcellentEnchantsDependency(private val excellentEnchantsPlugin: Plugin){

    init {
        CustomAnvil.instance.logger.info("Excellent Enchants Detected !")
    }

    fun registerEnchantments() {
        CustomAnvil.instance.logger.info("Preparing Excellent Enchants compatibility...")

        for (enchantment in EnchantRegistry.getRegistered()) {
            EnchantmentApi.unregisterEnchantment(enchantment.bukkitEnchantment.key) // As excellent enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
            EnchantmentApi.registerEnchantment(CAEEEnchantment(enchantment))
        }

        CustomAnvil.instance.logger.info("\"Excellent Enchants should now work as expected !")
    }

}
