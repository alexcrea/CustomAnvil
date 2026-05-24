package xyz.alexcrea.cuanvil.dependency.economy

import org.bukkit.plugin.Plugin

interface EconomyManager {

    companion object {
        var economy: EconomyManager? = null

        fun setupEconomy(plugin: Plugin) {
            if (plugin.server.pluginManager.getPlugin("Vault") == null)
                return
            if(UnlockedEconomyManager.unlockedAvailable())
                economy = UnlockedEconomyManager(plugin)

            if(economy == null || !economy!!.initialized())
                economy = VaultEconomyManager(plugin)
        }

    }

    fun initialized(): Boolean




}
