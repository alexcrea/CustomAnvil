package io.delilaheve.util

import io.delilaheve.CustomAnvil
import io.delilaheve.util.EnchantmentUtil.enchantmentName
import org.bukkit.NamespacedKey
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.anvil.AnvilUseType
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.config.WorkPenaltyType
import xyz.alexcrea.cuanvil.config.WorkPenaltyType.WorkPenaltyPart
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.economy.EconomyManager
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.util.dialog.AnvilRenameDialogUtil
import java.math.BigDecimal
import java.util.*

/**
 * Config option accessors
 */
object ConfigOptions {

    // ----------------------
    // Path for config values
    // ----------------------

    const val METRIC_TYPE = "metric_type"
    const val METRIC_COLLECT_ERROR = "metric_collect_errors"

    const val CAP_ANVIL_COST = "limit_repair_cost"
    const val MAX_ANVIL_COST = "limit_repair_value"
    const val REMOVE_ANVIL_COST_LIMIT = "remove_repair_limit"

    const val REPLACE_TOO_EXPENSIVE = "replace_too_expensive"

    const val ITEM_REPAIR_COST = "item_repair_cost"
    const val UNIT_REPAIR_COST = "unit_repair_cost"

    const val ITEM_RENAME_COST = "item_rename_cost"

    const val SACRIFICE_ILLEGAL_COST = "sacrifice_illegal_enchant_cost"
    const val ADD_BOOK_ENCHANTMENT_AS_STORED_ENCHANTMENT = "add_book_enchantment_as_stored_enchantment"

    // Color related config
    const val ALLOW_COLOR_CODE = "allow_color_code"
    const val ALLOW_HEXADECIMAL_COLOR = "allow_hexadecimal_color"
    const val ALLOW_MINIMESSAGE = "allow_minimessage"
    const val PERMISSION_NEEDED_FOR_COLOR = "permission_needed_for_color"
    const val USE_OF_COLOR_COST = "use_of_color_cost"

    const val PER_COLOR_CODE_PERMISSION = "per_color_code_permission"

    // Work penalty config
    const val WORK_PENALTY_ROOT = "work_penalty"
    const val WORK_PENALTY_INCREASE = "shared_increase"
    const val WORK_PENALTY_ADDITIVE = "shared_additive"
    const val EXCLUSIVE_WORK_PENALTY_INCREASE = "exclusive_increase"
    const val EXCLUSIVE_WORK_PENALTY_ADDITIVE = "exclusive_additive"

    // Enchant limit config
    const val ENCHANT_COUNT_LIMIT_ROOT = "enchantment_count_limit"
    const val ENCHANT_COUNT_LIMIT_DEFAULT = "$ENCHANT_COUNT_LIMIT_ROOT.default"
    const val ENCHANT_COUNT_LIMIT_ITEMS = "$ENCHANT_COUNT_LIMIT_ROOT.items"

    const val ENCHANT_LIMIT_ROOT = "enchant_limits"
    const val ENCHANT_VALUES_ROOT = "enchant_values"

    // Dialog menu rename
    const val DIALOG_RENAME_ENABLED = "enable_dialog_rename"
    const val DIALOG_MAX_SIZE = "dialog_rename_max_size"
    const val DIALOG_RENAME_USE_PERMISSION = "permission_needed_for_dialog_rename"
    const val DIALOG_KEEP_USER_TEXT = "dialog_rename_keep_user_text"

    // Others
    const val DISABLE_MERGE_OVER_ROOT = "disable-merge-over"

    const val IMMUTABLE_ENCHANTMENT_LIST = "immutable_enchantments"

    // Monetary configs
    const val MONETARY_USAGE_ROOT = "monetary_cost"
    const val SHOULD_USE_MONEY = "$MONETARY_USAGE_ROOT.enabled"
    const val MONEY_CURRENCY = "$MONETARY_USAGE_ROOT.currency"
    const val MONETARY_MULTIPLIER_ROOT = "$MONETARY_USAGE_ROOT.multipliers"

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
    const val DEFAULT_ADD_BOOK_ENCHANTMENT_AS_STORED_ENCHANTMENT = false

    const val DEFAULT_ENCHANT_COUNT_LIMIT = -1

    // Color related config
    const val DEFAULT_ALLOW_COLOR_CODE = false
    const val DEFAULT_ALLOW_HEXADECIMAL_COLOR = false
    const val DEFAULT_ALLOW_MINIMESSAGE = false
    const val DEFAULT_PERMISSION_NEEDED_FOR_COLOR = true
    const val DEFAULT_USE_OF_COLOR_COST = 0

    const val DEFAULT_PER_COLOR_CODE_PERMISSION = false

    // Monetary configs
    const val DEFAULT_SHOULD_USE_MONEY = false
    const val DEFAULT_MONEY_CURRENCY = "default"
    const val DEFAULT_MONEY_MULTIPLIER = 1.0

    // Debug flag
    private const val DEFAULT_DEBUG_LOG = false
    private const val DEFAULT_VERBOSE_DEBUG_LOG = false

    var OVERRIDE_DEBUG_LOG: Boolean? = null
    var OVERRIDE_VERBOSE_DEBUG_LOG: Boolean? = null

    // Dialog menu rename
    const val DEFAULT_DIALOG_RENAME_ENABLED = false
    const val DEFAULT_DIALOG_MAX_SIZE = 256
    const val DEFAULT_DIALOG_RENAME_USE_PERMISSION = false
    const val DEFAULT_DIALOG_KEEP_USER_TEXT = true

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

    @JvmField
    val DIALOG_MAX_SIZE_RANGE = 0..Int.MAX_VALUE

    // Valid range for an enchantment limit
    const val ENCHANT_LIMIT = 255

    // Valid range for an enchantment count limit
    @JvmField
    val ENCHANT_COUNT_LIMIT_RANGE = -1..255

    // --------------
    // Other defaults
    // --------------

    // Default value for an enchantment multiplier
    private const val DEFAULT_ENCHANT_VALUE = 0

    // Default max before merge disabled (negative mean enabled)
    const val DEFAULT_MAX_BEFORE_MERGE_DISABLED = -1

    // -----------
    // Permissions
    // -----------
    private const val RENAME_DIALOG_PERMISSION = "ca.rename.dialog"

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
     * Consider book enchantment as book stored enchantment
     */
    val addBookEnchantmentAsStoredEnchantment : Boolean
        get(){
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(ADD_BOOK_ENCHANTMENT_AS_STORED_ENCHANTMENT, DEFAULT_ADD_BOOK_ENCHANTMENT_AS_STORED_ENCHANTMENT)
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
     * Allow usage of minimessage formating
     */
    val allowMinimessage: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(ALLOW_MINIMESSAGE, DEFAULT_ALLOW_MINIMESSAGE)
        }

    /**
     * If one of the color component is enabled
     */
    val renameColorPossible: Boolean
        get() {
            return allowColorCode || allowHexadecimalColor || allowMinimessage
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
     * Should each color code require a permission
     */
    val usePerColorCodePermission: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(PER_COLOR_CODE_PERMISSION, DEFAULT_PER_COLOR_CODE_PERMISSION)
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
     * Get material enchantment count limit
     *
     * @return the current enchantment limit. -1 if none
     */
    fun getEnchantCountLimit(type: NamespacedKey): Int? {
        val limit = materialEnchantCountLimit(type)

        if(limit != null) return limit
        if(defaultEnchantCountLimit >= 0) return defaultEnchantCountLimit

        return DependencyManager.ecoEnchantCompatibility?.getEcoLevelLimit()
    }

    /**
     * Get the material enchantment count limit.
     *
     * @return The current enchantment limit. -1 if none
     */
    private fun materialEnchantCountLimit(type: NamespacedKey): Int? {
        val path = "$ENCHANT_COUNT_LIMIT_ITEMS.${type.key.lowercase()}"
        if(!ConfigHolder.DEFAULT_CONFIG.config.isInt(path))
            return null

        return ConfigHolder.DEFAULT_CONFIG.config
            .getInt(path)
            .takeIf { it in ENCHANT_COUNT_LIMIT_RANGE }
    }
    /**
     * User configured default enchantment count limit
     */
    val defaultEnchantCountLimit: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(ENCHANT_COUNT_LIMIT_DEFAULT, DEFAULT_ENCHANT_COUNT_LIMIT)
                .takeIf { it in ENCHANT_COUNT_LIMIT_RANGE }
                ?: DEFAULT_ENCHANT_COUNT_LIMIT
        }

    /**
     * Whether to show debug logging
     */
    val debugLog: Boolean
        get() {
            val overrider = OVERRIDE_DEBUG_LOG
            if(overrider != null) return overrider

            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(DEBUG_LOGGING, DEFAULT_DEBUG_LOG)
        }

    /**
     * Whether to show verbose debug logging
     */
    val verboseDebugLog: Boolean
        get() {
            val overrider = OVERRIDE_VERBOSE_DEBUG_LOG
            if(overrider != null) return overrider

            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(VERBOSE_DEBUG_LOGGING, DEFAULT_VERBOSE_DEBUG_LOG)
        }

    /**
     * Is the dialog menu for rename enabled
     */
    val doRenameDialog: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(DIALOG_RENAME_ENABLED, DEFAULT_DIALOG_RENAME_ENABLED)
        }

    /**
     * Do the dialog menu require permission
     */
    val doRenameDialogUsePermission: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(DIALOG_RENAME_USE_PERMISSION, DEFAULT_DIALOG_RENAME_USE_PERMISSION)
        }

    fun canUseDialogRename(player: HumanEntity): Boolean {
        if(!doRenameDialog || !AnvilRenameDialogUtil.anvilRenameDialog.canSendDialog()) return false
        if(doRenameDialogUsePermission && !player.hasPermission(RENAME_DIALOG_PERMISSION)) return false

        return true
    }

    /**
     * Do the dialog menu require permission
     */
    val renameDialogMaxSize: Int
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getInt(DIALOG_MAX_SIZE, DEFAULT_DIALOG_MAX_SIZE)
                .takeIf { it in DIALOG_MAX_SIZE_RANGE }
                ?: Int.MAX_VALUE
        }

    /**
     * Should the text used for rename should be kept in the item's pdc
     */
    val shouldKeepRenameText: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(DIALOG_KEEP_USER_TEXT, DEFAULT_DIALOG_KEEP_USER_TEXT)
        }

    /**
     * Get the given [enchantment]'s limit
     */
    fun enchantLimit(enchantment: CAEnchantment): Int {
        val limit = rawEnchantLimit(enchantment)
        if(limit >= 0) return limit.coerceAtMost(ENCHANT_LIMIT)

        // get default
        return enchantment.defaultMaxLevel()
    }

    /**
     * Get the given [enchantment]'s limit
     */
    fun rawEnchantLimit(enchantment: CAEnchantment): Int {
        // Test namespace
        var limit = enchantLimit(enchantment.key.toString())
        if (limit >= 0) return limit

        // Test legacy (name only)
        limit = enchantLimit(enchantment.enchantmentName)
        if (limit >= 0) return limit

        // Default to negative
        return -1
    }

    /**
     * Get the given [enchantmentName]'s limit
     */
    private fun enchantLimit(enchantmentName: String): Int {

        val path = "${ENCHANT_LIMIT_ROOT}.$enchantmentName"
        return CustomAnvil.instance.config
            .getInt(path, -1)
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
        if (value >= 0) return value

        // Legacy name
        val legacy = enchantment.enchantmentName
        value = maxBeforeMergeDisabled(legacy)
        if (value >= 0) return value

        if (key == "minecraft:sweeping_edge") {
            value = maxBeforeMergeDisabled("minecraft:sweeping")
            if (value >= 0) return value

            // legacy name of legacy enchantment name
            value = maxBeforeMergeDisabled("sweeping")
            if (value >= 0) return value
        }

        return DEFAULT_MAX_BEFORE_MERGE_DISABLED
    }

    /**
     * Get the given [enchantmentName]'s level before merge is disabled
     * a negative value would mean never disabled
     */
    private fun maxBeforeMergeDisabled(enchantmentName: String): Int {
        // find if set
        val path = "${DISABLE_MERGE_OVER_ROOT}.$enchantmentName"

        return CustomAnvil.instance
            .config
            .getInt(path, -1)
    }

    fun isImmutable(key: NamespacedKey): Boolean {
        val immutables = ConfigHolder.DEFAULT_CONFIG.config.getStringList(IMMUTABLE_ENCHANTMENT_LIST)

        // We need to ignore case so can't just check "contain"
        for (ench in immutables) {
            if (ench.equals(key.toString(), ignoreCase = true) ||
                ench.equals(key.key, ignoreCase = true)
            )
                return true
        }
        return false
    }

    /*
     * Monetary configs (only for 1.21.6+)
     * Also require dialog rename
     */
    fun shouldUseMoney(player: HumanEntity): Boolean {
            return EconomyManager.economy?.initialized() == true &&
                    canUseDialogRename(player) &&
                    ConfigHolder.DEFAULT_CONFIG
                        .config
                        .getBoolean(SHOULD_USE_MONEY, DEFAULT_SHOULD_USE_MONEY)
    }

    val usedCurrency: String
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getString(MONEY_CURRENCY, DEFAULT_MONEY_CURRENCY)!!
        }

    fun getMonetaryMultiplier(type: String): BigDecimal {
        return BigDecimal(ConfigHolder.DEFAULT_CONFIG
                .config
            .getDouble("$MONETARY_MULTIPLIER_ROOT.$type", DEFAULT_MONEY_MULTIPLIER))
    }

}
