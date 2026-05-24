package xyz.alexcrea.cuanvil.dependency.economy

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.math.BigDecimal

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

    // We assume "initialized" got checked before these function get called
    fun has(player: Player, money: BigDecimal): Boolean
    fun remove(player: Player, money: BigDecimal): Boolean

    fun format(money: BigDecimal): String;

}
