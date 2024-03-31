package io.delilaheve.util

import io.delilaheve.CustomAnvil
import io.delilaheve.util.EnchantmentUtil.enchantmentName
import org.bukkit.enchantments.Enchantment
import xyz.alexcrea.cuanvil.config.ConfigHolder

/**
 * Config option accessors
 */
object ConfigOptions {

    // Path for default enchantment limits
    private const val DEFAULT_LIMIT_PATH = "default_limit"

    // Path for limiting repair cost
    const val LIMIT_REPAIR_COST = "limit_repair_cost"

    // Path for repair value limit
    const val LIMIT_REPAIR_VALUE = "limit_repair_value"

    // Path for level cost on item repair
    const val ITEM_REPAIR_COST = "item_repair_cost"

    // Path for level cost on unit repair
    const val UNIT_REPAIR_COST = "unit_repair_cost"

    // Path for level cost on item renaming
    const val ITEM_RENAME_COST = "item_rename_cost"

    // Path for level cost on illegal enchantment on sacrifice
    const val SACRIFICE_ILLEGAL_COST = "sacrifice_illegal_enchant_cost"

    // Path for removing repair cost limits
    const val REMOVE_REPAIR_LIMIT = "remove_repair_limit"

    // Root path for enchantment limits
    const val ENCHANT_LIMIT_ROOT = "enchant_limits"

    // Root path for enchantment values
    const val ENCHANT_VALUES_ROOT = "enchant_values"

    // Keys for specific enchantment values
    private const val KEY_BOOK = "book"
    private const val KEY_ITEM = "item"

    // Debug logging toggle path
    private const val DEBUG_LOGGING = "debug_log"

    // Debug verbose logging toggle path
    private const val VERBOSE_DEBUG_LOGGING = "debug_log_verbose"

    // Default value for enchantment limits
    private const val DEFAULT_ENCHANT_LIMIT = 5

    // Default value for limiting repair cost
    const val DEFAULT_LIMIT_REPAIR = false

    // Default value for repair cost limit
    const val DEFAULT_LIMIT_REPAIR_VALUE = 39

    // Default value for level cost on item repair
    const val DEFAULT_ITEM_REPAIR_COST = 2

    // Default value for level cost per unit repair
    const val DEFAULT_UNIT_REPAIR_COST = 1

    // Default value for level cost on item renaming
    const val DEFAULT_ITEM_RENAME_COST = 1

    // Default value for level cost on illegal enchantment on sacrifice
    const val DEFAULT_SACRIFICE_ILLEGAL_COST = 1

    // Valid range for repair cost limit
    @JvmField
    val REPAIR_LIMIT_RANGE = 1..39

    // Valid range for repair cost
    @JvmField
    val REPAIR_COST_RANGE = 0..255

    // Valid range for rename cost
    @JvmField
    val ITEM_RENAME_COST_RANGE = 0..255

    // Valid range for illegal enchantment conflict cost
    @JvmField
    val SACRIFICE_ILLEGAL_COST_RANGE = 0..255

    // Default for removing repair cost limits
    const val DEFAULT_REMOVE_LIMIT = false

    // Valid range for an enchantment limit
    @JvmField
    val ENCHANT_LIMIT_RANGE = 1..255

    // Default value for an enchantment multiplier
    private const val DEFAULT_ENCHANT_VALUE = 0

    // Default value for debug logging
    private const val DEFAULT_DEBUG_LOG = false

    // Default value for debug logging
    private const val DEFAULT_VERBOSE_DEBUG_LOG = false

    /**
     * Default enchantment limit
     */
    private val defaultEnchantLimit: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(DEFAULT_LIMIT_PATH, DEFAULT_ENCHANT_LIMIT)
        }

    /**
     * Whether to limit repair costs to the vanilla limit
     */
    val limitRepairCost: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(LIMIT_REPAIR_COST, DEFAULT_LIMIT_REPAIR)
        }

    /**
     * Value to limit repair costs to
     */
    val limitRepairValue: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(LIMIT_REPAIR_VALUE, DEFAULT_LIMIT_REPAIR_VALUE)
                .takeIf { it in REPAIR_LIMIT_RANGE }
                ?: DEFAULT_LIMIT_REPAIR_VALUE
        }

    /**
     * Value of an item repair
     */
    val itemRepairCost: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(ITEM_REPAIR_COST, DEFAULT_ITEM_REPAIR_COST)
                .takeIf { it in REPAIR_COST_RANGE }
                ?: DEFAULT_ITEM_REPAIR_COST
        }

    /**
     * Value of an item repair
     */
    val unitRepairCost: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(UNIT_REPAIR_COST, DEFAULT_UNIT_REPAIR_COST)
                .takeIf { it in REPAIR_COST_RANGE }
                ?: DEFAULT_UNIT_REPAIR_COST
        }

    /**
     * Value of an item rename
     */
    val itemRenameCost: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(ITEM_RENAME_COST, DEFAULT_ITEM_RENAME_COST)
                .takeIf { it in ITEM_RENAME_COST_RANGE }
                ?: DEFAULT_ITEM_RENAME_COST
        }

    /**
     * Value of illegal enchantment conflict
     */
    val sacrificeIllegalCost: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(SACRIFICE_ILLEGAL_COST, DEFAULT_SACRIFICE_ILLEGAL_COST)
                .takeIf { it in SACRIFICE_ILLEGAL_COST_RANGE }
                ?: DEFAULT_SACRIFICE_ILLEGAL_COST
        }

    /**
     * Whether to remove repair cost limit
     */
    val removeRepairLimit: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(REMOVE_REPAIR_LIMIT, DEFAULT_REMOVE_LIMIT)
        }

    /**
     * Whether to show debug logging
     */
    val debugLog: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(DEBUG_LOGGING, DEFAULT_DEBUG_LOG)
        }

    /**
     * Whether to show verbose debug logging
     */
    val verboseDebugLog: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(VERBOSE_DEBUG_LOGGING, DEFAULT_VERBOSE_DEBUG_LOG)
        }

    /**
     * Get the given [enchantment]'s limit
     */
    fun enchantLimit(enchantment: Enchantment): Int {
        val path = "${ENCHANT_LIMIT_ROOT}.${enchantment.enchantmentName}"
        return CustomAnvil.instance
            .config
            .getInt(path, defaultEnchantLimit)
            .takeIf { it in ENCHANT_LIMIT_RANGE }
            ?: defaultEnchantLimit
    }

    /**
     * Get the appropriate [enchantment]'s value dependent on whether
     * it's source [isFromBook]
     */
    fun enchantmentValue(
        enchantment: Enchantment,
        isFromBook: Boolean
    ): Int {
        val typeKey = if (isFromBook) KEY_BOOK else KEY_ITEM
        val path = "${ENCHANT_VALUES_ROOT}.${enchantment.enchantmentName}.$typeKey"
        return CustomAnvil.instance
            .config
            .getInt(path, DEFAULT_ENCHANT_VALUE)
            .takeIf { it >= DEFAULT_ENCHANT_VALUE }
            ?: DEFAULT_ENCHANT_VALUE
    }

    /**
     * Get an array of key of basic config options
     */
    fun getBasicConfigKeys(): Array<String> {
        return arrayOf(
            DEFAULT_LIMIT_PATH,
            LIMIT_REPAIR_COST,
            LIMIT_REPAIR_VALUE,
            ITEM_REPAIR_COST,
            UNIT_REPAIR_COST,
            ITEM_RENAME_COST,
            SACRIFICE_ILLEGAL_COST,
            REMOVE_REPAIR_LIMIT
        )
    }


}
