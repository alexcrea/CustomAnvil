package xyz.alexcrea.cuanvil.dependency

import com.willfp.ecoenchants.enchant.EcoEnchants
import io.delilaheve.CustomAnvil
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEcoEnchant

class EcoEnchantDependency(private val ecoEnchantPlugin: Plugin) {

    init {
        CustomAnvil.instance.logger.info("Eco Enchant Detected !")
    }

    fun disableAnvilListener(){
        PrepareAnvilEvent.getHandlerList().unregister(this.ecoEnchantPlugin)
    }

    fun registerEnchantments() {
        val registery = CAEnchantmentRegistry.getInstance()
        for (ecoEnchant in EcoEnchants.values()) {
            val enchantments: CAEnchantment = CAEcoEnchant(ecoEnchant)

            registery.unregister(registery.getByKey(ecoEnchant.enchantment.key)) // As
            registery.register(enchantments)
        }
    }

}
