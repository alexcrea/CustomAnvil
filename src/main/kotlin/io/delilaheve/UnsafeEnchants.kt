package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.alexcrea.command.ReloadExecutor
import xyz.alexcrea.group.EnchantConflictManager
import xyz.alexcrea.group.ItemGroupManager
import xyz.alexcrea.util.Metrics
import xyz.alexcrea.util.MetricsUtil
import java.io.File
import java.io.FileReader

/**
 * Bukkit/Spigot/Paper plugin to alter enchantment max
 * levels and allow unsafe enchantment combinations
 */
class UnsafeEnchants : JavaPlugin() {

    companion object {
        // bstats plugin id
        private const val bstatsPluginId = 20923

        // Permission string required to use the plugin's features
        const val unsafePermission = "ue.unsafe"
        // Permission string required to bypass enchantment conflicts test
        const val bypassFusePermission = "ue.bypass.fuse"
        // Permission string required to bypass enchantment conflicts test
        const val bypassLevelPermission = "ue.bypass.level"
        // Permission string required to reload the config
        const val commandReloadPermission = "ue.command.reload"

        // Command Name to reload the config
        const val commandReloadName = "reloadunsafeenchants"

        // Item Grouping Configuration file name
        const val itemGroupingConfigFilePath = "item_groups.yml"
        // Conflict Configuration file name
        const val enchantConflicConfigFilePath = "enchant_conflict.yml"
        // Unit Repair Configuration file name
        const val unitRepairFilePath = "unit_repair_item.yml"

        // Current plugin instance
        lateinit var instance: UnsafeEnchants
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

        reloadAllConfigs()
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

    fun reloadAllConfigs(){
        saveDefaultConfig()

        // Load material grouping config
        val itemGroupConfig = reloadResource(itemGroupingConfigFilePath) ?: return
        // Read material groups from config
        val itemGroupsManager = ItemGroupManager()
        itemGroupsManager.prepareGroups(itemGroupConfig)

        // Load enchantment conflicts config
        val conflictConfig = reloadResource(enchantConflicConfigFilePath) ?: return
        // Read conflicts from config and material group manager
        val conflictManager = EnchantConflictManager()
        conflictManager.prepareConflicts(conflictConfig,itemGroupsManager)

        // Load unit repair config
        val unitRepairConfig = reloadResource(unitRepairFilePath) ?: return

        // Set the global variable
        UnsafeEnchants.conflictManager = conflictManager
        UnsafeEnchants.unitRepairConfig = unitRepairConfig

        // Test if default config
        MetricsUtil.testIfConfigIsDefault(config, itemGroupConfig, conflictConfig, unitRepairConfig)
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
