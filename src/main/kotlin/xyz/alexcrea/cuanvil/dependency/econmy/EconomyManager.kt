package xyz.alexcrea.cuanvil.dependency.econmy

import net.milkbowl.vault.economy.Economy
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider

object EconomyManager {

    private var economy: Economy? = null

    fun setupEconomy(plugin: Plugin) {
        if (economy != null) return

        if (plugin.server.pluginManager.getPlugin("Vault") == null)
            return

        val rsp: RegisteredServiceProvider<Economy?>? =
            plugin.server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) return

        economy = rsp.getProvider()
    }

    fun has(player: OfflinePlayer, amount: Double): Boolean {
        return economy?.has(player, amount) == true
    }



}
