package xyz.alexcrea.cuanvil.anvil

import org.bukkit.Material
import xyz.alexcrea.cuanvil.config.WorkPenaltyType
import xyz.alexcrea.cuanvil.util.anvil.AnvilUseTypeUtil

enum class AnvilUseType(
    val typeName: String, val path: String,
    val defaultPenalty: WorkPenaltyType.WorkPenaltyPart,
    val displayName: String, val displayMat: Material
) {

    RENAME_ONLY(
        "rename_only",
        WorkPenaltyType.WorkPenaltyPart(false, true),
        "Rename Only", Material.NAME_TAG
    ),
    MERGE(
        "merge",
        WorkPenaltyType.WorkPenaltyPart(true, true),
        "Merge", Material.ANVIL
    ),
    UNIT_REPAIR(
        "unit_repair",
        WorkPenaltyType.WorkPenaltyPart(true, true),
        "Unit Repair", Material.DIAMOND
    ),
    CUSTOM_CRAFT(
        "custom_craft",
        WorkPenaltyType.WorkPenaltyPart(false, false),
        "Custom Craft", Material.CRAFTING_TABLE
    ),
    LORE_EDIT_BOOK_APPEND(
        "lore_edit_book_append", "lore_edit.book_and_quil.append",
        WorkPenaltyType.WorkPenaltyPart(false, false),
        "Book Add", Material.WRITABLE_BOOK
    ),
    LORE_EDIT_BOOK_REMOVE(
        "lore_edit_book_remove", "lore_edit.book_and_quil.remove",
        WorkPenaltyType.WorkPenaltyPart(false, false),
        "Book Remove", Material.WRITABLE_BOOK
    ),
    LORE_EDIT_PAPER_APPEND(
        "lore_edit_paper_append", "lore_edit.paper.append_line",
        WorkPenaltyType.WorkPenaltyPart(false, false),
        "Paper Add", Material.WRITABLE_BOOK
    ),
    LORE_EDIT_PAPER_REMOVE(
        "lore_edit_paper_remove", "lore_edit.paper.remove_line",
        WorkPenaltyType.WorkPenaltyPart(false, false),
        "Paper Remove", Material.WRITABLE_BOOK
    ),
    ;

    constructor(
        typeName: String,
        defaultPenalty: WorkPenaltyType.WorkPenaltyPart,
        displayName: String, displayMat: Material
    ) :
            this(
                typeName,
                AnvilUseTypeUtil.defaultPath(typeName), // stupid util class
                defaultPenalty,
                displayName, displayMat
            )

}