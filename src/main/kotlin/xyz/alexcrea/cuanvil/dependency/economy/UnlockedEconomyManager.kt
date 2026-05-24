package xyz.alexcrea.cuanvil.dependency.economy

import io.delilaheve.util.ConfigOptions
import net.milkbowl.vault2.economy.Economy
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.math.BigDecimal

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

    private fun currency(): String {
        val configured = ConfigOptions.usedCurrency

        return if ("default".equals(configured, true))
            economy!!.getDefaultCurrency(plugin)
        else configured
    }

    override fun has(player: Player, money: BigDecimal): Boolean {
        if(money.signum() <= 0) return true

        return economy!!.has(plugin,
            player.uniqueId,
            player.world.name,
            currency(),
            money)
    }

    override fun remove(player: Player, money: BigDecimal): Boolean {
        if(money.signum() <= 0) return true

        return economy!!.withdraw(plugin,
            player.uniqueId,
            player.world.name,
            currency(),
            money)
            .transactionSuccess()
    }

    override fun format(money: BigDecimal): String {
        return economy!!.format(plugin, money, currency())
    }


}