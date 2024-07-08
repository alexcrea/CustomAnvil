package xyz.alexcrea.cuanvil.dependency

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.dependency.protocolib.NoProtocoLib
import xyz.alexcrea.cuanvil.dependency.protocolib.PacketManager
import xyz.alexcrea.cuanvil.dependency.protocolib.ProtocoLibWrapper
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import java.io.File

object DependencyManager {

    lateinit var packetManager: PacketManager
    var enchantmentSquaredCompatibility: EnchantmentSquaredDependency? = null
    var ecoEnchantCompatibility: EcoEnchantDependency? = null

    fun loadDependency(){
        val pluginManager = Bukkit.getPluginManager()

        // ProtocolLib dependency
        packetManager =
            if(pluginManager.isPluginEnabled("ProtocolLib")) ProtocoLibWrapper()
        else  NoProtocoLib()

        // Enchantment Squared dependency
        if(pluginManager.isPluginEnabled("EnchantsSquared")){
            enchantmentSquaredCompatibility = EnchantmentSquaredDependency(pluginManager.getPlugin("EnchantsSquared")!!)
            enchantmentSquaredCompatibility!!.disableAnvilListener()
        }

        // EcoEnchants dependency
        if(pluginManager.isPluginEnabled("EcoEnchants")){
            ecoEnchantCompatibility = EcoEnchantDependency(pluginManager.getPlugin("EcoEnchants")!!)
            ecoEnchantCompatibility!!.disableAnvilListener()
        }

    }

    fun handleConfigChanges(plugin: Plugin) {
        val folder = File(plugin.dataFolder, "compatibility")

        enchantmentSquaredCompatibility?.registerPluginConfiguration()
        ecoEnchantCompatibility?.registerPluginConfiguration(folder)

    }

    fun writeDefaultConfig(defaultConfig: FileConfiguration, enchantment: CAEnchantment) {
        defaultConfig["enchant_limits.${enchantment.key.key}"] = enchantment.defaultMaxLevel()

        val rarity = enchantment.defaultRarity()
        defaultConfig["enchant_values.${enchantment.key.key}.item"] = rarity.itemValue
        defaultConfig["enchant_values.${enchantment.key.key}.book"] = rarity.bookValue
    }

}
