package xyz.alexcrea.cuanvil.dependency

import org.bukkit.Bukkit
import xyz.alexcrea.cuanvil.dependency.protocolib.NoProtocoLib
import xyz.alexcrea.cuanvil.dependency.protocolib.PacketManager
import xyz.alexcrea.cuanvil.dependency.protocolib.ProtocoLibWrapper

object DependencyManager {

    lateinit var packetManager: PacketManager
    var enchantmentSquaredCompatibility: EnchantmentSquaredDependency? = null;

    fun loadDependency(){
        val pluginManager = Bukkit.getPluginManager();

        // ProtocolLib dependency
        packetManager =
            if(pluginManager.isPluginEnabled("ProtocolLib")) ProtocoLibWrapper();
        else  NoProtocoLib();

        // Enchantment Squared dependency
        enchantmentSquaredCompatibility =
            if(pluginManager.isPluginEnabled("EnchantsSquared")) EnchantmentSquaredDependency(pluginManager.getPlugin("EnchantsSquared")!!)
        else null

    }

}
