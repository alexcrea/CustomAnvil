package xyz.alexcrea.cuanvil.util.config

import xyz.alexcrea.cuanvil.util.AnvilUseType
import xyz.alexcrea.cuanvil.config.ConfigHolder.DEFAULT_CONFIG as CONFIG

enum class LoreEditType(
    val rootPath: String,
    val useType: AnvilUseType,
    val isAppend: Boolean,
    val isMultiLine: Boolean,
) {
    APPEND_BOOK("lore_edit.book_and_quil.append", AnvilUseType.LORE_EDIT_BOOK_APPEND, true, true),
    REMOVE_BOOK("lore_edit.book_and_quil.remove", AnvilUseType.LORE_EDIT_BOOK_REMOVE,false, true),
    APPEND_PAPER("lore_edit.paper.append_line", AnvilUseType.LORE_EDIT_PAPER_APPEND,true, false),
    REMOVE_PAPER("lore_edit.paper.remove_line", AnvilUseType.LORE_EDIT_PAPER_REMOVE,false, false),
    ;

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
            if (!isAppend) throw IllegalStateException("Lore edit Consume test should not happen on append")
            return CONFIG
                .config
                .getBoolean("${rootPath}.${LoreEditConfigUtil.DO_CONSUME}", LoreEditConfigUtil.DEFAULT_DO_CONSUME)
        }

}