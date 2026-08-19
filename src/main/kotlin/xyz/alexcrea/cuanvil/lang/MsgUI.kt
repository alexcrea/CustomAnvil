package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.UIMessage as Message

object MsgUI {

    val SHARED_CONFIG_NO_EDIT_PERM = Message("shared.no-permission")
    val SHARED_TYPED_CONFIG_TITLE = Message("shared.typed-config-title", "type")
    val SHARED_CLICK_TO_CHANGE = Message("shared.click-to-change")
    val SHARED_GREEN_GET_ITEM = Message("shared.green-get-item", "name")
    val SHARED_YELLOW_GET_ITEM = Message("shared.yellow-get-item", "name")

    val GLOBAL_ITEM_ITEM_LORE_PREFIX = Message("global-item.item-lore-prefix", "value")
    val GLOBAL_ITEM_ITEM_LORE_PREFIX_ALONE = Message("global-item.item-lore-prefix-alone", "value")

    val CONFIRM_ACTION_FAILED = Message("confirm-action.fail")
    val CONFIRM_ACTION_ARE_YOU_SURE = Message("confirm-action.is-user-sure")

    val SELECT_ITEM_TYPE_PLACE_HERE = Message("select-item-type.place-here")

    val ELEMENT_LIST_INSTRUCTION_NEW = Message("element-list.instruction-new", "type")
    val ELEMENT_LIST_CANCELLED_NEW = Message("element-list.cancelled-new", "type")
    val ELEMENT_LIST_DUPLICATED_NEW = Message("element-list.duplicated-new", "type")

    val UNIT_REPAIR_TITLE = Message("unit-repair.title", null, "page", "max_page")
    val UNIT_REPAIR_ITEM = Message("unit-repair.item", "name", "unit")
    val UNIT_REPAIR_ELEMENT_TITLE = Message("unit-repair.element.title", "type", "page", "max_page")
    val UNIT_REPAIR_NEW_TITLE = Message("unit-repair.new.title", null)
    val UNIT_REPAIR_NEW_DESCRIPTION = Message("unit-repair.new.description", null)

    val UNIT_REPAIR_ELEMENT_VALUE_TITLE = Message("unit-repair.element.value.title", "name", null)
    val UNIT_REPAIR_ELEMENT_VALUE_DESCRIPTION = Message("unit-repair.element.value.description", "name", "unit")

    val UNIT_REPAIR_NEW_ELEMENT_TITLE = Message("unit-repair.element.new.title", null)
    val UNIT_REPAIR_NEW_ELEMENT_DESCRIPTION = Message("unit-repair.element.new.description", "name")
    val UNIT_REPAIR_NEW_ELEMENT_CANNOT_REPAIR = Message("unit-repair.element.new.cannot-damage")
    val UNIT_REPAIR_NEW_ELEMENT_SAME_TYPE = Message("unit-repair.element.new.same-type")

    val CUSTOM_RECIPE_TITLE = Message("custom-recipe.title", null, "page", "max_page")

    val CUSTOM_RECIPE_ELEMENT_EXACT_COUNT_TITLE = Message("custom-recipe.element.exact-count")
    val CUSTOM_RECIPE_ELEMENT_LINEAR_XP_TITLE = Message("custom-recipe.element.linear-xp.title")
    val CUSTOM_RECIPE_ELEMENT_LINEAR_XP_NAME = Message("custom-recipe.element.linear-xp.name")
    val CUSTOM_RECIPE_ELEMENT_LINEAR_XP_LORE = Message("custom-recipe.element.linear-xp.lore")
    val CUSTOM_RECIPE_ELEMENT_COST_LEVEL_XP = Message("custom-recipe.element.recipe-cost.level")
    val CUSTOM_RECIPE_ELEMENT_COST_LINEAR_XP = Message("custom-recipe.element.recipe-cost.xp")

    val CUSTOM_RECIPE_ELEMENT_ITEM_LEFT_TITLE = Message("custom-recipe.element.item.left.title")
    val CUSTOM_RECIPE_ELEMENT_ITEM_LEFT_DESCRIPTION = Message("custom-recipe.element.item.left.description")
    val CUSTOM_RECIPE_ELEMENT_ITEM_RIGHT_TITLE = Message("custom-recipe.element.item.right.title")
    val CUSTOM_RECIPE_ELEMENT_ITEM_RIGHT_DESCRIPTION = Message("custom-recipe.element.item.right.description")
    val CUSTOM_RECIPE_ELEMENT_ITEM_RESULT_TITLE = Message("custom-recipe.element.item.result.title")
    val CUSTOM_RECIPE_ELEMENT_ITEM_RESULT_DESCRIPTION = Message("custom-recipe.element.item.result.description")

    val CUSTOM_RECIPE_ELEMENT_DELETE_TITLE = Message("custom-recipe.element.delete.title", "type")
    val CUSTOM_RECIPE_ELEMENT_DELETE_DESCRIPTION = Message("custom-recipe.element.delete.description", null)
    val CUSTOM_RECIPE_ELEMENT_DELETE_BUTTON_NAME = Message("custom-recipe.element.delete.button.name")
    val CUSTOM_RECIPE_ELEMENT_DELETE_BUTTON_LORE = Message("custom-recipe.element.delete.button.lore")

    val ENCHANTMENT_LEVEL_COST_TITLE = Message("enchant-level-cost.title", null, "page", "max_page")
    val ENCHANTMENT_LEVEL_COST_ELEMENT_TITLE = Message("enchant-level-cost.element.title", "name")
    val ENCHANTMENT_LEVEL_COST_ELEMENT_DESCRIPTION = Message("enchant-level-cost.element.description", "name")
    val ENCHANTMENT_LEVEL_COST_ELEMENT_ITEM_COST = Message("enchant-level-cost.element.item-cost")
    val ENCHANTMENT_LEVEL_COST_ELEMENT_BOOK_COST = Message("enchant-level-cost.element.book-cost")

    val ENCHANTMENT_LEVEL_LIMIT_TITLE = Message("enchant-level-limit.title", null, "page", "max_page")
    val ENCHANTMENT_LEVEL_LIMIT_ELEMENT_TITLE = Message("enchant-level-limit.element.title", "name")
    val ENCHANTMENT_LEVEL_LIMIT_ELEMENT_DESCRIPTION = Message("enchant-level-limit.element.description", "name")

    val ENCHANTMENT_MERGE_LIMIT_TITLE = Message("enchant-merge-limit.title", null, "page", "max_page")
    val ENCHANTMENT_MERGE_LIMIT_ELEMENT_TITLE = Message("enchant-merge-limit.element.title", "name")
    val ENCHANTMENT_MERGE_LIMIT_ELEMENT_DESCRIPTION = Message("enchant-merge-limit.element.description", "name")

    val ENCHANTMENT_CONFLICT_TITLE = Message("enchant-conflict.title", null, "page", "max_page")
    val ENCHANTMENT_CONFLICT_ELEMENT_ENCHANTMENTS = Message("enchant-conflict.element.selected-enchantments", "group")
    val ENCHANTMENT_CONFLICT_ELEMENT_SUB_GROUPS = Message("enchant-conflict.element.selected-sub-groups", "group")

    val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_TITLE = Message("enchant-conflict.element.delete.title", "type")
    val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_DESCRIPTION = Message("enchant-conflict.element.delete.description", null)
    val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_BUTTON_NAME = Message("enchant-conflict.element.delete.button.name")
    val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_BUTTON_LORE = Message("enchant-conflict.element.delete.button.lore")
    val ENCHANTMENT_CONFLICT_ELEMENT_MIN_BEFORE_COUNT_TITLE = Message("enchant-conflict.element.min-before-count.title")
    val ENCHANTMENT_CONFLICT_ELEMENT_MIN_BEFORE_COUNT_DESCRIPTION = Message("enchant-conflict.element.min-before-count.description")
    val ENCHANTMENT_CONFLICT_ELEMENT_MIN_BEFORE_COUNT_ITEM = Message("enchant-conflict.element.min-before-count.item")

    val MATERIAL_GROUP_TITLE = Message("material-group.title", null, "page", "max_page")
    val MATERIAL_GROUP_ELEMENT_SELECTED_MATERIALS = Message("material-group.element.selected-materials", "group")
    val MATERIAL_GROUP_ELEMENT_SELECTED_SUB_GROUPS = Message("material-group.element.selected-sub-groups", "group")

    val MATERIAL_GROUP_ELEMENT_DELETE_TITLE = Message("material-group.element.delete.title", "type")
    val MATERIAL_GROUP_ELEMENT_DELETE_DESCRIPTION = Message("material-group.element.delete.description", null)
    val MATERIAL_GROUP_ELEMENT_DELETE_BUTTON_NAME = Message("material-group.element.delete.button.name")
    val MATERIAL_GROUP_ELEMENT_DELETE_BUTTON_LORE = Message("material-group.element.delete.button.lore")

    val MATERIAL_SELECT_CONFIRM_TITLE = Message("material-select.new.confirm.title", "name")
    val MATERIAL_SELECT_CONFIRM_DESCRIPTION = Message("material-select.new.confirm.description", "name")

    /*
     * ------------------
     *  Basic Config Gui
     * ------------------
     */
    val BASIC_TITLE = Message("basic-config.title")

    val BASIC_CAP_ANVIL_COST_TITLE = Message("basic-config.cap-anvil-cost.title")
    val BASIC_CAP_ANVIL_COST_DESCRIPTION = Message("basic-config.cap-anvil-cost.description")
    val BASIC_CAP_ANVIL_COST_ITEM = Message("basic-config.cap-anvil-cost-cost.item")
    val BASIC_CAP_ANVIL_COST_DISABLED_TITLE = Message("basic-config.cap-anvil-cost.disabled.title")
    val BASIC_CAP_ANVIL_COST_DISABLED_DESCRIPTION = Message("basic-config.cap-anvil-cost.disabled.description")

    val BASIC_MAX_ANVIL_COST_TITLE = Message("basic-config.max-anvil-cost.title")
    val BASIC_MAX_ANVIL_COST_DESCRIPTION = Message("basic-config.max-anvil-cost.description")
    val BASIC_MAX_ANVIL_COST_ITEM = Message("basic-config.max-anvil-cost.item")
    val BASIC_MAX_ANVIL_COST_DISABLED_TITLE = Message("basic-config.max-anvil-cost.disabled.title")
    val BASIC_MAX_ANVIL_COST_DISABLED_DESCRIPTION = Message("basic-config.max-anvil-cost.disabled.description")

    val BASIC_REMOVE_COST_LIMIT_TITLE = Message("basic-config.remove-cost-limit.title")
    val BASIC_REMOVE_COST_LIMIT_DESCRIPTION = Message("basic-config.remove-cost-limit.description")
    val BASIC_REMOVE_COST_LIMIT_ITEM = Message("basic-config.remove-cost-limit.item")

    val BASIC_REPLACE_TOO_EXPENSIVE_TITLE = Message("basic-config.remove-too-expensive.title")
    val BASIC_REPLACE_TOO_EXPENSIVE_DESCRIPTION = Message("basic-config.remove-too-expensive.description")
    val BASIC_REPLACE_TOO_EXPENSIVE_DESCRIPTION_NO_NMS = Message("basic-config.remove-too-expensive.description-no-nms")

    val BASIC_ITEM_REPAIR_COST_TITLE = Message("basic-config.item-repair-cost.title")
    val BASIC_ITEM_REPAIR_COST_DESCRIPTION = Message("basic-config.item-repair-cost.description")

    val BASIC_ITEM_RENAME_COST_TITLE = Message("basic-config.item-rename-cost.title")
    val BASIC_ITEM_RENAME_COST_DESCRIPTION = Message("basic-config.item-rename-cost.description")

    val BASIC_UNIT_REPAIR_COST_TITLE = Message("basic-config.unit-repair-cost.title")
    val BASIC_UNIT_REPAIR_COST_DESCRIPTION = Message("basic-config.unit-repair-cost.description")

    val BASIC_SACRIFICE_ILLEGAL_COST_TITLE = Message("basic-config.sacrifice-illegal-cost.title")
    val BASIC_SACRIFICE_ILLEGAL_COST_DESCRIPTION = Message("basic-config.sacrifice-illegal-cost.description")


    val BASIC_COLOR_CODE_LIMIT_TITLE = Message("basic-config.color-code.title")
    val BASIC_COLOR_CODE_LIMIT_DESCRIPTION = Message("basic-config.color-code.description")

    val BASIC_COLOR_HEX_LIMIT_TITLE = Message("basic-config.color-hex.title")
    val BASIC_COLOR_HEX_LIMIT_DESCRIPTION = Message("basic-config.color-hex.description")

    val BASIC_COLOR_PERMISSION_TITLE = Message("basic-config.color-permission.title")
    val BASIC_COLOR_PERMISSION_DESCRIPTION = Message("basic-config.color-permission.description")
    val BASIC_COLOR_PERMISSION_DISABLED_TITLE = Message("basic-config.color-permission.disabled.title")
    val BASIC_COLOR_PERMISSION_DISABLED_DESCRIPTION = Message("basic-config.color-permission.disabled.description")

    val BASIC_COLOR_COST_TITLE = Message("basic-config.color-cost.title")
    val BASIC_COLOR_COST_DESCRIPTION = Message("basic-config.color-cost.description")
    val BASIC_COLOR_COST_ITEM = Message("basic-config.color-cost.item")
    val BASIC_COLOR_COST_DISABLED_TITLE = Message("basic-config.color-cost.disabled.title")
    val BASIC_COLOR_COST_DISABLED_DESCRIPTION = Message("basic-config.color-cost.disabled.description")

    val BASIC_WORK_PENALTY_TITLE = Message("basic-config.work-penalty.title")
    val BASIC_WORK_PENALTY_ITEM = Message("basic-config.work-penalty.item")
    val BASIC_WORK_PENALTY_LORE = Message("basic-config.work-penalty.lore")
    val BASIC_WORK_PENALTY_LORE_BREAK = Message("basic-config.work-penalty.lore-break")
    val BASIC_WORK_PENALTY_EXPLAIN_INCREASING = Message("basic-config.work-penalty.explanation.increasing")
    val BASIC_WORK_PENALTY_EXPLAIN_ADDITIVE = Message("basic-config.work-penalty.explanation.additive")
    val BASIC_WORK_PENALTY_EXPLAIN_SHARED = Message("basic-config.work-penalty.explanation.shared")
    val BASIC_WORK_PENALTY_EXPLAIN_EXCLUSIVE = Message("basic-config.work-penalty.explanation.exclusive")



}