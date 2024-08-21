package xyz.alexcrea.cuanvil.dependency

import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.PacketManagerSelector

object DependencyManager {

    lateinit var packetManager: PacketManager
    var enchantmentSquaredCompatibility: EnchantmentSquaredDependency? = null
    var ecoEnchantCompatibility: EcoEnchantDependency? = null
    var disenchantmentCompatibility: DisenchantmentDependency? = null

    fun loadDependency(){
        val pluginManager = Bukkit.getPluginManager()

        // Packet Manager
        val forceProtocolib = ConfigHolder.DEFAULT_CONFIG.config.getBoolean("force_protocolib", false)
        packetManager = PacketManagerSelector.selectPacketManager(forceProtocolib)

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

        // Disenchantment dependency
        if(pluginManager.isPluginEnabled("Disenchantment")){
            disenchantmentCompatibility = DisenchantmentDependency()
            disenchantmentCompatibility!!.redirectListeners()
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

    fun tryEventPreAnvilBypass(event: PrepareAnvilEvent): Boolean {
        var bypass = false

        if(disenchantmentCompatibility?.testPrepareAnvil(event) == true) bypass = true

        return bypass
    }

    fun tryClickAnvilResultBypass(event: InventoryClickEvent, inventory: AnvilInventory): Boolean {
        var bypass = false

        if(disenchantmentCompatibility?.testAnvilResult(event, inventory) == true) bypass = true

        return bypass
    }

}
