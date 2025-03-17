package xyz.alexcrea.cuanvil.util

import org.bukkit.Material
import xyz.alexcrea.cuanvil.config.WorkPenaltyType.WorkPenaltyPart
import xyz.alexcrea.cuanvil.util.config.LoreEditType

enum class AnvilUseType(
    val typeName: String, val path: String,
    val defaultPenalty: WorkPenaltyPart,
    val displayName: String, val displayMat: Material
) {

    RENAME_ONLY(
        "rename_only",
        WorkPenaltyPart(false, true),
        "Rename Only", Material.NAME_TAG
    ),
    MERGE(
        "merge",
        WorkPenaltyPart(true, true),
        "Merge", Material.ANVIL
    ),
    UNIT_REPAIR(
        "unit_repair",
        WorkPenaltyPart(true, true),
        "Unit Repair", Material.DIAMOND
    ),
    CUSTOM_CRAFT(
        "custom_craft",
        WorkPenaltyPart(false, false),
        "Custom Craft", Material.CRAFTING_TABLE
    ),
    LORE_EDIT_BOOK_APPEND(
        "lore_edit_book_append", LoreEditType.APPEND_BOOK.rootPath,
        WorkPenaltyPart(false, false),
        "Book Add", Material.WRITABLE_BOOK
    ),
    LORE_EDIT_BOOK_REMOVE(
        "lore_edit_book_remove", LoreEditType.REMOVE_BOOK.rootPath,
        WorkPenaltyPart(false, false),
        "Book Remove", Material.WRITABLE_BOOK
    ),
    LORE_EDIT_PAPER_APPEND(
        "lore_edit_paper_append", LoreEditType.APPEND_PAPER.rootPath,
        WorkPenaltyPart(false, false),
        "Paper Add", Material.WRITABLE_BOOK
    ),
    LORE_EDIT_PAPER_REMOVE(
        "lore_edit_paper_remove", LoreEditType.REMOVE_PAPER.rootPath,
        WorkPenaltyPart(false, false),
        "Paper Remove", Material.WRITABLE_BOOK
    ),
    ;

    constructor(
        typeName: String,
        defaultPenalty: WorkPenaltyPart,
        displayName: String, displayMat: Material
    ) :
            this(
                typeName,
                AnvilUseTypeUtil.defaultPath(typeName), // stupid util class
                defaultPenalty,
                displayName, displayMat
            )

}