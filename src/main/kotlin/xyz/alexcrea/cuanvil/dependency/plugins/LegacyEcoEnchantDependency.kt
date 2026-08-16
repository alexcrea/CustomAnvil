package xyz.alexcrea.cuanvil.dependency.plugins

import com.willfp.ecoenchants.enchantments.EcoEnchant
import com.willfp.ecoenchants.enchantments.EcoEnchants
import org.bukkit.enchantments.Enchantment
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CALegacyEcoEnchant

class LegacyEcoEnchantDependency {


    private var ecoEnchantOldEnchantments: MutableSet<EcoEnchant>? = null
    private fun unregisterEnchantment(enchant: Any) {
        EnchantmentApi.unregisterEnchantment((enchant as Enchantment).key)
    }

    private fun registerEnchantment(ecoEnchant: EcoEnchant, enchant: Any) {
        EnchantmentApi.registerEnchantment(CALegacyEcoEnchant(ecoEnchant, enchant as Enchantment))
    }

    fun registerEnchantments() {
        val enchantments = EcoEnchants.values()
        for (ecoEnchant in enchantments) {
            // As eco enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
            unregisterEnchantment(ecoEnchant)
            registerEnchantment(ecoEnchant, ecoEnchant)
        }

        ecoEnchantOldEnchantments = HashSet(enchantments)
    }

    fun handleConfigReload() {
        // Should not happen in known case.
        if (this.ecoEnchantOldEnchantments == null) return

        val newEnchantments = EcoEnchants.values()

        // Add new enchantments
        for (ecoEnchant in newEnchantments)
            if (!this.ecoEnchantOldEnchantments!!.contains(ecoEnchant))
                registerEnchantment(ecoEnchant, ecoEnchant)


        // Remove old enchantments that not now currently used
        this.ecoEnchantOldEnchantments!!.removeAll(newEnchantments)
        for (oldEnchantment in this.ecoEnchantOldEnchantments!!) {
            unregisterEnchantment(oldEnchantment)
        }

        this.ecoEnchantOldEnchantments = HashSet(newEnchantments)
    }

}
