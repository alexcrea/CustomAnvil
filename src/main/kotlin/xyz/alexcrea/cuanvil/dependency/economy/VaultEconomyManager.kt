package xyz.alexcrea.cuanvil.dependency.economy

import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.Plugin
class VaultEconomyManager : EconomyManager {

    val economy: Economy?

    constructor(plugin: Plugin) {
        val rsp = plugin.server.servicesManager.getRegistration(Economy::class.java)
        economy = rsp?.getProvider()
    }

    override fun initialized(): Boolean {
        return economy != null
    }


}