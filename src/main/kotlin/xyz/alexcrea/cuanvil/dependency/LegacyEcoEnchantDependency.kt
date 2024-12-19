package xyz.alexcrea.cuanvil.dependency

import com.willfp.ecoenchants.enchantments.EcoEnchant
import com.willfp.ecoenchants.enchantments.EcoEnchants
import org.bukkit.enchantments.Enchantment
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CALegacyEcoEnchant

class LegacyEcoEnchantDependency {


    private var ecoEnchantOldEnchantments: MutableSet<EcoEnchant>? = null
    fun registerEnchantments() {
        val enchantments = EcoEnchants.values()
        for (ecoEnchant in enchantments) {
            ecoEnchant as Enchantment

            EnchantmentApi.unregisterEnchantment(ecoEnchant) // As eco enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
            EnchantmentApi.registerEnchantment(CALegacyEcoEnchant(ecoEnchant, ecoEnchant))
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
                EnchantmentApi.registerEnchantment(CALegacyEcoEnchant(ecoEnchant, ecoEnchant as Enchantment))


        // Remove old enchantments that not now currently used
        this.ecoEnchantOldEnchantments!!.removeAll(newEnchantments)
        for (oldEnchantment in this.ecoEnchantOldEnchantments!!) {
            EnchantmentApi.unregisterEnchantment(oldEnchantment as Enchantment)
        }

        this.ecoEnchantOldEnchantments = HashSet(newEnchantments)
    }

}
