package xyz.alexcrea.cuanvil.dependency

import io.delilaheve.CustomAnvil
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import xyz.alexcrea.cuanvil.anvil.AnvilCost
import xyz.alexcrea.cuanvil.anvil.AnvilUseType
import xyz.alexcrea.cuanvil.api.event.listener.CAClickResultBypassEvent
import xyz.alexcrea.cuanvil.api.event.listener.CAEarlyPreAnvilBypassEvent
import xyz.alexcrea.cuanvil.api.event.listener.CAPreAnvilBypassEvent
import xyz.alexcrea.cuanvil.api.event.listener.CATreatAnvilResultEvent
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.datapack.DataPackDependency
import xyz.alexcrea.cuanvil.dependency.gui.GenericExternGuiTester
import xyz.alexcrea.cuanvil.dependency.packet.PacketManager
import xyz.alexcrea.cuanvil.dependency.packet.PacketManagerSelector
import xyz.alexcrea.cuanvil.dependency.plugins.*
import xyz.alexcrea.cuanvil.dependency.scheduler.BukkitScheduler
import xyz.alexcrea.cuanvil.dependency.scheduler.FoliaScheduler
import xyz.alexcrea.cuanvil.dependency.scheduler.TaskScheduler
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.componentLore
import xyz.alexcrea.cuanvil.lang.MsgWarning
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_OUTPUT_SLOT
import xyz.alexcrea.cuanvil.util.MetricsUtil.trackError
import java.lang.IllegalStateException
import java.util.logging.Level

@Suppress("UnstableApiUsage")
object DependencyManager {

    lateinit var scheduler: TaskScheduler
    lateinit var packetManager: PacketManager
    var externGuiTester: GenericExternGuiTester = GenericExternGuiTester()

    var enchantmentSquaredCompatibility: EnchantmentSquaredDependency? = null
    var ecoEnchantCompatibility: EcoEnchantDependency? = null
    var hasEcoItem: Boolean = false
    var excellentEnchantsCompatibility: ExcellentEnchantsDependency? = null

    var disenchantmentCompatibility: DisenchantmentDependency? = null
    var havenBagsCompatibility: HavenBagsDependency? = null

    var axPlayerWarpsCompatibility: AxPlayerWarpsDependency? = null

    var itemsAdderCompatibility: ItemsAdderDependency? = null

    val genericDependencies = ArrayList<GenericPluginDependency>()

    fun loadDependency() {
        val pluginManager = Bukkit.getPluginManager()

        // Bukkit or Paper scheduler ?
        scheduler = if (PlatformUtil.isFolia) {
            CustomAnvil.instance.logger.info("Folia detected... Custom Anvil Folia support is experimental. issues are more likely to happens.")

            FoliaScheduler()
        } else BukkitScheduler()

        // Packet Manager
        val forceProtocolib = ConfigHolder.DEFAULT_CONFIG.config.getBoolean("force_protocolib", false)
        packetManager = PacketManagerSelector.selectPacketManager(forceProtocolib)

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
        // EcoItem check
        if (pluginManager.isPluginEnabled("EcoItems")) {
            hasEcoItem = true
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

        // AxPlayerWarps dependency
        if (pluginManager.isPluginEnabled("AxPlayerWarps")) {
            axPlayerWarpsCompatibility = AxPlayerWarpsDependency()
        }

        if (pluginManager.isPluginEnabled("ItemsAdder")) {
            val dependency = ItemsAdderDependency(pluginManager.getPlugin("ItemsAdder")!!)
            itemsAdderCompatibility = dependency
            genericDependencies.add(dependency)
        }

        // "Generic" dependencies
        if (pluginManager.isPluginEnabled("ToolStats"))
            genericDependencies.add(ToolStatsDependency(pluginManager.getPlugin("ToolStats")!!))

        if (pluginManager.isPluginEnabled("ItemsAdder"))
            genericDependencies.add(GenericPluginDependency(pluginManager.getPlugin("ItemsAdder")!!))

        if (pluginManager.isPluginEnabled("SuperEnchants")) {
            val compatibility = SuperEnchantDependency(pluginManager.getPlugin("SuperEnchants")!!)
            if (compatibility.registerEnchantments())
                genericDependencies.add(compatibility)
        }

        if (pluginManager.isPluginEnabled("EnchantedBook")) {
            genericDependencies.add(EnchantedBookDependency(pluginManager.getPlugin("EnchantedBook")!!))
        }

        for (dependency in genericDependencies)
            dependency.redirectListeners()

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

    private fun logException(target: CommandSender, e: Exception) {
        CustomAnvil.logError(
            "Error while trying to handle custom anvil supported plugin: ",
            e
        )
        trackError(e)

        // Finally, warn the player
        MsgWarning.DEPENDENCY_GENERIC_EXCEPTION.send(target)
    }

    private fun logExceptionAndClear(view: AnvilView, e: Exception) {
        // Just in case to avoid illegal items
        view.setItem(ANVIL_OUTPUT_SLOT, null)

        logException(view.player, e)
    }

    // Return true if should bypass (either by a dependency or error)
    // called before immutability test
    fun earlyTryEventPreAnvilBypass(event: PrepareAnvilEvent, player: HumanEntity): Boolean {
        try {
            return earlyUnsafeTryEventPreAnvilBypass(event, player)
        } catch (e: Exception) {
            logExceptionAndClear(event.view, e)
            return true
        }
    }

    private fun earlyUnsafeTryEventPreAnvilBypass(event: PrepareAnvilEvent, player: HumanEntity): Boolean {
        // Run the event
        val bypassEvent = CAEarlyPreAnvilBypassEvent(event)
        Bukkit.getPluginManager().callEvent(bypassEvent)

        var bypass = bypassEvent.isCancelled

        // Test if the inventory is a gui(version specific)
        if (!bypass && externGuiTester.testIfGui(event.view)) bypass = true

        // Test if in an ax player warp rating gui
        if (!bypass && (axPlayerWarpsCompatibility?.testIfGui(player) == true)) bypass = true

        return bypass
    }

    // Return true if should bypass (either by a dependency or error)
    fun tryEventPreAnvilBypass(event: PrepareAnvilEvent, player: Player): Boolean {
        try {
            return unsafeTryEventPreAnvilBypass(event, player)
        } catch (e: Exception) {
            logExceptionAndClear(event.view, e)
            return true
        }
    }

    private fun unsafeTryEventPreAnvilBypass(event: PrepareAnvilEvent, player: Player): Boolean {
        // Run the event
        val bypassEvent = CAPreAnvilBypassEvent(event)
        Bukkit.getPluginManager().callEvent(bypassEvent)

        var bypass = bypassEvent.isCancelled

        // Test if disenchantment used prepare anvil
        if (!bypass && (disenchantmentCompatibility?.testPrepareAnvil(event, player) == true)) bypass = true

        // Test heaven bags used prepare anvil
        if (!bypass && (havenBagsCompatibility?.testPrepareAnvil(event, player) == true)) bypass = true

        // Test excellent enchantments used prepare anvil
        if (!bypass && (excellentEnchantsCompatibility?.testPrepareAnvil(event) == true)) bypass = true

        for (genericDependency in genericDependencies) {
            if (!bypass && genericDependency.testPrepareAnvil(event)) bypass = true
        }

        return bypass
    }

    // Return null if there was an issue
    fun tryTreatAnvilResult(
        view: AnvilView,
        player: HumanEntity,
        result: ItemStack,
        useType: AnvilUseType,
        cost: AnvilCost
    ): ItemStack? {
        val treatEvent = CATreatAnvilResultEvent(view, useType, result, cost)
        try {
            unsafeTryTreatAnvilResult(treatEvent)
            return treatEvent.result
        } catch (e: Exception) {
            logExceptionAndClear(view, e)
            return null
        }
    }

    private fun unsafeTryTreatAnvilResult(event: CATreatAnvilResultEvent) {
        Bukkit.getPluginManager().callEvent(event)

        excellentEnchantsCompatibility?.treatAnvilResult(event)
    }

    // Return true if should bypass (either by a dependency or error)
    fun tryClickAnvilResultBypass(event: InventoryClickEvent, view: AnvilView): Boolean {
        try {
            return unsafeTryClickAnvilResultBypass(event, view)
        } catch (e: Exception) {
            logExceptionAndClear(view, e)
            return true
        }
    }

    private fun unsafeTryClickAnvilResultBypass(event: InventoryClickEvent, view: AnvilView): Boolean {
        // Run the event
        val bypassEvent = CAClickResultBypassEvent(event)
        Bukkit.getPluginManager().callEvent(bypassEvent)

        var bypass = bypassEvent.isCancelled

        // Test if disenchantment used event click
        if (!bypass && (disenchantmentCompatibility?.testAnvilResult(event, view) == true)) bypass = true

        // Test if haven bag used event click
        if (!bypass && (havenBagsCompatibility?.testAnvilResult(event, view) == true)) bypass = true

        // Test if disenchantment used event click
        if (!bypass && (excellentEnchantsCompatibility?.testAnvilResult(event) == true)) bypass = true

        for (genericDependency in genericDependencies) {
            if (!bypass && genericDependency.testAnvilResult(event)) bypass = true
        }

        // Test if the inventory is a gui(version specific)
        if (!bypass && externGuiTester.testIfGui(view)) bypass = true

        // Test if in an ax player warp rating gui
        if (!bypass && (axPlayerWarpsCompatibility?.testIfGui(view.player) == true)) bypass = true

        return bypass
    }

    // Clone item and use plugin specific clone if needed
    fun cloneItem(player: HumanEntity, item: ItemStack): ItemStack {
        try {
            return unsafeCloneItem(item)
        } catch (e: Exception) {
            logException(player, e)
            return item.clone()
        }
    }

    private fun unsafeCloneItem(item: ItemStack): ItemStack {
        val cloned = itemsAdderCompatibility?.tryClone(item)
        if (cloned != null) return cloned

        return item.clone()
    }

    fun stripLore(item: ItemStack): MutableList<Component?> {
        val dummy = item.clone()

        enchantmentSquaredCompatibility?.stripLore(dummy)

        val itemLore = dummy.itemMeta?.componentLore() ?: return ArrayList()

        val lore = ArrayList<Component?>()
        lore.addAll(itemLore)
        return lore
    }

    fun updateLore(item: ItemStack) {
        enchantmentSquaredCompatibility?.updateLore(item)
    }


    private val prepareAnvilConstructor =
        PrepareAnvilEvent::class.java.constructors.first()

    fun createFakeEvent(view: AnvilView, result: ItemStack?): PrepareAnvilEvent {
        val result = prepareAnvilConstructor.newInstance(view, result)
        if(result !is PrepareAnvilEvent)
            throw IllegalStateException("Could not create a PrepareAnvilEvent ?")

        return result
    }

}
