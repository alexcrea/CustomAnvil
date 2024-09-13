package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.alexcrea.cuanvil.api.event.CAConfigReadyEvent
import xyz.alexcrea.cuanvil.api.event.CAEnchantRegistryReadyEvent
import xyz.alexcrea.cuanvil.command.EditConfigExecutor
import xyz.alexcrea.cuanvil.command.ReloadExecutor
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant
import xyz.alexcrea.cuanvil.listener.ChatEventListener
import xyz.alexcrea.cuanvil.update.PluginSetDefault
import xyz.alexcrea.cuanvil.update.Update_1_21
import xyz.alexcrea.cuanvil.util.Metrics
import java.io.File
import java.io.FileReader
import java.util.logging.Level

/**
 * Bukkit/Spigot/Paper plugin to alter anvil feature
 */
class CustomAnvil : JavaPlugin() {

    companion object {
        // bstats plugin id
        private const val bstatsPluginId = 20923

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

        // Test command name
        const val commandTestName = "customanvilconfig"

        // Current plugin instance
        lateinit var instance: CustomAnvil

        // Chat message listener
        lateinit var chatListener: ChatEventListener

        /**
         * Logging handler
         */
        fun log(message: String) {
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

    /**
     * Setup plugin for use
     */
    override fun onEnable() {
        instance = this

        // Disable old plugin name if exist
        val potentialPlugin = Bukkit.getPluginManager().getPlugin("UnsafeEnchantsPlus")
        if (potentialPlugin != null) {
            Bukkit.getPluginManager().disablePlugin(potentialPlugin)
            logger.warning("An old version of this plugin was detected")
            logger.warning("Please note CustomAnvil is a more recent version of UnsafeEnchantsPlus")
        }

        // Add commands
        prepareCommand()

        // Load chat listener
        chatListener = ChatEventListener()
        server.pluginManager.registerEvents(chatListener, this)

        // Load default configuration
        if (!ConfigHolder.loadDefaultConfig()) {
            logger.log(Level.SEVERE,"could not load default config.")
            return
        }

        // Load dependency
        DependencyManager.loadDependency()

        // Register anvil events
        server.pluginManager.registerEvents(AnvilEventListener(DependencyManager.packetManager), this)

        // Load metrics
        Metrics(this, bstatsPluginId)

        // Load other thing later.
        // It is so other dependent plugins can implement there event listener before we fire them.
        DependencyManager.scheduler.scheduleGlobally(this, {loadEnchantmentSystem()})
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

        // temporary: handle 1.21 update
        Update_1_21.handleUpdate()

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

        command = getCommand(commandTestName)
        command?.setExecutor(EditConfigExecutor())
    }

}
