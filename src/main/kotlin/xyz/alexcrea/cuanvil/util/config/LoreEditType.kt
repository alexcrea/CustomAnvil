package xyz.alexcrea.cuanvil.util.config

import xyz.alexcrea.cuanvil.util.AnvilUseType
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.ALLOW_COLOR_CODE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.ALLOW_HEX_COLOR
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_ALLOW_COLOR_CODE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_ALLOW_HEX_COLOR
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_REMOVE_COLOR_COST
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_REMOVE_COLOR_ON_LORE_REMOVE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_USE_COLOR_COST
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.REMOVE_COLOR_COST
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.REMOVE_COLOR_COST_RANGE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.REMOVE_COLOR_ON_LORE_REMOVE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.USE_COLOR_COST
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.USE_COLOR_COST_RANGE
import xyz.alexcrea.cuanvil.config.ConfigHolder.DEFAULT_CONFIG as CONFIG

enum class LoreEditType(
    val rootPath: String,
    val useType: AnvilUseType,
    val isAppend: Boolean,
    val isMultiLine: Boolean,
) {
    APPEND_BOOK(AnvilUseType.LORE_EDIT_BOOK_APPEND, true, true),
    REMOVE_BOOK(AnvilUseType.LORE_EDIT_BOOK_REMOVE, false, true),
    APPEND_PAPER(AnvilUseType.LORE_EDIT_PAPER_APPEND, true, false),
    REMOVE_PAPER(AnvilUseType.LORE_EDIT_PAPER_REMOVE, false, false),
    ;

    constructor(
        useType: AnvilUseType,
        isAppend: Boolean,
        isMultiLine: Boolean,
    ) : this(useType.path, useType, isAppend, isMultiLine)

    /**
     * If this edit type is enabled
     */
    val enabled: Boolean
        get() {
            return CONFIG
                .config
                .getBoolean("${rootPath}.${LoreEditConfigUtil.IS_ENABLED}", LoreEditConfigUtil.DEFAULT_IS_ENABLED)
        }

    /**
     * Fixed cost added to this edit
     */
    val fixedCost: Int
        get() {
            return CONFIG
                .config
                .getInt("${rootPath}.${LoreEditConfigUtil.FIXED_COST}", LoreEditConfigUtil.DEFAULT_FIXED_COST)
                .takeIf { it in LoreEditConfigUtil.FIXED_COST_RANGE }
                ?: LoreEditConfigUtil.DEFAULT_FIXED_COST
        }

    /**
     * Cost added per line added
     */
    val perLineCost: Int
        get() {
            if (!isMultiLine) throw IllegalStateException("Per line cost get on single line edit type")
            return CONFIG
                .config
                .getInt("${rootPath}.${LoreEditConfigUtil.PER_LINE_COST}", LoreEditConfigUtil.DEFAULT_PER_LINE_COST)
                .takeIf { it in LoreEditConfigUtil.PER_LINE_COST_RANGE }
                ?: LoreEditConfigUtil.DEFAULT_PER_LINE_COST
        }

    /**
     * If the edit should consume the provided material
     */
    val doConsume: Boolean
        get() {
            return CONFIG
                .config
                .getBoolean("${rootPath}.${LoreEditConfigUtil.DO_CONSUME}", LoreEditConfigUtil.DEFAULT_DO_CONSUME)
        }

    /**
     * Allow usage of color code on lore add
     */
    val allowColorCode: Boolean
        get() {
            if (!isAppend) throw IllegalStateException("Can only call with an append edit type")
            return CONFIG
                .config
                .getBoolean("$rootPath.$ALLOW_COLOR_CODE", DEFAULT_ALLOW_COLOR_CODE)
        }

    /**
     * Allow usage of hexadecimal color on lore add
     */
    val allowHexColor: Boolean
        get() {
            if (!isAppend) throw IllegalStateException("Can only call with an append edit type")
            return CONFIG
                .config
                .getBoolean("${rootPath}.$ALLOW_HEX_COLOR", DEFAULT_ALLOW_HEX_COLOR)
        }

    /**
     * Cost when using either color code and hex color on lore add
     */
    val useColorCost: Int
        get() {
            if (!isAppend) throw IllegalStateException("Can only call with an append edit type")
            return CONFIG
                .config
                .getInt("${rootPath}.$USE_COLOR_COST", DEFAULT_USE_COLOR_COST)
                .takeIf { it in USE_COLOR_COST_RANGE }
                ?: DEFAULT_USE_COLOR_COST

        }

    /**
     * Should the color code & hex color should get removed on lore remove
     */
    val shouldRemoveColorOnLoreRemoval: Boolean
        get() {
            if (isAppend) throw IllegalStateException("Can only call with a remove edit type")
            return CONFIG
                .config
                .getBoolean("${rootPath}.$REMOVE_COLOR_ON_LORE_REMOVE", DEFAULT_REMOVE_COLOR_ON_LORE_REMOVE)
        }

    /**
     * Cost when using either color code and hex color on lore remove
     */
    val removeColorCost: Int
        get() {
            if (isAppend) throw IllegalStateException("Can only call with a remove edit type")
            return CONFIG
                .config
                .getInt("${rootPath}.$REMOVE_COLOR_COST", DEFAULT_REMOVE_COLOR_COST)
                .takeIf { it in REMOVE_COLOR_COST_RANGE }
                ?: DEFAULT_REMOVE_COLOR_COST

        }

}