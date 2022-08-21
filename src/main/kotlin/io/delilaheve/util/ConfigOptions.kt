package io.delilaheve.util

import io.delilaheve.UnsafeEnchants
import io.delilaheve.util.EnchantmentUtil.enchantmentName
import org.bukkit.enchantments.Enchantment

/**
 * Config option accessors
 */
object ConfigOptions {

    // Path for default enchantment limits
    private const val DEFAULT_LIMIT_PATH = "default_limit"
    // Path for allowing unsafe enchants
    private const val ALLOW_UNSAFE_PATH = "allow_unsafe"
    // Path for limiting repair cost
    private const val LIMIT_REPAIR_COST = "limit_repair_cost"
    // Path for removing repair cost limits
    private const val REMOVE_REPAIR_LIMIT = "remove_repair_limit"
    // Root path for enchantment limits
    private const val ENCHANT_LIMIT_ROOT = "enchant_limits"
    // Root path for enchantment values
    private const val ENCHANT_VALUES_ROOT = "enchant_values"
    // Keys for specific enchantment values
    private const val KEY_BOOK = "book"
    private const val KEY_ITEM = "item"
    // Debug logging toggle path
    private const val DEBUG_LOGGING = "debug_log"

    // Default value for enchantment limits
    private const val DEFAULT_ENCHANT_LIMIT = 10
    // Default value for allowing unsafe enchantments
    private const val DEFAULT_ALLOW_UNSAFE = true
    // Default value for limiting repair cost
    private const val DEFAULT_LIMIT_REPAIR = true
    // Default for removing repair cost limits
    private const val DEFAULT_REMOVE_LIMIT = false
    // Valid range for an enchantment limit
    private val ENCHANT_LIMIT_RANGE = 1..255
    // Default value for an enchantment multiplier
    private const val DEFAULT_ENCHANT_VALUE = 0
    // Default value for debug logging
    private const val DEFAULT_DEBUG_LOG = false

    /**
     * Default enchantment limit
     */
    private val defaultEnchantLimit: Int
        get() {
            return UnsafeEnchants.instance
                .config
                .getInt(DEFAULT_LIMIT_PATH, DEFAULT_ENCHANT_LIMIT)
        }

    /**
     * Whether to allow unsafe enchantments
     */
    val allowUnsafe: Boolean
        get() {
            return UnsafeEnchants.instance
                .config
                .getBoolean(ALLOW_UNSAFE_PATH, DEFAULT_ALLOW_UNSAFE)
        }

    /**
     * Whether to limit repair costs to the vanilla limit
     */
    val limitRepairCost: Boolean
        get() {
            return UnsafeEnchants.instance
                .config
                .getBoolean(LIMIT_REPAIR_COST, DEFAULT_LIMIT_REPAIR)
        }

    /**
     * Whether to remove repair cost limit
     */
    val removeRepairLimit: Boolean
        get() {
            return UnsafeEnchants.instance
                .config
                .getBoolean(REMOVE_REPAIR_LIMIT, DEFAULT_REMOVE_LIMIT)
        }

    /**
     * Whether to show debug logging
     */
    val debugLog: Boolean
        get() {
            return UnsafeEnchants.instance
                .config
                .getBoolean(DEBUG_LOGGING, DEFAULT_DEBUG_LOG)
        }

    /**
     * Get the given [enchantment]'s limit
     */
    fun enchantLimit(enchantment: Enchantment): Int {
        val path = "${ENCHANT_LIMIT_ROOT}.${enchantment.enchantmentName}"
        return UnsafeEnchants.instance
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
        return UnsafeEnchants.instance
            .config
            .getInt(path, DEFAULT_ENCHANT_VALUE)
            .takeIf { it >= DEFAULT_ENCHANT_VALUE }
            ?: DEFAULT_ENCHANT_VALUE
    }

}
