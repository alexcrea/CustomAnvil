package xyz.alexcrea.cuanvil.dependency

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.dependency.protocolib.NoProtocoLib
import xyz.alexcrea.cuanvil.dependency.protocolib.PacketManager
import xyz.alexcrea.cuanvil.dependency.protocolib.ProtocoLibWrapper
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

}
