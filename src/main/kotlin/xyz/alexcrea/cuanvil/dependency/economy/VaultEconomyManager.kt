@file:Suppress("DEPRECATION")

package xyz.alexcrea.cuanvil.dependency.economy

import net.milkbowl.vault.economy.Economy
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.math.BigDecimal

class VaultEconomyManager : EconomyManager {

    val economy: Economy?

    constructor(plugin: Plugin) {
        val rsp = plugin.server.servicesManager.getRegistration(Economy::class.java)
        economy = rsp?.getProvider()
    }

    override fun initialized(): Boolean {
        return economy != null
    }

    override fun has(player: Player, money: BigDecimal): Boolean {
        if (money.signum() <= 0) return true

        return economy!!.has(player, money.toDouble())
    }

    override fun remove(player: Player, money: BigDecimal): Boolean {
        if (money.signum() <= 0) return true

        return economy!!.withdrawPlayer(player, money.toDouble()).transactionSuccess()
    }

    override fun format(money: BigDecimal): String {
        return economy!!.format(money.toDouble())
    }

}