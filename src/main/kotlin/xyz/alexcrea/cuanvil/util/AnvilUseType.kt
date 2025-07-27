package xyz.alexcrea.cuanvil.util

import org.bukkit.inventory.ItemType
import xyz.alexcrea.cuanvil.config.WorkPenaltyType.WorkPenaltyPart

@Suppress("UnstableApiUsage")
enum class AnvilUseType(
    val typeName: String, val path: String,
    val defaultPenalty: WorkPenaltyPart,
    val displayName: String, val displayMat: ItemType
) {

    RENAME_ONLY(
        "rename_only",
        WorkPenaltyPart(false, true),
        "Rename Only", ItemType.NAME_TAG
    ),
    MERGE(
        "merge",
        WorkPenaltyPart(true, true),
        "Merge", ItemType.ANVIL
    ),
    UNIT_REPAIR(
        "unit_repair",
        WorkPenaltyPart(true, true),
        "Unit Repair", ItemType.DIAMOND
    ),
    CUSTOM_CRAFT(
        "custom_craft",
        WorkPenaltyPart(false, false),
        "Custom Craft", ItemType.CRAFTING_TABLE
    ),
    LORE_EDIT_BOOK_APPEND(
        "lore_edit_book_append", "lore_edit.book_and_quil.append",
        WorkPenaltyPart(false, false),
        "Book Add", ItemType.WRITABLE_BOOK
    ),
    LORE_EDIT_BOOK_REMOVE(
        "lore_edit_book_remove", "lore_edit.book_and_quil.remove",
        WorkPenaltyPart(false, false),
        "Book Remove", ItemType.WRITABLE_BOOK
    ),
    LORE_EDIT_PAPER_APPEND(
        "lore_edit_paper_append", "lore_edit.paper.append_line",
        WorkPenaltyPart(false, false),
        "Paper Add", ItemType.WRITABLE_BOOK
    ),
    LORE_EDIT_PAPER_REMOVE(
        "lore_edit_paper_remove", "lore_edit.paper.remove_line",
        WorkPenaltyPart(false, false),
        "Paper Remove", ItemType.WRITABLE_BOOK
    ),
    ;

    constructor(
        typeName: String,
        defaultPenalty: WorkPenaltyPart,
        displayName: String, displayMat: ItemType
    ) :
            this(
                typeName,
                AnvilUseTypeUtil.defaultPath(typeName), // stupid util class
                defaultPenalty,
                displayName, displayMat
            )

}