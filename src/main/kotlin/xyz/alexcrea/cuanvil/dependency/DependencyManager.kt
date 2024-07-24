package xyz.alexcrea.cuanvil.dependency

import org.bukkit.Bukkit
import xyz.alexcrea.cuanvil.dependency.protocolib.NoProtocoLib
import xyz.alexcrea.cuanvil.dependency.protocolib.PacketManager
import xyz.alexcrea.cuanvil.dependency.protocolib.ProtocoLibWrapper

object DependencyManager {

    lateinit var packetManager: PacketManager
    var enchantmentSquaredCompatibility: EnchantmentSquaredDependency? = null
    var ecoEnchantCompatibility: EcoEnchantDependency? = null

    fun loadDependency(){
        val pluginManager = Bukkit.getPluginManager()

        // ProtocolLib dependency
        packetManager =
            if(pluginManager.isPluginEnabled("ProtocolLib")) ProtocoLibWrapper()
        else NoProtocoLib()

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

    fun handleCompatibilityConfig() {
        enchantmentSquaredCompatibility?.registerPluginConfiguration()

    }

    fun registerEnchantments() {
        enchantmentSquaredCompatibility?.registerEnchantments()
        ecoEnchantCompatibility?.registerEnchantments()

    }

    fun handleConfigReload(){
        // Register enchantment of compatible plugin and load configuration change.
        handleCompatibilityConfig()

        // Then handle plugin reload
        ecoEnchantCompatibility?.handleConfigReload()

    }

}
