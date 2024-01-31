package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.alexcrea.group.ItemGroupManager

/**
 * Bukkit/Spigot/Paper plugin to alter enchantment max
 * levels and allow unsafe enchantment combinations
 */
class UnsafeEnchants : JavaPlugin() {

    companion object {
        // Permission string required to use the plugin's features
        const val unsafePermission = "ue.unsafe"
        // Permission string required to bypass illegal enchantment group
        const val unsafeBypassPermission = "ue.unsafe_all"
        // Item Grouping Configuration file name
        const val itemGroupingConfigName = "item_groups.yml"

        // Current plugin instance
        lateinit var instance: UnsafeEnchants
        // Current item grouping configuration instance
        lateinit var itemGroups: ItemGroupManager

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

        // Save default material grouping config
        saveResource(itemGroupingConfigName,false)
        // Load material grouping config
        val itemGroupConfig = YamlConfiguration()
        val configReader = this.getTextResource(itemGroupingConfigName)
        if(configReader == null){
            logger.severe("could no load item grouping configuration")
        }else{
            itemGroupConfig.load(configReader)
        }
        // Read material groups from config
        itemGroups = ItemGroupManager()
        itemGroups.prepareGroups(itemGroupConfig)

        server.pluginManager.registerEvents(
            AnvilEventListener(),
            this
        )
    }

}
