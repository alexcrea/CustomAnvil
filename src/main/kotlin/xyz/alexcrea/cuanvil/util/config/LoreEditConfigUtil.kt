package xyz.alexcrea.cuanvil.util.config

import xyz.alexcrea.cuanvil.config.ConfigHolder.DEFAULT_CONFIG as CONFIG

object LoreEditConfigUtil {

    // Per edit type configs Path
    const val IS_ENABLED = "enabled"
    const val FIXED_COST = "fixed_cost"
    const val PER_LINE_COST = "per_line_cost"
    const val DO_CONSUME = "do_consume"

    // Permission configs path
    const val BOOK_PERMISSION_NEEDED = "lore_edit.book_and_quil.use_permission"
    const val PAPER_PERMISSION_NEEDED = "lore_edit.paper.use_permission"

    // Color configs path
    const val ALLOW_COLOR_CODE = "allow_color_code"
    const val ALLOW_HEX_COLOR = "allow_hexadecimal_color"
    const val USE_COLOR_COST = "use_cost"

    const val REMOVE_COLOR_ON_LORE_REMOVE = "remove_color_on_remove"
    const val REMOVE_COLOR_COST = "remove_color_cost"

    // Lore order config path
    const val PAPER_EDIT_ORDER = "lore_edit.paper.order"

    // --------------
    // Default Values
    // --------------

    // Per edit type configs defaults
    const val DEFAULT_IS_ENABLED = false
    const val DEFAULT_FIXED_COST = 1
    const val DEFAULT_PER_LINE_COST = 0
    const val DEFAULT_DO_CONSUME = false

    // Permission configs defaults
    const val DEFAULT_BOOK_PERMISSION_NEEDED = true
    const val DEFAULT_PAPER_PERMISSION_NEEDED = true

    // Color configs defaults
    const val DEFAULT_ALLOW_COLOR_CODE = true
    const val DEFAULT_ALLOW_HEX_COLOR = true
    const val DEFAULT_USE_COLOR_COST = 0

    const val DEFAULT_REMOVE_COLOR_ON_LORE_REMOVE = false
    const val DEFAULT_REMOVE_COLOR_COST = 0

    // Lore order config default
    const val DEFAULT_PAPER_EDIT_ORDER = "end"

    // -------------
    // Config Ranges
    // -------------

    val FIXED_COST_RANGE = 0..1000
    val PER_LINE_COST_RANGE = 0..1000

    val USE_COLOR_COST_RANGE = 0..1000
    val REMOVE_COLOR_COST_RANGE = 0..1000

    // -------------------
    // Generic Get methods
    // -------------------

    /**
     * Get if we should append/remove at the end or at the start of the lore list
     * This may change to an "OrderType" enum or equivalent later
     */
    val paperLoreOrderIsEnd: Boolean
        get() {
            return CONFIG
                .config
                .getString(PAPER_EDIT_ORDER, DEFAULT_PAPER_EDIT_ORDER)
                .equals(DEFAULT_PAPER_EDIT_ORDER, ignoreCase = true)
        }

    /**
     * If lore edit via book need permission
     */
    val bookLoreEditNeedPermission: Boolean
        get() {
            return CONFIG
                .config
                .getBoolean(BOOK_PERMISSION_NEEDED, DEFAULT_BOOK_PERMISSION_NEEDED)
        }

    /**
     * If lore edit via paper need permission
     */
    val paperLoreEditNeedPermission: Boolean
        get() {
            return CONFIG
                .config
                .getBoolean(PAPER_PERMISSION_NEEDED, DEFAULT_PAPER_PERMISSION_NEEDED)
        }

    // -----------------
    // Color Get methods
    // -----------------

}