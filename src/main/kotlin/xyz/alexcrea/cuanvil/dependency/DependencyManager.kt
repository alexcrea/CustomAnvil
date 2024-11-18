package xyz.alexcrea.cuanvil.dependency

import io.delilaheve.CustomAnvil
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.gui.ExternGuiTester
import xyz.alexcrea.cuanvil.dependency.gui.GuiTesterSelector
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.PacketManagerSelector
import xyz.alexcrea.cuanvil.dependency.scheduler.BukkitScheduler
import xyz.alexcrea.cuanvil.dependency.scheduler.FoliaScheduler
import xyz.alexcrea.cuanvil.dependency.scheduler.TaskScheduler

object DependencyManager {

    var isFolia: Boolean = false
    lateinit var scheduler: TaskScheduler
    lateinit var packetManager: PacketManager
    var externGuiTester: ExternGuiTester? = null

    var enchantmentSquaredCompatibility: EnchantmentSquaredDependency? = null
    var ecoEnchantCompatibility: EcoEnchantDependency? = null
    var excellentEnchantsCompatibility: ExcellentEnchantsDependency? = null

    var disenchantmentCompatibility: DisenchantmentDependency? = null

    fun loadDependency(){
        val pluginManager = Bukkit.getPluginManager()

        // Bukkit or Paper scheduler ?
        isFolia = testIsFolia()
        scheduler = if(isFolia) {
            CustomAnvil.instance.logger.info("Folia detected... Custom Anvil Folia support is experimental. issues are more likely to happens.")

            FoliaScheduler()
        } else BukkitScheduler()

        // Packet Manager
        val forceProtocolib = ConfigHolder.DEFAULT_CONFIG.config.getBoolean("force_protocolib", false)
        packetManager = PacketManagerSelector.selectPacketManager(forceProtocolib)
        externGuiTester = GuiTesterSelector.selectGuiTester

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

        // Excellent Enchants dependency
        if(pluginManager.isPluginEnabled("ExcellentEnchants")){
            excellentEnchantsCompatibility = ExcellentEnchantsDependency()
            excellentEnchantsCompatibility!!.redirectListeners()
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
        excellentEnchantsCompatibility?.registerEnchantments()

    }

    fun handleConfigReload(){
        // Register enchantment of compatible plugin and load configuration change.
        handleCompatibilityConfig()

        // Then handle plugin reload
        ecoEnchantCompatibility?.handleConfigReload()

    }

    fun tryEventPreAnvilBypass(event: PrepareAnvilEvent, player: HumanEntity): Boolean {
        var bypass = false

        // Test if disenchantment used special prepare anvil
        if(disenchantmentCompatibility?.testPrepareAnvil(event, player) == true) bypass = true

        // Test excellent enchantments used special prepare anvil
        if(!bypass && (excellentEnchantsCompatibility?.testPrepareAnvil(event) == true)) bypass = true

        // Test if the inventory is a gui(version specific)
        if(!bypass && (externGuiTester?.testIfGui(event.view) == true)) bypass = true

        return bypass
    }

    fun treatAnvilResult(event: PrepareAnvilEvent, result: ItemStack) {
        excellentEnchantsCompatibility?.treatAnvilResult(event, result)
    }

    fun tryClickAnvilResultBypass(event: InventoryClickEvent, inventory: AnvilInventory): Boolean {
        var bypass = false

        // Test if disenchantment used special event click
        if(disenchantmentCompatibility?.testAnvilResult(event, inventory) == true) bypass = true

        // Test if disenchantment used special event click
        if(!bypass && (excellentEnchantsCompatibility?.testAnvilResult(event) == true)) bypass = true

        // Test if the inventory is a gui(version specific)
        if(!bypass && (externGuiTester?.testIfGui(event.view) == true)) bypass = true

        return bypass
    }


    private fun testIsFolia(): Boolean {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }

}
