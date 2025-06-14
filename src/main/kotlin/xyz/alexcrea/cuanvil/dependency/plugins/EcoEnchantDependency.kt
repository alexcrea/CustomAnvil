package xyz.alexcrea.cuanvil.dependency.plugins

import com.willfp.ecoenchants.enchant.EcoEnchant
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

    fun disableAnvilListener() {
        PrepareAnvilEvent.getHandlerList().unregister(this.ecoEnchantPlugin)
    }

    private var ecoEnchantOldEnchantments: MutableSet<EcoEnchant>? = null
    fun registerEnchantments() {
        CustomAnvil.instance.logger.info("Preparing Eco Enchant compatibility...")

        val enchantments = EcoEnchants.values()
        for (ecoEnchant in enchantments) {
            EnchantmentApi.unregisterEnchantment(ecoEnchant.enchantment) // As eco enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
            EnchantmentApi.registerEnchantment(CAEcoEnchant(ecoEnchant))
        }

        ecoEnchantOldEnchantments = HashSet(enchantments)

        CustomAnvil.instance.logger.info("Eco Enchant should now work as expected !")
    }

    fun handleConfigReload() {
        // Should not happen in known case.
        if (this.ecoEnchantOldEnchantments == null) return

        val newEnchantments = EcoEnchants.values()

        // Add new enchantments
        for (ecoEnchant in newEnchantments)
            if (!this.ecoEnchantOldEnchantments!!.contains(ecoEnchant))
                EnchantmentApi.registerEnchantment(CAEcoEnchant(ecoEnchant))

        // Remove old enchantments that not now currently used
        this.ecoEnchantOldEnchantments!!.removeAll(newEnchantments)
        for (oldEnchantment in this.ecoEnchantOldEnchantments!!) {
            EnchantmentApi.unregisterEnchantment(oldEnchantment.enchantment)
        }

        this.ecoEnchantOldEnchantments = HashSet(newEnchantments)
    }

}
