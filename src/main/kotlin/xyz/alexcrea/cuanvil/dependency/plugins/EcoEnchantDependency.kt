package xyz.alexcrea.cuanvil.dependency.plugins

import com.willfp.ecoenchants.enchant.EcoEnchant
import com.willfp.ecoenchants.enchant.EcoEnchants
import io.delilaheve.CustomAnvil
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEcoEnchant

class EcoEnchantDependency(private val ecoEnchantPlugin: Plugin) {

    private val isLegacy: Boolean
    private val legacyDependency: LegacyEcoEnchantDependency?

    init {
        CustomAnvil.instance.logger.info("Eco Enchant Detected !")

        var isLegacy = true
        try {
            Class.forName("com.willfp.ecoenchants.enchant.EcoEnchants")
            isLegacy = false
        } catch (_: ClassNotFoundException) {
        }

        this.isLegacy = isLegacy;
        if (isLegacy) {
            this.legacyDependency = LegacyEcoEnchantDependency()
        } else {
            this.legacyDependency = null
        }

    }

    fun disableAnvilListener() {
        PrepareAnvilEvent.getHandlerList().unregister(this.ecoEnchantPlugin)
    }

    private var ecoEnchantOldEnchantments: MutableSet<EcoEnchant>? = null
    fun registerEnchantments() {
        CustomAnvil.instance.logger.info("Preparing Eco Enchant compatibility...")

        if (isLegacy) {
            legacyDependency!!.registerEnchantments();
            return
        }

        val enchantments = EcoEnchants.values()
        for (ecoEnchant in enchantments) {
            EnchantmentApi.unregisterEnchantment(ecoEnchant.enchantment) // As eco enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
            EnchantmentApi.registerEnchantment(CAEcoEnchant(ecoEnchant))
        }

        ecoEnchantOldEnchantments = HashSet(enchantments)

        CustomAnvil.instance.logger.info("Eco Enchant should now work as expected !")
    }

    fun handleConfigReload() {
        if (isLegacy) {
            legacyDependency!!.handleConfigReload()
            return
        }

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
