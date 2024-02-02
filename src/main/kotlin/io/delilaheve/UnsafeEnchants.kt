package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.alexcrea.group.EnchantConflictManager
import xyz.alexcrea.group.ItemGroupManager
import java.io.File
import java.io.FileReader

/**
 * Bukkit/Spigot/Paper plugin to alter enchantment max
 * levels and allow unsafe enchantment combinations
 */
class UnsafeEnchants : JavaPlugin() {

    companion object {
        // Permission string required to use the plugin's features
        const val unsafePermission = "ue.unsafe"
        // Permission string required to bypass enchantment conflicts test
        const val unsafeBypassPermission = "ue.bypass.fuse"
        // Item Grouping Configuration file name
        const val itemGroupingConfigName = "item_groups.yml"
        // Conflict Configuration file name
        const val enchantConflicConfigName = "enchant_conflict.yml"

        // Current plugin instance
        lateinit var instance: UnsafeEnchants
        // Current item grouping configuration instance
        lateinit var conflictManager: EnchantConflictManager

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
        saveDefaultConfig()

        // Load material grouping config
        val itemGroupConfig = reloadResource(itemGroupingConfigName) ?: return
        // Read material groups from config
        val itemGroupsManager = ItemGroupManager()
        itemGroupsManager.prepareGroups(itemGroupConfig)

        // Load enchantment conflicts config
        val conflictConfig = reloadResource(enchantConflicConfigName) ?: return
        // Read conflicts from config and material group manager
        conflictManager = EnchantConflictManager()
        conflictManager.prepareConflicts(conflictConfig,itemGroupsManager)

        server.pluginManager.registerEvents(
            AnvilEventListener(),
            this
        )

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
