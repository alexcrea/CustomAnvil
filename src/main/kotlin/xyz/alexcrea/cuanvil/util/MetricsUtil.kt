package xyz.alexcrea.cuanvil.util

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import org.bukkit.configuration.ConfigurationSection
import xyz.alexcrea.cuanvil.config.ConfigHolder

object MetricsUtil {

    private const val baseConfigHash = -1592940914
    private const val enchantLimitsConfigHash = -1014133828
    private const val enchantValuesConfigHash = 1072574774
    private const val enchantConflictConfigHash = 1406650190
    private const val itemGroupsConfigHash = 1406650190
    private const val unitRepairItemConfigHash = 536871958
    private const val customAnvilCraftConfigHash = 0
    private const val baseConfigPieName = "isDefaultBaseConfig"
    private const val enchantLimitsConfigPieName = "isDefaultEnchantLimitsConfig"
    private const val enchantValuesConfigPieName = "isDefaultEnchantValuesConfig"
    private const val enchantConflictConfigPieName = "isDefaultEnchantConflictConfig"
    private const val itemGroupsConfigPieName = "isDefaultItemGroupsConfig"
    private const val unitRepairItemConfigPieName = "isDefaultUnitRepairItemConfig"
    private const val customAnvilCraftConfigPieName = "isDefaultCustomAnvilCraftConfig"
    private var isDefaultBaseConfig = true
    private var isDefaultEnchantLimitsConfig = true
    private var isDefaultEnchantValuesConfig = true
    private var isDefaultEnchantConflictConfig = true
    private var isDefaultItemGroupsConfig = true
    private var isDefaultUnitRepairItemConfig = true
    private var isDefaultCustomAnvilCraftConfig = true

    /**
     * Get hash of a key, value a pair of a configuration section
     */
    private fun getHashFromKey(section: ConfigurationSection, key: String): Int {
        // Key is assumend to exist
        val resultHash = if (section.isConfigurationSection(key)) {
            val sectionResult = getConfigurationHash(section.getConfigurationSection(key)!!)
            key.hashCode() xor sectionResult
        } else {
            key.hashCode() xor section.getString(key).hashCode()
        }
        return resultHash.hashCode()
    }

    /**
     * Get hash of a configuration section
     */
    private fun getConfigurationHash(section: ConfigurationSection): Int {
        var resultHash = 0
        for (key in section.getKeys(false)) {
            resultHash = resultHash xor getHashFromKey(section, key)
        }
        return resultHash
    }

    /**
     * Get hash value of the default config
     */
    private fun testBaseConfig(defaultConfig: ConfigurationSection): Int {
        var result = 0
        for (key in ConfigOptions.getBasicConfigKeys()) {
            result = result xor getHashFromKey(defaultConfig, key)
        }
        return result
    }

    /**
     * Test if the used configuration is the default config
     */
    fun testIfConfigIsDefault() {
        // Calculate hash of config
        val baseConfig = testBaseConfig(ConfigHolder.DEFAULT_CONFIG.config)
        val limitEnchantConfig = getHashFromKey(ConfigHolder.DEFAULT_CONFIG.config, ConfigOptions.ENCHANT_LIMIT_ROOT)
        val enchantValueConfig = getHashFromKey(ConfigHolder.DEFAULT_CONFIG.config, ConfigOptions.ENCHANT_VALUES_ROOT)
        val enchantConflictConfig = getConfigurationHash(ConfigHolder.CONFLICT_HOLDER.config)
        val itemGroupConfig = getConfigurationHash(ConfigHolder.ITEM_GROUP_HOLDER.config)
        val unitRepairConfig = getConfigurationHash(ConfigHolder.UNIT_REPAIR_HOLDER.config)
        val customRecipeConfig = getConfigurationHash(ConfigHolder.CUSTOM_RECIPE_HOLDER.config)
        // Test if default
        isDefaultBaseConfig = baseConfigHash == baseConfig
        isDefaultEnchantLimitsConfig = enchantLimitsConfigHash == limitEnchantConfig
        isDefaultEnchantValuesConfig = enchantValuesConfigHash == enchantValueConfig
        isDefaultEnchantConflictConfig = enchantConflictConfigHash == enchantConflictConfig
        isDefaultItemGroupsConfig = itemGroupsConfigHash == itemGroupConfig
        isDefaultUnitRepairItemConfig = unitRepairItemConfigHash == unitRepairConfig
        isDefaultCustomAnvilCraftConfig = customAnvilCraftConfigHash == customRecipeConfig
        // If not default and debug flag active, print the hash.
        if (ConfigOptions.debugLog) {
            if (!isDefaultBaseConfig) {
                CustomAnvil.log("baseConfig: $baseConfig")
            }
            if (!isDefaultEnchantLimitsConfig) {
                CustomAnvil.log("limitEnchantConfig: $limitEnchantConfig")
            }
            if (!isDefaultEnchantValuesConfig) {
                CustomAnvil.log("enchantValueConfig: $enchantValueConfig")
            }
            if (!isDefaultEnchantConflictConfig) {
                CustomAnvil.log("enchantConflictConfig: $enchantConflictConfig")
            }
            if (!isDefaultItemGroupsConfig) {
                CustomAnvil.log("itemGroupConfig: $itemGroupConfig")
            }
            if (!isDefaultUnitRepairItemConfig) {
                CustomAnvil.log("unitRepairConfig: $unitRepairConfig")
            }
            if (!isDefaultCustomAnvilCraftConfig) {
                CustomAnvil.log("customRecipeConfig: $customRecipeConfig")
            }
        }

    }

    fun notifyChange(holder: ConfigHolder, path: String) {
        if (ConfigHolder.DEFAULT_CONFIG.equals(holder)) {
            if (path.startsWith(ConfigOptions.ENCHANT_LIMIT_ROOT + ".")) {
                isDefaultEnchantLimitsConfig = false
            } else if (path.startsWith(ConfigOptions.ENCHANT_VALUES_ROOT + ".")) {
                isDefaultEnchantValuesConfig = false
            } else {
                isDefaultBaseConfig = false
            }
        } else if (ConfigHolder.CONFLICT_HOLDER.equals(holder)) {
            isDefaultEnchantConflictConfig = false
        } else if (ConfigHolder.ITEM_GROUP_HOLDER.equals(holder)) {
            isDefaultItemGroupsConfig = false
        } else if (ConfigHolder.UNIT_REPAIR_HOLDER.equals(holder)) {
            isDefaultUnitRepairItemConfig = false
        }
    }

    fun addCustomMetric(metric: Metrics) {
        metric.addCustomChart(Metrics.SimplePie(baseConfigPieName) {
            isDefaultBaseConfig.toString()
        })
        metric.addCustomChart(Metrics.SimplePie(enchantLimitsConfigPieName) {
            isDefaultEnchantLimitsConfig.toString()
        })
        metric.addCustomChart(Metrics.SimplePie(enchantValuesConfigPieName) {
            isDefaultEnchantValuesConfig.toString()
        })
        metric.addCustomChart(Metrics.SimplePie(enchantConflictConfigPieName) {
            isDefaultEnchantConflictConfig.toString()
        })
        metric.addCustomChart(Metrics.SimplePie(itemGroupsConfigPieName) {
            isDefaultItemGroupsConfig.toString()
        })
        metric.addCustomChart(Metrics.SimplePie(unitRepairItemConfigPieName) {
            isDefaultUnitRepairItemConfig.toString()
        })
        metric.addCustomChart(Metrics.SimplePie(customAnvilCraftConfigPieName) {
            isDefaultCustomAnvilCraftConfig.toString()
        })

    }
}