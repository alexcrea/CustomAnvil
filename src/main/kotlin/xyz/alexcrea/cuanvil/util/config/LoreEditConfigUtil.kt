package xyz.alexcrea.cuanvil.util.config

import xyz.alexcrea.cuanvil.config.ConfigHolder.DEFAULT_CONFIG as CONFIG

object LoreEditConfigUtil {

    // Per edit type configs Path
    const val IS_ENABLED = "enabled"
    const val FIXED_COST = "fixed_cost"
    const val PER_LINE_COST = "per_line_cost"
    const val USE_COST_PENALTY = "use_cost_penalty"
    const val INCREASE_COST_PENALTY = "increase_cost_penalty"
    const val DO_CONSUME = "do_consume"

    // Permission configs path
    const val BOOK_PERMISSION_NEEDED = "lore_edit.book_and_quil.use_permission"
    const val PAPER_PERMISSION_NEEDED = "lore_edit.paper.use_permission"

    // Color configs path
    const val COLOR_BOOK_COLOR_CODE = "lore_edit.book_and_quil.color.allow_color_code"
    const val COLOR_BOOK_HEX = "lore_edit.book_and_quil.color.allow_hexadecimal_color"
    const val COLOR_BOOK_COST = "lore_edit.book_and_quil.color.use_cost"

    const val COLOR_PAPER_COLOR_CODE = "lore_edit.paper.color.allow_color_code"
    const val COLOR_PAPER_HEX = "lore_edit.paper.color.allow_hexadecimal_color"
    const val COLOR_PAPER_COST = "lore_edit.paper.color.use_cost"

    // Lore order config path
    const val PAPER_EDIT_ORDER = "lore_edit.paper.order"

    // --------------
    // Default Values
    // --------------

    // Per edit type configs defaults
    const val DEFAULT_IS_ENABLED = true
    const val DEFAULT_FIXED_COST = 1
    const val DEFAULT_PER_LINE_COST = 0
    const val DEFAULT_USE_COST_PENALTY = false
    const val DEFAULT_INCREASE_COST_PENALTY = false
    const val DEFAULT_DO_CONSUME = false

    // Permission configs defaults
    const val DEFAULT_BOOK_PERMISSION_NEEDED = true
    const val DEFAULT_PAPER_PERMISSION_NEEDED = true

    // Color configs defaults
    const val DEFAULT_COLOR_BOOK_COLOR_CODE = true
    const val DEFAULT_COLOR_BOOK_HEX = true
    const val DEFAULT_COLOR_BOOK_COST = 0

    const val DEFAULT_COLOR_PAPER_COLOR_CODE = true
    const val DEFAULT_COLOR_PAPER_HEX = true
    const val DEFAULT_COLOR_PAPER_COST = 0

    // Lore order config default
    const val DEFAULT_PAPER_EDIT_ORDER = "end"

    // -------------
    // Config Ranges
    // -------------

    val FIXED_COST_RANGE = 0..1000
    val PER_LINE_COST_RANGE = 0..1000

    val COLOR_BOOK_COST_RANGE = 0..1000
    val COLOR_PAPER_COST_RANGE = 0..1000

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

    // book edit functions
    /**
     * Allow usage of color code on book lore edit
     */
    val bookAllowColorCode: Boolean
        get() {
            return CONFIG
                .config
                .getBoolean(COLOR_BOOK_COLOR_CODE, DEFAULT_COLOR_BOOK_COLOR_CODE)
        }

    /**
     * Allow usage of hexadecimal color on book lore edit
     */
    val bookAllowHexColor: Boolean
        get() {
            return CONFIG
                .config
                .getBoolean(COLOR_BOOK_HEX, DEFAULT_COLOR_BOOK_HEX)
        }

    /**
     * Cost when using either color code and hex color on book edit
     */
    val bookUseColorCost: Int
        get() {
            return CONFIG
                .config
                .getInt(COLOR_BOOK_COST, DEFAULT_COLOR_BOOK_COST)
                .takeIf { it in COLOR_BOOK_COST_RANGE }
                ?: DEFAULT_COLOR_BOOK_COST

        }

    // paper edit functions
    /**
     * Allow usage of color code on paper lore edit
     */
    val paperAllowColorCode: Boolean
        get() {
            return CONFIG
                .config
                .getBoolean(COLOR_PAPER_COLOR_CODE, DEFAULT_COLOR_PAPER_COLOR_CODE)
        }

    /**
     * Allow usage of hexadecimal color on paper lore edit
     */
    val paperAllowHexColor: Boolean
        get() {
            return CONFIG
                .config
                .getBoolean(COLOR_PAPER_HEX, DEFAULT_COLOR_PAPER_HEX)
        }

    /**
     * Cost when using either color code and hex color on paper edit
     */
    val paperUseColorCost: Int
        get() {
            return CONFIG
                .config
                .getInt(COLOR_PAPER_COST, DEFAULT_COLOR_PAPER_COST)
                .takeIf { it in COLOR_PAPER_COST_RANGE }
                ?: DEFAULT_COLOR_PAPER_COST

        }

}