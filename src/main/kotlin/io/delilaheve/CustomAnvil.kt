package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.alexcrea.cuanvil.command.EditConfigExecutor
import xyz.alexcrea.cuanvil.command.ReloadExecutor
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant
import xyz.alexcrea.cuanvil.listener.ChatEventListener
import xyz.alexcrea.cuanvil.update.Update_1_21
import xyz.alexcrea.cuanvil.util.Metrics
import java.io.File
import java.io.FileReader

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

        val pluginManager = Bukkit.getPluginManager();

        // Disable old plugin name if exist
        val potentialPlugin = Bukkit.getPluginManager().getPlugin("UnsafeEnchantsPlus")
        if (potentialPlugin != null) {
            Bukkit.getPluginManager().disablePlugin(potentialPlugin)
            logger.warning("An old version of this plugin was detected")
            logger.warning("Please note CustomAnvil is a more recent version of UnsafeEnchantsPlus")
        }

        // Load dependency
        DependencyManager.loadDependency()

        // Register enchantments
        CAEnchantmentRegistry.getInstance().registerStartupEnchantments()

        // Load chat listener
        chatListener = ChatEventListener()
        pluginManager.registerEvents(chatListener, this)

        // Load config
        val success = ConfigHolder.loadConfig()
        if (!success) return

        // temporary: handle 1.21 update
        Update_1_21.handleUpdate()

        // Handle custom enchant config
        DependencyManager.handleConfigChanges(this)

        // Load gui constants //TODO maybe something better later
        MainConfigGui.getInstance().init(DependencyManager.packetManager)
        GuiSharedConstant.loadConstants()

        // Load metrics
        Metrics(this, bstatsPluginId)

        // Add commands to reload the plugin
        prepareCommand()

        server.pluginManager.registerEvents(
            AnvilEventListener(DependencyManager.packetManager),
            this
        )
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
        // Load resource
        val yamlConfig = YamlConfiguration()
        try {
            val configReader = FileReader(file)
            yamlConfig.load(configReader)
        } catch (test: Exception) {
            if (hardFailSafe) {
                // This is important and may impact gameplay if it does not load.
                // Failsafe is to stop the plugin
                logger.severe("Resource $resourceName Could not be load or reload.")
                logger.severe("Disabling plugin.")
                Bukkit.getPluginManager().disablePlugin(this)
            } else {
                logger.warning("Resource $resourceName Could not be load or reload.")
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
