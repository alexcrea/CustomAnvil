package xyz.alexcrea.cuanvil.dependency.economy

import net.milkbowl.vault2.economy.Economy
import org.bukkit.plugin.Plugin

class UnlockedEconomyManager: EconomyManager {

    val plugin: String
    val economy: Economy?

    companion object {
        fun unlockedAvailable(): Boolean {
            try {
                Class.forName("net.milkbowl.vault2.economy.Economy")
                return true
            } catch (_: ClassNotFoundException) {
                return false
            }
        }
    }

    constructor(plugin: Plugin) {
        this.plugin = plugin.name

        val rsp = plugin.server.servicesManager.getRegistration(Economy::class.java)
        economy = rsp?.getProvider()
    }

    override fun initialized(): Boolean {
        return economy != null
    }



}