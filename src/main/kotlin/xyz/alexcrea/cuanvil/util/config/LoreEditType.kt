package xyz.alexcrea.cuanvil.util.config

import xyz.alexcrea.cuanvil.anvil.AnvilUseType
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.ALLOW_COLOUR_CODE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.ALLOW_HEX_COLOUR
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.ALLOW_MINIMESSAGE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_ALLOW_COLOUR_CODE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_ALLOW_HEX_COLOUR
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_ALLOW_MINIMESSAGE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_REMOVE_COLOUR_COST
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.DEFAULT_USE_COLOUR_COST
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.REMOVE_COLOUR_COST
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.REMOVE_COLOUR_COST_RANGE
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.USE_COLOUR_COST
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil.USE_COLOUR_COST_RANGE
import xyz.alexcrea.cuanvil.config.ConfigHolder.DEFAULT as CONFIG

enum class LoreEditType(
    val rootPath: String,
    val useType: AnvilUseType,
    val isBook: Boolean,
    val isAppend: Boolean,
    val isMultiLine: Boolean,
) {
    APPEND_BOOK(AnvilUseType.LORE_EDIT_BOOK_APPEND, true, true, true),
    REMOVE_BOOK(AnvilUseType.LORE_EDIT_BOOK_REMOVE, true, false, true),
    APPEND_PAPER(AnvilUseType.LORE_EDIT_PAPER_APPEND, false, true, false),
    REMOVE_PAPER(AnvilUseType.LORE_EDIT_PAPER_REMOVE, false, false, false),
    ;

    constructor(
        useType: AnvilUseType,
        isPaper: Boolean,
        isAppend: Boolean,
        isMultiLine: Boolean,
    ) : this(useType.path, useType,
        isPaper, isAppend, isMultiLine)

    /**
     * If this edit type is enabled
     */
    val enabled: Boolean
        get() {
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getBoolean("${rootPath}.${LoreEditConfigUtil.IS_ENABLED}", LoreEditConfigUtil.DEFAULT_IS_ENABLED)
            }
        }

    /**
     * Fixed cost added to this edit
     */
    val fixedCost: Int
        get() {
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getInt("${rootPath}.${LoreEditConfigUtil.FIXED_COST}", LoreEditConfigUtil.DEFAULT_FIXED_COST)
                .takeIf { it in LoreEditConfigUtil.FIXED_COST_RANGE }
                ?: LoreEditConfigUtil.DEFAULT_FIXED_COST
            }
        }

    /**
     * Cost added per line added
     */
    val perLineCost: Int
        get() {
            if (!isMultiLine) throw IllegalStateException("Per line cost get on single line edit type")
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getInt("${rootPath}.${LoreEditConfigUtil.PER_LINE_COST}", LoreEditConfigUtil.DEFAULT_PER_LINE_COST)
                .takeIf { it in LoreEditConfigUtil.PER_LINE_COST_RANGE }
                ?: LoreEditConfigUtil.DEFAULT_PER_LINE_COST
            }
        }

    /**
     * If the edit should consume the provided material
     */
    val doConsume: Boolean
        get() {
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getBoolean("${rootPath}.${LoreEditConfigUtil.DO_CONSUME}", LoreEditConfigUtil.DEFAULT_DO_CONSUME)
            }
        }

    /**
     * Allow usage or removal of color code
     */
    val allowColorCode: Boolean
        get() {
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getBoolean("$rootPath.$ALLOW_COLOUR_CODE", DEFAULT_ALLOW_COLOUR_CODE)
            }
        }

    /**
     * Allow usage or removal of hexadecimal color
     */
    val allowHexColor: Boolean
        get() {
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getBoolean("${rootPath}.$ALLOW_HEX_COLOUR", DEFAULT_ALLOW_HEX_COLOUR)
            }
        }

    /**
     * Allow usage or removal of minimessage on lore add
     */
    val allowMinimessage: Boolean
        get() {
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getBoolean("${rootPath}.$ALLOW_MINIMESSAGE", DEFAULT_ALLOW_MINIMESSAGE)
            }
        }

    /**
     * Cost when using either color code and hex color on lore add
     */
    val useColorCost: Int
        get() {
            if (!isAppend) throw IllegalStateException("Can only call with an append edit type")
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getInt("${rootPath}.$USE_COLOUR_COST", DEFAULT_USE_COLOUR_COST)
                .takeIf { it in USE_COLOUR_COST_RANGE }
                   ?: DEFAULT_USE_COLOUR_COST
            }
        }

    /**
     * Cost when using either color code and hex color on lore remove
     */
    val removeColorCost: Int
        get() {
            if (isAppend) throw IllegalStateException("Can only call with a remove edit type")
            return CONFIG.read.use { lock -> lock.get()
                .config
                .getInt("${rootPath}.$REMOVE_COLOUR_COST", DEFAULT_REMOVE_COLOUR_COST)
                .takeIf { it in REMOVE_COLOUR_COST_RANGE }
                   ?: DEFAULT_REMOVE_COLOUR_COST
            }
        }

}