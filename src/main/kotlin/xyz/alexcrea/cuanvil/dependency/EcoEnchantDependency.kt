package xyz.alexcrea.cuanvil.dependency

import com.willfp.ecoenchants.enchant.EcoEnchants
import io.delilaheve.CustomAnvil
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEcoEnchant

class EcoEnchantDependency(private val ecoEnchantPlugin: Plugin) {

    init {
        CustomAnvil.instance.logger.info("Eco Enchant Detected !")
    }

    fun disableAnvilListener(){
        PrepareAnvilEvent.getHandlerList().unregister(this.ecoEnchantPlugin)
    }

    fun registerEnchantments() {
        CustomAnvil.instance.logger.info("Preparing Eco Enchant compatibility...")

        for (ecoEnchant in EcoEnchants.values()) {
            EnchantmentApi.unregisterEnchantment(ecoEnchant.enchantment) // As eco enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
            EnchantmentApi.registerEnchantment(CAEcoEnchant(ecoEnchant))
        }

        CustomAnvil.instance.logger.info("Eco Enchant should now work as expected !")
    }

}
