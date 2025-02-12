package xyz.alexcrea.cuanvil.util

import org.bukkit.Material
import xyz.alexcrea.cuanvil.config.WorkPenaltyType.WorkPenaltyPart

enum class AnvilUseType(val typeName: String,
                        val defaultPenalty: WorkPenaltyPart,
                        val displayName: String, val displayMat: Material
    ) {

    RENAME_ONLY("rename_only",
        WorkPenaltyPart(false, true),
        "Rename Only", Material.NAME_TAG
        ),
    MERGE("merge",
        WorkPenaltyPart(true, true),
        "Merge", Material.ANVIL
    ),
    UNIT_REPAIR("unit_repair",
        WorkPenaltyPart(true, true),
        "Unit Repair", Material.DIAMOND
    ),
    CUSTOM_CRAFT("custom_craft",
        WorkPenaltyPart(false, false),
        "Custom Craft", Material.CRAFTING_TABLE
    ),
    ;

}