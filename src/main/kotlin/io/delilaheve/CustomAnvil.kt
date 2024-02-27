package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.alexcrea.command.ReloadExecutor
import xyz.alexcrea.group.EnchantConflictManager
import xyz.alexcrea.group.ItemGroupManager
import xyz.alexcrea.cuanvil.util.Metrics
import xyz.alexcrea.util.MetricsUtil
import java.io.File
import java.io.FileReader

/**
 * Bukkit/Spigot/Paper plugin to alter enchantment max
 * levels and allow unsafe enchantment combinations
 */
class CustomAnvil : JavaPlugin() {

    companion object {
        // bstats plugin id
        private const val bstatsPluginId = 20923

        // Permission string required to use the plugin's features
        const val unsafePermission = "ca.unsafe"
        // Permission string required to bypass enchantment conflicts test
        const val bypassFusePermission = "ca.bypass.fuse"
        // Permission string required to bypass enchantment conflicts test
        const val bypassLevelPermission = "ca.bypass.level"
        // Permission string required to reload the config
        const val commandReloadPermission = "ca.command.reload"

        // Command Name to reload the config
        const val commandReloadName = "anvilconfigreload"

        // Item Grouping Configuration file name
        const val itemGroupingConfigFilePath = "item_groups.yml"
        // Conflict Configuration file name
        const val enchantConflicConfigFilePath = "enchant_conflict.yml"
        // Unit Repair Configuration file name
        const val unitRepairFilePath = "unit_repair_item.yml"

        // Current plugin instance
        lateinit var instance: CustomAnvil
        // Current item grouping configuration instance
        lateinit var conflictManager: EnchantConflictManager

        // Configuration for unit repair
        lateinit var unitRepairConfig: YamlConfiguration

        /**
         * Logging handler
         */
        fun log(message: String) {
            if (ConfigOptions.debugLog) {
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
        if(potentialPlugin != null){
            Bukkit.getPluginManager().disablePlugin(potentialPlugin)
            logger.warning("An old version of this plugin was detected")
            logger.warning("Please note CustomAnvil is a more recent version of UnsafeEnchantsPlus")
        }

        val success = reloadAllConfigs(true)
        if(!success) return

        // Load metrics
        val metric = Metrics(this, bstatsPluginId)
        MetricsUtil.addCustomMetric(metric)

        // Add command to reload the plugin
        val command = getCommand(commandReloadName)
        command?.setExecutor(ReloadExecutor())

        server.pluginManager.registerEvents(
            AnvilEventListener(),
            this
        )
    }

    fun reloadAllConfigs(hardFailSafe: Boolean): Boolean{
        saveDefaultConfig()
        reloadConfig()

        // Load material grouping config
        val itemGroupConfig = reloadResource(itemGroupingConfigFilePath, hardFailSafe) ?: return false
        // Read material groups from config
        val itemGroupsManager = ItemGroupManager()
        itemGroupsManager.prepareGroups(itemGroupConfig)

        // Load enchantment conflicts config
        val conflictConfig = reloadResource(enchantConflicConfigFilePath, hardFailSafe) ?: return false
        // Read conflicts from config and material group manager
        val conflictManager = EnchantConflictManager()
        conflictManager.prepareConflicts(conflictConfig,itemGroupsManager)

        // Load unit repair config
        val unitRepairConfig = reloadResource(unitRepairFilePath, hardFailSafe) ?: return false

        // Set the global variable
        CustomAnvil.conflictManager = conflictManager
        CustomAnvil.unitRepairConfig = unitRepairConfig

        // Test if is default config
        MetricsUtil.testIfConfigIsDefault(config, itemGroupConfig, conflictConfig, unitRepairConfig)
        return true
    }

    private fun reloadResource(resourceName: String,
                               hardFailSafe:Boolean = true): YamlConfiguration?{
        // Save default resource
        val file = File(dataFolder,resourceName)
        if(!file.exists()){
            saveResource(resourceName,false)
        }
        // Load resource
        val yamlConfig = YamlConfiguration()
        try {
            val configReader = FileReader(file)
            yamlConfig.load(configReader)
        } catch (test: Exception){
            if(hardFailSafe){
                // This is important and may impact gameplay if it does not load.
                // Failsafe is to stop the plugin
                logger.severe("Resource $resourceName Could not be load or reload.")
                logger.severe("Disabling plugin.")
                Bukkit.getPluginManager().disablePlugin(this)
            }else{
                logger.warning("Resource $resourceName Could not be load or reload.")
            }
            return null
        }
        return yamlConfig
    }

}
