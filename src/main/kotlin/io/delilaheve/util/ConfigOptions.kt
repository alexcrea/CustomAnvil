package io.delilaheve.util

import io.delilaheve.CustomAnvil
import io.delilaheve.util.EnchantmentUtil.enchantmentName
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.config.WorkPenaltyType
import xyz.alexcrea.cuanvil.config.WorkPenaltyType.WorkPenaltyPart
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.util.AnvilUseType
import java.util.*

/**
 * Config option accessors
 */
object ConfigOptions {

    // ----------------------
    // Path for config values
    // ----------------------

    const val CAP_ANVIL_COST = "limit_repair_cost"
    const val MAX_ANVIL_COST = "limit_repair_value"
    const val REMOVE_ANVIL_COST_LIMIT = "remove_repair_limit"

    const val REPLACE_TOO_EXPENSIVE = "replace_too_expensive"

    const val ITEM_REPAIR_COST = "item_repair_cost"
    const val UNIT_REPAIR_COST = "unit_repair_cost"

    const val ITEM_RENAME_COST = "item_rename_cost"

    const val SACRIFICE_ILLEGAL_COST = "sacrifice_illegal_enchant_cost"

    // Color related config
    const val ALLOW_COLOR_CODE = "allow_color_code"
    const val ALLOW_HEXADECIMAL_COLOR = "allow_hexadecimal_color"
    const val PERMISSION_NEEDED_FOR_COLOR = "permission_needed_for_color"
    const val USE_OF_COLOR_COST = "use_of_color_cost"

    // Work penalty config
    const val WORK_PENALTY_ROOT = "work_penalty"
    const val WORK_PENALTY_INCREASE = "shared_increase"
    const val WORK_PENALTY_ADDITIVE = "shared_additive"
    const val EXCLUSIVE_WORK_PENALTY_INCREASE = "exclusive_increase"
    const val EXCLUSIVE_WORK_PENALTY_ADDITIVE = "exclusive_additive"

    const val DEFAULT_LIMIT_PATH = "default_limit"

    const val ENCHANT_LIMIT_ROOT = "enchant_limits"
    const val ENCHANT_VALUES_ROOT = "enchant_values"

    const val DISABLE_MERGE_OVER_ROOT = "disable-merge-over"

    // Keys for specific enchantment values
    private const val KEY_BOOK = "book"
    private const val KEY_ITEM = "item"

    // Debug flag
    const val DEBUG_LOGGING = "debug_log"
    const val VERBOSE_DEBUG_LOGGING = "debug_log_verbose"

    // ----------------------
    // Default config values
    // ----------------------

    const val DEFAULT_CAP_ANVIL_COST = false
    const val DEFAULT_MAX_ANVIL_COST = 39
    const val DEFAULT_REMOVE_ANVIL_COST_LIMIT = false

    const val DEFAULT_REPLACE_TOO_EXPENSIVE = false

    const val DEFAULT_ITEM_REPAIR_COST = 2
    const val DEFAULT_UNIT_REPAIR_COST = 1

    const val DEFAULT_ITEM_RENAME_COST = 1

    const val DEFAULT_SACRIFICE_ILLEGAL_COST = 1

    // Color related config
    const val DEFAULT_ALLOW_COLOR_CODE = false
    const val DEFAULT_ALLOW_HEXADECIMAL_COLOR = false
    const val DEFAULT_PERMISSION_NEEDED_FOR_COLOR = true
    const val DEFAULT_USE_OF_COLOR_COST = 0

    const val DEFAULT_ENCHANT_LIMIT = 5

    // Debug flag
    private const val DEFAULT_DEBUG_LOG = false
    private const val DEFAULT_VERBOSE_DEBUG_LOG = false

    // -------------
    // Config Ranges
    // -------------

    // Valid range for repair cost limit
    @JvmField
    val MAX_ANVIL_COST_RANGE = 0..1000

    // Valid range for repair cost
    @JvmField
    val REPAIR_COST_RANGE = 0..1000

    // Valid range for rename cost
    @JvmField
    val ITEM_RENAME_COST_RANGE = 0..1000

    // Valid range for illegal enchantment conflict cost
    @JvmField
    val SACRIFICE_ILLEGAL_COST_RANGE = 0..1000

    // Valid range for color use cost
    @JvmField
    val USE_OF_COLOR_COST_RANGE = 0..1000

    // Valid range for an enchantment limit
    @JvmField
    val ENCHANT_LIMIT_RANGE = 1..255

    // --------------
    // Other defaults
    // --------------

    // Default value for an enchantment multiplier
    private const val DEFAULT_ENCHANT_VALUE = 0

    // Default max before merge disabled (negative mean enabled)
    const val DEFAULT_MAX_BEFORE_MERGE_DISABLED = -1

    // -------------
    // Get methods
    // -------------

    /**
     * Whether to cap anvil costs
     */
    val doCapCost: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(CAP_ANVIL_COST, DEFAULT_CAP_ANVIL_COST)
        }

    /**
     * Value to limit anvil costs to
     */
    val maxAnvilCost: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(MAX_ANVIL_COST, DEFAULT_MAX_ANVIL_COST)
                .takeIf { it in MAX_ANVIL_COST_RANGE }
                ?: DEFAULT_MAX_ANVIL_COST
        }

    /**
     * Whether to remove anvil cost limit
     */
    val doRemoveCostLimit: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(REMOVE_ANVIL_COST_LIMIT, DEFAULT_REMOVE_ANVIL_COST_LIMIT)
        }

    /**
     * Whether to remove repair cost limit
     */
    val doReplaceTooExpensive: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(REPLACE_TOO_EXPENSIVE, DEFAULT_REPLACE_TOO_EXPENSIVE)
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
     * Allow usage of color code
     */
    val allowColorCode: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(ALLOW_COLOR_CODE, DEFAULT_ALLOW_COLOR_CODE)
        }

    /**
     * Allow usage of hexadecimal color
     */
    val allowHexadecimalColor: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(ALLOW_HEXADECIMAL_COLOR, DEFAULT_ALLOW_HEXADECIMAL_COLOR)
        }

    /**
     * If one of the color component is enabled
     */
    val renameColorPossible: Boolean
        get() {
            return allowColorCode || allowHexadecimalColor
        }

    /**
     * If players need a permission to use color
     */
    val permissionNeededForColor: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(PERMISSION_NEEDED_FOR_COLOR, DEFAULT_PERMISSION_NEEDED_FOR_COLOR)
        }

    /**
     * How many xp should use of color should cost
     */
    val useOfColorCost: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(USE_OF_COLOR_COST, DEFAULT_USE_OF_COLOR_COST)
                .takeIf { it in USE_OF_COLOR_COST_RANGE }
                ?: DEFAULT_USE_OF_COLOR_COST
        }

    /**
     * How work penalties should work
     */
    val workPenaltyType: WorkPenaltyType
        get() {
            val penaltyMap = EnumMap<AnvilUseType, WorkPenaltyPart>(AnvilUseType::class.java)

            for (type in AnvilUseType.entries) {
                penaltyMap[type] = workPenaltyPart(type)
            }

            return WorkPenaltyType(penaltyMap)
        }

    /**
     * How work penalty should work
     */
    fun workPenaltyPart(type: AnvilUseType): WorkPenaltyPart {
        val config = ConfigHolder.DEFAULT_CONFIG.config

        // Find values
        val defaultPenalty = type.defaultPenalty
        val section = config.getConfigurationSection(type.path) ?: return defaultPenalty

        val penaltyIncrease = section.getBoolean(WORK_PENALTY_INCREASE, defaultPenalty.penaltyIncrease)
        val penaltyAdditive = section.getBoolean(WORK_PENALTY_ADDITIVE, defaultPenalty.penaltyAdditive)
        val exclusivePenaltyIncrease =
            section.getBoolean(EXCLUSIVE_WORK_PENALTY_INCREASE, defaultPenalty.exclusivePenaltyIncrease)
        val exclusivePenaltyAdditive =
            section.getBoolean(EXCLUSIVE_WORK_PENALTY_ADDITIVE, defaultPenalty.exclusivePenaltyAdditive)

        return WorkPenaltyPart(penaltyIncrease, penaltyAdditive, exclusivePenaltyIncrease, exclusivePenaltyAdditive)
    }

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
    fun enchantLimit(enchantment: CAEnchantment): Int {
        // Test namespace
        var limit = enchantLimit(enchantment.key.toString())
        if (limit != null) return limit

        // Test legacy (name only)
        limit = enchantLimit(enchantment.enchantmentName)
        if (limit != null) return limit

        // get default (and test old legacy if present)
        return getDefaultLevel(enchantment.enchantmentName)
    }

    /**
     * Get the given [enchantmentName]'s limit
     */
    private fun enchantLimit(enchantmentName: String): Int? {

        val path = "${ENCHANT_LIMIT_ROOT}.$enchantmentName"
        return CustomAnvil.instance
            .config
            .getInt(path, ENCHANT_LIMIT_RANGE.first - 1)
            .takeIf { it in ENCHANT_LIMIT_RANGE }
    }

    /**
     * Get default value if enchantment do not exist on config
     */
    private fun getDefaultLevel(
        enchantmentName: String, // compatibility with 1.20.5. TODO better update system
    ): Int {
        if (enchantmentName == "sweeping_edge") {
            val limit = enchantLimit("sweeping")
            if (limit != null) return limit

        }
        return defaultEnchantLimit
    }

    /**
     * Get the appropriate [enchantment]'s value dependent on whether
     * it's source [isFromBook]
     */
    fun enchantmentValue(
        enchantment: CAEnchantment,
        isFromBook: Boolean
    ): Int {
        // Test namespace
        var limit = enchantmentValue(enchantment.key.toString(), isFromBook)
        if (limit != null) return limit

        // Test legacy (name only)
        limit = enchantmentValue(enchantment.enchantmentName, isFromBook)
        if (limit != null) return limit

        // get default (and test old legacy if present)
        return getDefaultValue(enchantment, isFromBook)

    }

    /**
     * Get the appropriate [enchantmentName]'s value dependent on whether
     * it's source [isFromBook]
     */
    private fun enchantmentValue(
        enchantmentName: String,
        isFromBook: Boolean
    ): Int? {
        val typeKey = if (isFromBook) KEY_BOOK else KEY_ITEM
        val path = "${ENCHANT_VALUES_ROOT}.${enchantmentName}.$typeKey"
        return CustomAnvil.instance
            .config
            .getInt(path, DEFAULT_ENCHANT_VALUE - 1)
            .takeIf { it >= DEFAULT_ENCHANT_VALUE }
    }

    /**
     * Get default value if enchantment do not exist on config
     */
    private fun getDefaultValue(
        enchantment: CAEnchantment, // compatibility with 1.20.5. TODO better update system
        isFromBook: Boolean
    ): Int {

        val enchantmentName = enchantment.key.toString()
        if (enchantmentName == "minecraft:sweeping_edge") {
            var limit = enchantmentValue("minecraft:sweeping", isFromBook)
            if (limit != null) return limit

            // legacy name
            limit = enchantmentValue("sweeping", isFromBook)
            if (limit != null) return limit
        }

        val rarity = enchantment.defaultRarity()
        return if (isFromBook)
            rarity.bookValue
        else
            rarity.itemValue
    }

    /**
     * Get the given [enchantmentName]'s level before merge is disabled
     * a negative value would mean never disabled
     */
    fun maxBeforeMergeDisabled(enchantment: CAEnchantment): Int {
        val key = enchantment.key.toString()
        var value = maxBeforeMergeDisabled(key)
        if (value != null) return value

        // Legacy name
        val legacy = enchantment.enchantmentName
        value = maxBeforeMergeDisabled(legacy)
        if (value != null) return value

        if (key == "minecraft:sweeping_edge") {
            value = maxBeforeMergeDisabled("minecraft:sweeping")
            if (value != null) return value

            // legacy name of legacy enchantment name
            value = maxBeforeMergeDisabled("sweeping")
            if (value != null) return value
        }

        return DEFAULT_MAX_BEFORE_MERGE_DISABLED
    }

    /**
     * Get the given [enchantmentName]'s level before merge is disabled
     * a negative value would mean never disabled
     */
    private fun maxBeforeMergeDisabled(enchantmentName: String): Int? {
        // find if set
        val path = "${DISABLE_MERGE_OVER_ROOT}.$enchantmentName"

        return CustomAnvil.instance
            .config
            .getInt(path, ENCHANT_LIMIT_RANGE.min() - 1)
            .takeIf { it in ENCHANT_LIMIT_RANGE }
    }

}
