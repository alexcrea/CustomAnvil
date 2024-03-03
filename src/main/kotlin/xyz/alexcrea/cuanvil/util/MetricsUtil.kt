package xyz.alexcrea.cuanvil.util

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import org.bukkit.configuration.ConfigurationSection

object MetricsUtil {

    private const val baseConfigHash = -1592940914
    private const val enchantLimitsConfigHash = 781312397
    private const val enchantValuesConfigHash = 1072574774
    private const val enchantConflictConfigHash = 1406650190
    private const val itemGroupsConfigHash = -1014133828
    private const val unitRepairItemConfigHash = 536871958
    private const val baseConfigPieName = "isDefaultBaseConfig"
    private const val enchantLimitsConfigPieName = "isDefaultEnchantLimitsConfig"
    private const val enchantValuesConfigPieName = "isDefaultEnchantValuesConfig"
    private const val enchantConflictConfigPieName = "isDefaultEnchantConflictConfig"
    private const val itemGroupsConfigPieName = "isDefaultItemGroupsConfig"
    private const val unitRepairItemConfigPieName = "isDefaultUnitRepairItemConfig"
    private var isDefaultBaseConfig = true
    private var isDefaultEnchantLimitsConfig = true
    private var isDefaultEnchantValuesConfig = true
    private var isDefaultEnchantConflictConfig = true
    private var isDefaultItemGroupsConfig = true
    private var isDefaultUnitRepairItemConfig = true

    /**
     * Get hash of a key, value a pair of a configuration section
     */
    private fun getHashFromKey(section: ConfigurationSection, key: String): Int {
        // Key is assumend to exist
        val resultHash: Int
        if(section.isConfigurationSection(key)){
            val sectionResult = getConfigurationHash(section.getConfigurationSection(key)!!)
            resultHash = key.hashCode() xor sectionResult
        }else{
            resultHash = key.hashCode() xor section.getString(key).hashCode()
        }
        return resultHash.hashCode()
    }

    /**
     * Get hash of a configuration section
     */
    private fun getConfigurationHash(section: ConfigurationSection): Int {
        var resultHash = 0
        for (key in section.getKeys(false)) {
            resultHash = resultHash xor getHashFromKey(section,key)
        }
        return resultHash
    }

    /**
     * Get hash value of the default config
     */
    private fun testBaseConfig(defaultConfig: ConfigurationSection): Int{
        var result = 0
        for (key in ConfigOptions.getBasicConfigKeys()) {
            result = result xor getHashFromKey(defaultConfig,key)
        }
        return result
    }

    /**
     * Test if the used configuration is the default config
     */
    fun testIfConfigIsDefault(defaultConfig: ConfigurationSection,
                            enchantConflictConfig: ConfigurationSection,
                            itemGroupsConfig: ConfigurationSection,
                            unitRepairItemConfig: ConfigurationSection){
        // Calculate hash of config
        val baseConfig = testBaseConfig(defaultConfig)
        val limitEnchantConfig = getHashFromKey(defaultConfig, ConfigOptions.ENCHANT_LIMIT_ROOT)
        val enchantValueConfig = getHashFromKey(defaultConfig, ConfigOptions.ENCHANT_VALUES_ROOT)
        val enchantConflictConfig2 = getConfigurationHash(enchantConflictConfig)
        val itemGroupConfig = getConfigurationHash(itemGroupsConfig)
        val unitRepairConfig = getConfigurationHash(unitRepairItemConfig)
        // Test if default
        isDefaultBaseConfig = baseConfigHash == baseConfig
        isDefaultEnchantLimitsConfig = enchantLimitsConfigHash == limitEnchantConfig
        isDefaultEnchantValuesConfig = enchantValuesConfigHash  == enchantValueConfig
        isDefaultEnchantConflictConfig = enchantConflictConfigHash == enchantConflictConfig2
        isDefaultItemGroupsConfig = itemGroupsConfigHash == itemGroupConfig
        isDefaultUnitRepairItemConfig = unitRepairItemConfigHash == unitRepairConfig
        // If not default and debug flag active, print the hash.
        if(ConfigOptions.debugLog){
            if(!isDefaultBaseConfig){CustomAnvil.log("baseConfig: $baseConfig")}
            if(!isDefaultEnchantLimitsConfig){CustomAnvil.log("limitEnchantConfig: $limitEnchantConfig")}
            if(!isDefaultEnchantValuesConfig){CustomAnvil.log("enchantValueConfig: $enchantValueConfig")}
            if(!isDefaultEnchantConflictConfig){CustomAnvil.log("enchantConflictConfig: $enchantConflictConfig")}
            if(!isDefaultItemGroupsConfig){CustomAnvil.log("itemGroupConfig: $itemGroupConfig")}
            if(!isDefaultUnitRepairItemConfig){CustomAnvil.log("unitRepairConfig: $unitRepairConfig")}
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

    }
}