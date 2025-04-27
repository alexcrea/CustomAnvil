package xyz.alexcrea.cuanvil.dependency

import io.delilaheve.CustomAnvil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.datapack.DataPackDependency
import xyz.alexcrea.cuanvil.dependency.gui.ExternGuiTester
import xyz.alexcrea.cuanvil.dependency.gui.GuiTesterSelector
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.PacketManagerSelector
import xyz.alexcrea.cuanvil.dependency.scheduler.BukkitScheduler
import xyz.alexcrea.cuanvil.dependency.scheduler.FoliaScheduler
import xyz.alexcrea.cuanvil.dependency.scheduler.TaskScheduler
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_OUTPUT_SLOT
import java.util.logging.Level

object DependencyManager {

    var isFolia: Boolean = false
    lateinit var scheduler: TaskScheduler
    lateinit var packetManager: PacketManager
    var externGuiTester: ExternGuiTester? = null

    var enchantmentSquaredCompatibility: EnchantmentSquaredDependency? = null
    var ecoEnchantCompatibility: EcoEnchantDependency? = null
    var excellentEnchantsCompatibility: ExcellentEnchantsDependency? = null

    var disenchantmentCompatibility: DisenchantmentDependency? = null
    var havenBagsCompatibility: HavenBagsDependency? = null

    fun loadDependency() {
        val pluginManager = Bukkit.getPluginManager()

        // Bukkit or Paper scheduler ?
        isFolia = testIsFolia()
        scheduler = if (isFolia) {
            CustomAnvil.instance.logger.info("Folia detected... Custom Anvil Folia support is experimental. issues are more likely to happens.")

            FoliaScheduler()
        } else BukkitScheduler()

        // Packet Manager
        val forceProtocolib = ConfigHolder.DEFAULT_CONFIG.config.getBoolean("force_protocolib", false)
        packetManager = PacketManagerSelector.selectPacketManager(forceProtocolib)
        externGuiTester = GuiTesterSelector.selectGuiTester

        // Enchantment Squared dependency
        if (pluginManager.isPluginEnabled("EnchantsSquared")) {
            enchantmentSquaredCompatibility = EnchantmentSquaredDependency(pluginManager.getPlugin("EnchantsSquared")!!)
            enchantmentSquaredCompatibility!!.disableAnvilListener()
        }

        // EcoEnchants dependency
        if (pluginManager.isPluginEnabled("EcoEnchants")) {
            ecoEnchantCompatibility = EcoEnchantDependency(pluginManager.getPlugin("EcoEnchants")!!)
            ecoEnchantCompatibility!!.disableAnvilListener()
        }

        // Excellent Enchants dependency
        if (pluginManager.isPluginEnabled("ExcellentEnchants")) {
            excellentEnchantsCompatibility = ExcellentEnchantsDependency()
            excellentEnchantsCompatibility!!.redirectListeners()
        }

        // Disenchantment dependency
        if (pluginManager.isPluginEnabled("Disenchantment")) {
            disenchantmentCompatibility = DisenchantmentDependency()
            disenchantmentCompatibility!!.redirectListeners()
        }

        // HavenBags dependency
        if (pluginManager.isPluginEnabled("HavenBags")) {
            havenBagsCompatibility = HavenBagsDependency()
            havenBagsCompatibility!!.redirectListeners()
        }
    }

    fun handleCompatibilityConfig() {
        enchantmentSquaredCompatibility?.registerPluginConfiguration()

        // datapacks
        DataPackDependency.handleDatapackConfigs()
    }

    fun registerEnchantments() {
        enchantmentSquaredCompatibility?.registerEnchantments()
        ecoEnchantCompatibility?.registerEnchantments()
        excellentEnchantsCompatibility?.registerEnchantments()

    }

    fun handleConfigReload() {
        // Register enchantment of compatible plugin and load configuration change.
        handleCompatibilityConfig()

        // Then handle plugin reload
        ecoEnchantCompatibility?.handleConfigReload()
    }

    // Return true if should bypass (either by a dependency or error)
    fun tryEventPreAnvilBypass(event: PrepareAnvilEvent, player: HumanEntity): Boolean {
        try {
            return unsafeTryEventPreAnvilBypass(event, player)
        } catch (e: Exception) {
            CustomAnvil.instance.logger.log(
                Level.SEVERE,
                "Error while trying to handle custom anvil supported plugin: ",
                e
            )

            // Just in case to avoid illegal items
            event.inventory.setItem(ANVIL_OUTPUT_SLOT, null)

            // Finally, warn the player, maybe a lot of time but better warn than do nothing
            event.view.player.sendMessage(ChatColor.RED.toString() + "Error while handling the anvil.")
            return true
        }
    }

    private fun unsafeTryEventPreAnvilBypass(event: PrepareAnvilEvent, player: HumanEntity): Boolean {
        var bypass = false

        // Test if disenchantment used prepare anvil
        if (disenchantmentCompatibility?.testPrepareAnvil(event, player) == true) bypass = true

        // Test heaven bags used prepare anvil
        if (!bypass && (havenBagsCompatibility?.testPrepareAnvil(event, player) == true)) bypass = true

        // Test excellent enchantments used prepare anvil
        if (!bypass && (excellentEnchantsCompatibility?.testPrepareAnvil(event) == true)) bypass = true

        // Test if the inventory is a gui(version specific)
        if (!bypass && (externGuiTester?.testIfGui(event.view) == true)) bypass = true


        return bypass
    }

    // Return true only if error occurred (and so should bypass rest)
    fun tryTreatAnvilResult(event: PrepareAnvilEvent, result: ItemStack): Boolean {
        try {
            unsafeTryTreatAnvilResult(event, result)
            return false
        } catch (e: Exception) {
            CustomAnvil.instance.logger.log(
                Level.SEVERE,
                "Error while trying to handle custom anvil supported plugin: ",
                e
            )

            // Just in case to avoid illegal items
            event.inventory.setItem(ANVIL_OUTPUT_SLOT, null)

            // Finally, warn the player, maybe a lot of time but better warn than do nothing
            event.view.player.sendMessage(ChatColor.RED.toString() + "Error while handling the anvil.")
            return true
        }
    }

    private fun unsafeTryTreatAnvilResult(event: PrepareAnvilEvent, result: ItemStack) {
        excellentEnchantsCompatibility?.treatAnvilResult(event, result)
    }

    // Return true if should bypass (either by a dependency or error)
    fun tryClickAnvilResultBypass(event: InventoryClickEvent, inventory: AnvilInventory): Boolean {
        try {
            return unsafeTryClickAnvilResultBypass(event, inventory)
        } catch (e: Exception) {
            CustomAnvil.instance.logger.log(
                Level.SEVERE,
                "Error while trying to handle custom anvil supported plugin: ",
                e
            )

            // Just in case to avoid illegal items
            event.inventory.setItem(ANVIL_OUTPUT_SLOT, null)

            // Finally, warn the player, maybe a lot of time but better warn than do nothing
            event.whoClicked.sendMessage(ChatColor.RED.toString() + "Error while handling the anvil.")
            return true
        }
    }

    private fun unsafeTryClickAnvilResultBypass(event: InventoryClickEvent, inventory: AnvilInventory): Boolean {
        var bypass = false

        // Test if disenchantment used event click
        if (disenchantmentCompatibility?.testAnvilResult(event, inventory) == true) bypass = true

        // Test if haven bag used event click
        if (!bypass && (havenBagsCompatibility?.testAnvilResult(event, inventory) == true)) bypass = true

        // Test if disenchantment used event click
        if (!bypass && (excellentEnchantsCompatibility?.testAnvilResult(event) == true)) bypass = true

        // Test if the inventory is a gui(version specific)
        if (!bypass && (externGuiTester?.testIfGui(event.view) == true)) bypass = true

        return bypass
    }

    fun stripLore(item: ItemStack): ArrayList<String> {
        val lore = ArrayList<String>()
        val dummy = item.clone()

        enchantmentSquaredCompatibility?.stripLore(dummy)

        val itemLore = dummy.itemMeta!!.lore
        if (itemLore != null) lore.addAll(itemLore)

        return lore
    }

    fun updateLore(item: ItemStack) {
        enchantmentSquaredCompatibility?.updateLore(item)
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
