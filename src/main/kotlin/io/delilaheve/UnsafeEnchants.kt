package io.delilaheve

import io.delilaheve.util.ConfigOptions
import org.bukkit.plugin.java.JavaPlugin

/**
 * Bukkit/Spigot/Paper plugin to alter enchantment max
 * levels and allow unsafe enchantment combinations
 */
class UnsafeEnchants : JavaPlugin() {

    companion object {
        // Permission string required to use the plugin's features
        const val unsafePermission = "ue.unsafe"
        // Current plugin instance
        lateinit var instance: UnsafeEnchants

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
        server.pluginManager.registerEvents(
            AnvilEventListener(),
            this
        )
    }

}
