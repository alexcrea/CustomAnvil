package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.alexcrea.cuanvil.api.event.CAConfigReadyEvent
import xyz.alexcrea.cuanvil.api.event.CAEnchantRegistryReadyEvent
import xyz.alexcrea.cuanvil.command.CustomAnvilCmd
import xyz.alexcrea.cuanvil.command.EditConfigExecutor
import xyz.alexcrea.cuanvil.command.ReloadExecutor
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant
import xyz.alexcrea.cuanvil.listener.AnvilCloseListener
import xyz.alexcrea.cuanvil.listener.AnvilResultListener
import xyz.alexcrea.cuanvil.listener.ChatEventListener
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener
import xyz.alexcrea.cuanvil.update.ModrinthUpdateChecker
import xyz.alexcrea.cuanvil.update.PluginSetDefault
import xyz.alexcrea.cuanvil.update.UpdateHandler
import xyz.alexcrea.cuanvil.util.Metrics
import java.io.File
import java.io.FileReader
import java.util.logging.Level

/**
 * Bukkit/Spigot/Paper plugin to alter anvil feature
 */
open class CustomAnvil : JavaPlugin() {

    companion object {
        // pluginIDS
        private const val bstatsPluginId = 20923
        private const val modrinthPluginID = "S75Ueiq9"

        // Permission string required to use the plugin's features
        const val affectedByPluginPermission = "ca.affected"

        // Permission string required to bypass enchantment conflicts test
        const val bypassFusePermission = "ca.bypass.fuse"

        // Permission string required to bypass enchantment conflicts test
        const val bypassLevelPermission = "ca.bypass.level"

        // Permission string required to reload the config
        const val commandReloadPermission = "ca.command.reload"

        // Permission string required to edit the plugin's config
        const val editConfigPermission = "ca.config.edit"

        // Command Name to reload the config
        const val commandReloadName = "anvilconfigreload"

        // Config command name
        const val commandConfigName = "customanvilconfig"

        // Current plugin instance
        lateinit var instance: CustomAnvil

        // Chat message listener
        lateinit var chatListener: ChatEventListener

        /**
         * Logging handler
         */
        @JvmStatic fun log(message: String) {
            if (ConfigOptions.debugLog) {
                instance.logger.info(message)
            }
        }

        /**
         * Vebose Logging handler
         */
        fun verboseLog(message: String) {
            if (ConfigOptions.verboseDebugLog) {
                instance.logger.info(message)
            }
        }

    }

    // stop plugin if we do not force a dirty start (true by default)
    // Return true if start was stopped
    private fun tryDirtyStart(): Boolean {
        if(!ConfigHolder.DEFAULT_CONFIG.config.getBoolean("dirty_start", false)) {
            Bukkit.getPluginManager().disablePlugin(this)
            return true
        }
        return false
    }

    // stop plugin if we force a safe start (false by default)
    // Return true if start was stopped
    private fun trySafeStart(): Boolean {
        if(ConfigHolder.DEFAULT_CONFIG.config.getBoolean("safe_start", false)) {
            Bukkit.getPluginManager().disablePlugin(this)
            return true
        }
        return false
    }

    /**
     * Setup plugin for use
     */
    override fun onEnable() {
        instance = this
        try {
            legacyCheck()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "error trying to check for legacy system" , e)
            if(trySafeStart()) return
        }

        // Add commands
        try {
            prepareCommand()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "error trying to register commands" , e)
            if(trySafeStart()) return
        }

        // Load default configuration
        try {
            if(!ConfigHolder.loadDefaultConfig())
                throw RuntimeException("Error loading configuration file")
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "error occurred loading default configuration", e)
            if(tryDirtyStart()) return
        }

        // Load dependency
        try {
            DependencyManager.loadDependency()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "error loading dependency compatibility", e)
            if(tryDirtyStart()) return
        }

        // Register listeners
        try {
            registerListeners()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "error registering listeners", e)
            if(tryDirtyStart()) return
        }

        // Load metrics
        try {
            Metrics(this, bstatsPluginId)
        } catch (_: Exception) {}

        // Load other thing later.
        // It is so other dependent plugins can implement there event listener before we fire them.
        DependencyManager.scheduler.scheduleGlobally(this) { loadEnchantmentSystemDirty() }
    }

    private fun loadEnchantmentSystemDirty() {
        try {
            loadEnchantmentSystem()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "error initializing enchantment ssytem", e)
            tryDirtyStart()
        }
    }

    private fun legacyCheck() {
        // Disable old plugin name if exist
        val potentialPlugin = Bukkit.getPluginManager().getPlugin("UnsafeEnchantsPlus")
        if (potentialPlugin != null) {
            Bukkit.getPluginManager().disablePlugin(potentialPlugin)
            logger.warning("An old version of this plugin was detected")
            logger.warning("Please note CustomAnvil is a more recent version of UnsafeEnchantsPlus")
        }

        val isPaper = PlatformUtil.isPaper
        if(!isPaper) {
            logger.warning("It seems you are using spigot")
            logger.warning("Please take notice that spigot is less supported than paper and derivatives")
        }

        val loader = if(isPaper) "paper" else "spigot"

        val version = description.version
        val featured = if(version.contains("dev")) null else true

        ModrinthUpdateChecker(modrinthPluginID, loader, null)
            .setFeatured(featured)
            .setOnError { logger.log(Level.WARNING, "error trying to fetch latest update", it) }
            .checkVersion { latestver: String? ->
                if(latestver == null || version.contains(latestver)) return@checkVersion

                logger.warning("An update may be available: $latestver")
            }
    }

    private fun registerListeners() {
        // Register chat listener
        chatListener = ChatEventListener()
        server.pluginManager.registerEvents(chatListener, this)

        // Register anvil events
        server.pluginManager.registerEvents(PrepareAnvilListener(), this)
        server.pluginManager.registerEvents(AnvilResultListener(), this)
        server.pluginManager.registerEvents(AnvilCloseListener(DependencyManager.packetManager), this)
    }

    private fun loadEnchantmentSystem(){
        // Register enchantments
        CAEnchantmentRegistry.getInstance().registerBukkit()
        DependencyManager.registerEnchantments()

        val enchantReadyEvent = CAEnchantRegistryReadyEvent()
        server.pluginManager.callEvent(enchantReadyEvent)

        // Load config
        if (!ConfigHolder.loadNonDefaultConfig()) {
            logger.log(Level.SEVERE,"could not load non default config.")
            return
        }

        // Handle minecraft and plugin updates
        UpdateHandler.handleUpdates()

        // Register enchantment of compatible plugin and load configuration change.
        DependencyManager.handleCompatibilityConfig()

        // Call config event
        val configReadyEvent = CAConfigReadyEvent()
        server.pluginManager.callEvent(configReadyEvent)

        // Load gui constants //TODO maybe something better later
        MainConfigGui.getInstance().init(DependencyManager.packetManager)
        GuiSharedConstant.loadConstants()

        // Finally, re add default we may be missing
        PluginSetDefault.reAddMissingDefault()

    }

    fun reloadResource(
        resourceName: String,
        hardFailSafe: Boolean = true
    ): YamlConfiguration? {
        // Save default resource
        val file = File(dataFolder, resourceName)
        if (!file.exists()) {
            saveResource(resourceName, false)
        }

        return reloadResource(file, hardFailSafe)
    }

    // Unlike above function. this function will not clone default from jar.
    fun reloadResource(
        resourceFile: File,
        hardFailSafe: Boolean = true
    ): YamlConfiguration? {
        // Test if file exist
        if (!resourceFile.exists()) {
            return null
        }

        // Load resource
        val yamlConfig = YamlConfiguration()
        try {
            val configReader = FileReader(resourceFile)
            yamlConfig.load(configReader)
        } catch (test: Exception) {
            if (hardFailSafe) {
                // This is important and may impact gameplay if it does not load.
                // Failsafe is to stop the plugin
                logger.severe("Resource ${resourceFile.path} Could not be load or reload.")
                logger.severe("Disabling plugin.")
                Bukkit.getPluginManager().disablePlugin(this)
            } else {
                logger.warning("Resource ${resourceFile.path} Could not be load or reload.")
            }
            return null
        }
        return yamlConfig
    }

    fun prepareCommand() {
        var command = getCommand(commandReloadName)
        command?.setExecutor(ReloadExecutor())

        command = getCommand(commandConfigName)
        command?.setExecutor(EditConfigExecutor())

        CustomAnvilCmd(this)
    }

}
