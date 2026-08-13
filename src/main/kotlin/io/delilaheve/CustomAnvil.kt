package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.alexcrea.cuanvil.api.event.CAConfigReadyEvent
import xyz.alexcrea.cuanvil.api.event.CAEnchantRegistryReadyEvent
import xyz.alexcrea.cuanvil.command.CustomAnvilCommand
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.MinecraftVersionUtil
import xyz.alexcrea.cuanvil.dependency.economy.EconomyManager
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant
import xyz.alexcrea.cuanvil.lang.Lang
import xyz.alexcrea.cuanvil.lang.Lang.translate
import xyz.alexcrea.cuanvil.listener.AnvilCloseListener
import xyz.alexcrea.cuanvil.listener.AnvilResultListener
import xyz.alexcrea.cuanvil.listener.ChatEventListener
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener
import xyz.alexcrea.cuanvil.update.ModrinthUpdateChecker
import xyz.alexcrea.cuanvil.update.PluginSetDefault
import xyz.alexcrea.cuanvil.update.UpdateHandler
import xyz.alexcrea.cuanvil.update.UpdateUtils
import xyz.alexcrea.cuanvil.util.MetricsUtil
import java.io.File
import java.io.FileReader
import java.util.logging.Level

/**
 * Bukkit/Spigot/Paper plugin to alter anvil feature
 */
open class CustomAnvil : JavaPlugin() {

    companion object {
        // pluginIDS
        private const val modrinthPluginID = "S75Ueiq9"

        // Permission string required to use the plugin's features
        const val affectedByPluginPermission = "ca.affected"

        // Permission string required to bypass enchantment conflicts test
        const val bypassFusePermission = "ca.bypass.fuse"

        // Permission string required to bypass enchantment conflicts test
        const val bypassLevelPermission = "ca.bypass.level"

        // Permission string required to reload the config
        const val commandReloadPermission = "ca.command.reload"

        // Permission string required to get diagnostic data
        const val diagnosticPermission = "ca.command.diagnostic"

        // Permission string required to edit the plugin's config
        const val editConfigPermission = "ca.config.edit"

        // Permission string required to edit the plugin's config
        const val giveEnchantmentPermission = "ca.command.enchantment"

        // Command Name to reload the config
        const val commandReloadName = "anvilconfigreload"

        // Config command name
        const val commandConfigName = "customanvilconfig"

        // Current plugin instance
        lateinit var instance: CustomAnvil

        // Chat message listener
        lateinit var chatListener: ChatEventListener

        var latestVer: String? = null

        // Debug
        val debugStorageQueue = ArrayDeque<String>()

        private fun addToLogQueue(message: String) {
            if(debugStorageQueue.size >= 200) {
                // Let not store infinite debug logs
                debugStorageQueue.removeFirst()
            }

            debugStorageQueue.addLast(message)
        }

        /**
         * Logging handler
         */
        @JvmStatic fun log(message: String) {
            if (ConfigOptions.debugLog) {
                if(ConfigOptions.showDebugLogInConsole)
                    instance.logger.info(message)
                addToLogQueue(message)
            }
        }

        /**
         * Vebose Logging handler
         */
        @JvmStatic fun verboseLog(message: String) {
            if (ConfigOptions.verboseDebugLog) {
                if(ConfigOptions.showDebugLogInConsole)
                    instance.logger.info(message)
                addToLogQueue(message)
            }
        }

        /**
         * Error Logging handler
         */
        @JvmStatic fun logError(message: String, throwable: Throwable? = null, track: Boolean = true) {
            instance.logger.log(Level.SEVERE, message, throwable)
            addToLogQueue(message)

            if(track && throwable != null) {
                MetricsUtil.trackError(throwable)
            }
        }
    }

    // stop plugin if we do not force a dirty start (true by default)
    // Return true if start was stopped
    private fun tryDirtyStart(): Boolean {
        if(ConfigHolder.DEFAULT_CONFIG == null) return false
        if(!ConfigHolder.DEFAULT_CONFIG.config.getBoolean("dirty_start", false)) {
            Bukkit.getPluginManager().disablePlugin(this)
            return true
        }
        return false
    }

    // stop plugin if we force a safe start (false by default)
    // Return true if start was stopped
    private fun trySafeStart(): Boolean {
        if(ConfigHolder.DEFAULT_CONFIG == null) return false
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
        // Load default configuration
        try {
            if(!ConfigHolder.loadDefaultConfig())
                throw RuntimeException("Error loading configuration file")
        } catch (e: Exception) {
            logError("error occurred loading default configuration", e)
            if(tryDirtyStart()) return
        }

        // Load language
        try {
            Lang.reload()
        } catch (e: Exception) {
            logError("error occurred loading language file", e)
            if(tryDirtyStart()) return
        }

        try {
            legacyCheck()
        } catch (e: Exception) {
            logError("error.load.legacy.failed".translate(), e)
            if(trySafeStart()) return
        }


        // Add commands
        try {
            CustomAnvilCommand(this)
        } catch (e: Exception) {
            logError("error.load.command-register".translate(), e)
            if(trySafeStart()) return
        }


        // Load dependency
        try {
            DependencyManager.loadDependency()
        } catch (e: Exception) {
            logError("error.load.compatibility".translate(), e)
            if(tryDirtyStart()) return
        }

        // Register listeners
        try {
            registerListeners()
        } catch (e: Exception) {
            logError("error.load.listeners".translate(), e)
            if(tryDirtyStart()) return
        }

        // Load metrics
        MetricsUtil.loadMetrics(this)

        // Load other thing later.
        // It is so other dependent plugins can implement there event listener before we fire them.
        DependencyManager.scheduler.scheduleGlobally(this) { loadEnchantmentSystemDirty() }
    }

    override fun onDisable() {
        MetricsUtil.shutdownMetrics()
    }

    private fun legacyCheck() {
        // Disable old plugin name if exist
        val potentialPlugin = Bukkit.getPluginManager().getPlugin("UnsafeEnchantsPlus")
        if (potentialPlugin != null) {
            Bukkit.getPluginManager().disablePlugin(potentialPlugin)
            logger.warning("warning.load.legacy.old-name.1".translate())
            logger.warning("warning.load.legacy.old-name.2".translate())
        }

        val isPaper = PlatformUtil.isPaper
        if(!isPaper) {
            logger.warning("warning.load.legacy.spigot.1".translate())
            logger.warning("warning.load.legacy.spigot.2".translate())
            if(MinecraftVersionUtil.isTooNewForSpigot) {
                logger.warning("warning.load.legacy.spigot-old.1".translate())
                logger.warning("warning.load.legacy.spigot-old.1".translate())
            }
        }

        val loader = if(isPaper) "paper" else "spigot"

        val version = description.version
        val featured = if(version.contains("dev")) null else true

        ModrinthUpdateChecker(modrinthPluginID, loader,
            UpdateUtils.currentMinecraftVersion().toString())
            .setFeatured(featured)
            .setOnError {
                logger.log(Level.WARNING, "error.load.update.check-fail".translate(), it)
            }
            .checkVersion { latestVer: String? ->
                CustomAnvil.latestVer = latestVer
                if(latestVer == null || version.contains(latestVer)) return@checkVersion

                logger.warning("warning.load.update.available".translate(Pair("version", latestVer)))
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

    private fun loadEnchantmentSystemDirty() {
        try {
            loadEnchantmentSystem()
        } catch (e: Exception) {
            logError("error.load.enchant-system".translate(), e)
            tryDirtyStart()
        }
    }

    private fun loadEnchantmentSystem(){
        // Register enchantments
        CAEnchantmentRegistry.getInstance().registerBukkit()
        DependencyManager.registerEnchantments()

        val enchantReadyEvent = CAEnchantRegistryReadyEvent()
        server.pluginManager.callEvent(enchantReadyEvent)

        // Load config
        if (!ConfigHolder.loadNonDefaultConfig()) {
            logError("error.load.non-default-config".translate())
            server.pluginManager.disablePlugin(this)
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

        // Prepare economy if possible
        EconomyManager.setupEconomy(this)

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
        } catch (_: Exception) {
            if (hardFailSafe) {
                // This is important and may impact gameplay if it does not load.
                // Failsafe is to stop the plugin
                logError("error.reload.resource.fail".translate(Pair("path", resourceFile.path)))
                logError("error.reload.resource.hard-fail".translate())
                Bukkit.getPluginManager().disablePlugin(this)
            } else {
                logError("error.reload.resource.fail".translate(Pair("path", resourceFile.path)))
            }
            return null
        }
        return yamlConfig
    }

}
