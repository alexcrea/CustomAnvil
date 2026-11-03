package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.UIMessage as Message

object MsgUI {

    @JvmField val SHARED_CONFIG_NO_EDIT_PERM = Message("shared.no-permission")
    @JvmField val SHARED_TYPED_CONFIG_TITLE = Message("shared.typed-config-title", "type")
    @JvmField val SHARED_CLICK_TO_CHANGE = Message("shared.click-to-change")
    @JvmField val SHARED_GREEN_GET_ITEM = Message("shared.green-get-item", "name")
    @JvmField val SHARED_YELLOW_GET_ITEM = Message("shared.yellow-get-item", "name")

    @JvmField val SHARED_FORMATED_YES = Message("shared.formated-yes")
    @JvmField val SHARED_FORMATED_NO = Message("shared.formated-no")
    @JvmField val SHARED_DEFAULT = Message("shared.default")
    @JvmField val SHARED_VALUED_DEFAULT = Message("shared.valued-default", "value")

    @JvmField val SHARED_BACK_ITEM_TITLE = Message("shared.back-item-title")
    @JvmField val SHARED_SAVE_ITEM_TITLE = Message("shared.save-item-title")
    @JvmField val SHARED_NO_CHANGE_ITEM_TITLE = Message("shared.no-change-item-title")

    @JvmField val SHARED_TEMPORARY_CLOSE_TITLE = Message("shared.temporary-close.title")
    @JvmField val SHARED_TEMPORARY_CLOSE_LORE = Message("shared.temporary-close.lore")
    @JvmField val SHARED_TEMPORARY_CLOSE_RETURN = Message("shared.temporary-close.return")

    @JvmField val SHARED_CANCEL_TITLE = Message("shared.cancel.title")
    @JvmField val SHARED_CANCEL_LORE = Message("shared.cancel.lore")

    @JvmField val SHARED_CONFIRM_TITLE = Message("shared.confirm.title")
    @JvmField val SHARED_CONFIRM_LORE = Message("shared.confirm.lore")
    @JvmField val SHARED_CONFIRM_PERMANENT_LORE = Message("shared.confirm.permanent-lore")

    @JvmField val SHARED_QUIT = Message("shared.quit")
    @JvmField val SHARED_RESET = Message("shared.reset")
    @JvmField val SHARED_RESET_DEFAULT_LORE = Message("shared.reset-default-lore", "value")
    @JvmField val SHARED_EMPTY_ITEM = Message("shared.empty-item")

    fun booleanMessage(bool: Boolean): Message {
        return if(bool) SHARED_FORMATED_YES else SHARED_FORMATED_NO
    }

    @JvmField val GLOBAL_ITEM_ITEM_LORE_PREFIX = Message("global-item.item-lore-prefix", "value")
    @JvmField val GLOBAL_ITEM_ITEM_LORE_PREFIX_ALONE = Message("global-item.item-lore-prefix-alone", "value")

    @JvmField val CONFIRM_ACTION_FAILED = Message("confirm-action.fail")
    @JvmField val CONFIRM_ACTION_ARE_YOU_SURE = Message("confirm-action.is-user-sure")

    @JvmField val SELECT_ITEM_TYPE_PLACE_HERE = Message("select-item-type.place-here")

    @JvmField val ELEMENT_LIST_INSTRUCTION_NEW = Message("element-list.new.instruction", "type")
    @JvmField val ELEMENT_LIST_CANCELLED_NEW = Message("element-list.cancelled-new", "type")
    @JvmField val ELEMENT_LIST_DUPLICATED_NEW = Message("element-list.duplicated-new", "type")
    @JvmField val ELEMENT_LIST_GO_LEFT = Message("element-list.go-left", "page")
    @JvmField val ELEMENT_LIST_GO_RIGHT = Message("element-list.go-right", "page")
    @JvmField val ELEMENT_LIST_NEW_TITLE = Message("element-list.new.title", "type")
    @JvmField val ELEMENT_LIST_NEW_LORE = Message("element-list.new.lore", "type")

    @JvmField val UNIT_REPAIR_TITLE = Message("unit-repair.title", null, "page", "max_page")
    @JvmField val UNIT_REPAIR_ITEM = Message("unit-repair.item", "name", "unit")
    @JvmField val UNIT_REPAIR_ELEMENT_TITLE = Message("unit-repair.element.title", "type", "page", "max_page")
    @JvmField val UNIT_REPAIR_ELEMENT_NAME = Message("unit-repair.element.name", "name")
    @JvmField val UNIT_REPAIR_ELEMENT_LORE = Message("unit-repair.element.lore", "name", "count")

    @JvmField val UNIT_REPAIR_NEW_TITLE = Message("unit-repair.new.title", null)
    @JvmField val UNIT_REPAIR_NEW_NAME = Message("unit-repair.new.name", null)
    @JvmField val UNIT_REPAIR_NEW_LORE = Message("unit-repair.new.lore", null)
    @JvmField val UNIT_REPAIR_NEW_DESCRIPTION = Message("unit-repair.new.description", null)

    @JvmField val UNIT_REPAIR_ELEMENT_VALUE_TITLE = Message("unit-repair.element.value.title", "name", null)
    @JvmField val UNIT_REPAIR_ELEMENT_VALUE_DESCRIPTION = Message("unit-repair.element.value.description", "name", "unit")

    @JvmField val UNIT_REPAIR_NEW_ELEMENT_TITLE = Message("unit-repair.element.new.title", null)
    @JvmField val UNIT_REPAIR_NEW_ELEMENT_DESCRIPTION = Message("unit-repair.element.new.description", "name")
    @JvmField val UNIT_REPAIR_NEW_ELEMENT_NAME = Message("unit-repair.element.new.name", "name")
    @JvmField val UNIT_REPAIR_NEW_ELEMENT_LORE = Message("unit-repair.element.new.lore")
    @JvmField val UNIT_REPAIR_NEW_ELEMENT_CANNOT_REPAIR = Message("unit-repair.element.new.cannot-damage")
    @JvmField val UNIT_REPAIR_NEW_ELEMENT_SAME_TYPE = Message("unit-repair.element.new.same-type")

    @JvmField val CUSTOM_RECIPE_TITLE = Message("custom-recipe.title", null, "page", "max_page")
    @JvmField val CUSTOM_RECIPE_NAME = Message("custom-recipe.name", "name")
    @JvmField val CUSTOM_RECIPE_GENERIC_NAME = Message("custom-recipe.generic-name")
    @JvmField val CUSTOM_RECIPE_LORE_DEFAULT = Message("custom-recipe.lore.default",
        "should_work", "exact_count", "per_craft_lv_cost", "per_craft_xp_cost")
    @JvmField val CUSTOM_RECIPE_LORE_LINEAR = Message("custom-recipe.lore.linear", "exact_linear")

    @JvmField val CUSTOM_RECIPE_ELEMENT_EXACT_COUNT_TITLE = Message("custom-recipe.element.exact-count", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_LINEAR_XP_TITLE = Message("custom-recipe.element.linear-xp.title", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_LINEAR_XP_NAME = Message("custom-recipe.element.linear-xp.name")
    @JvmField val CUSTOM_RECIPE_ELEMENT_LINEAR_XP_LORE = Message("custom-recipe.element.linear-xp.lore")
    @JvmField val CUSTOM_RECIPE_ELEMENT_COST_LEVEL_XP = Message("custom-recipe.element.recipe-cost.level", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_COST_LINEAR_XP = Message("custom-recipe.element.recipe-cost.xp", null)

    @JvmField val CUSTOM_RECIPE_ELEMENT_ITEM_LEFT_TITLE = Message("custom-recipe.element.item.left.title", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_ITEM_LEFT_DESCRIPTION = Message("custom-recipe.element.item.left.description", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_ITEM_RIGHT_TITLE = Message("custom-recipe.element.item.right.title", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_ITEM_RIGHT_DESCRIPTION = Message("custom-recipe.element.item.right.description", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_ITEM_RESULT_TITLE = Message("custom-recipe.element.item.result.title", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_ITEM_RESULT_DESCRIPTION = Message("custom-recipe.element.item.result.description", null)

    @JvmField val CUSTOM_RECIPE_ELEMENT_DELETE_TITLE = Message("custom-recipe.element.delete.title", "type")
    @JvmField val CUSTOM_RECIPE_ELEMENT_DELETE_DESCRIPTION = Message("custom-recipe.element.delete.description", null)
    @JvmField val CUSTOM_RECIPE_ELEMENT_DELETE_BUTTON_NAME = Message("custom-recipe.element.delete.button.name")
    @JvmField val CUSTOM_RECIPE_ELEMENT_DELETE_BUTTON_LORE = Message("custom-recipe.element.delete.button.lore")

    @JvmField val ENCHANTMENT_LEVEL_COST_TITLE = Message("enchant-level-cost.title", null, "page", "max_page")
    @JvmField val ENCHANTMENT_LEVEL_COST_ELEMENT_TITLE = Message("enchant-level-cost.element.title", "name")
    @JvmField val ENCHANTMENT_LEVEL_COST_ELEMENT_DESCRIPTION = Message("enchant-level-cost.element.description", "name")
    @JvmField val ENCHANTMENT_LEVEL_COST_ELEMENT_ITEM_COST = Message("enchant-level-cost.element.item-cost", "cost")
    @JvmField val ENCHANTMENT_LEVEL_COST_ELEMENT_BOOK_COST = Message("enchant-level-cost.element.book-cost", "cost")

    @JvmField val ENCHANTMENT_LEVEL_LIMIT_TITLE = Message("enchant-level-limit.title", null, "page", "max_page")
    @JvmField val ENCHANTMENT_LEVEL_LIMIT_ELEMENT_TITLE = Message("enchant-level-limit.element.title", "name")
    @JvmField val ENCHANTMENT_LEVEL_LIMIT_ELEMENT_DESCRIPTION = Message("enchant-level-limit.element.description", "name")

    @JvmField val ENCHANTMENT_MERGE_LIMIT_TITLE = Message("enchant-merge-limit.title", null, "page", "max_page")
    @JvmField val ENCHANTMENT_MERGE_LIMIT_ELEMENT_TITLE = Message("enchant-merge-limit.element.title", "name")
    @JvmField val ENCHANTMENT_MERGE_LIMIT_ELEMENT_DESCRIPTION = Message("enchant-merge-limit.element.description", "name")

    @JvmField val ENCHANTMENT_CONFLICT_TITLE = Message("enchant-conflict.title", null, "page", "max_page")
    @JvmField val ENCHANTMENT_CONFLICT_GENERIC_NAME = Message("enchant-conflict.generic-name")
    @JvmField val ENCHANTMENT_CONFLICT_NAME = Message("enchant-conflict.name", "name")
    @JvmField val ENCHANTMENT_CONFLICT_LORE = Message("enchant-conflict.lore", "enchantment_count", "group_count", "min_count")
    @JvmField val ENCHANTMENT_CONFLICT_DEFAULT_NEW = Message("enchant-conflict.default-new")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_ENCHANTMENTS = Message("enchant-conflict.element.selected-enchantments", "group")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_SUB_GROUPS = Message("enchant-conflict.element.selected-sub-groups", "group")

    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_TITLE = Message("enchant-conflict.element.delete.title", "type")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_DESCRIPTION = Message("enchant-conflict.element.delete.description", null)
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_BUTTON_NAME = Message("enchant-conflict.element.delete.button.name")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_BUTTON_LORE = Message("enchant-conflict.element.delete.button.lore")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_MIN_BEFORE_COUNT_TITLE = Message("enchant-conflict.element.min-before-count.title", null)
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_MIN_BEFORE_COUNT_DESCRIPTION = Message("enchant-conflict.element.min-before-count.description", null)
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_MIN_BEFORE_COUNT_ITEM = Message("enchant-conflict.element.min-before-count.item")

    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_TITLE = Message("enchant-conflict.element.enchantments.title")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_LORE_HEADER = Message("enchant-conflict.element.enchantments.lore.header")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_LORE_EMPTY = Message("enchant-conflict.element.enchantments.lore.empty")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_LORE_NOT_EMPTY = Message("enchant-conflict.element.enchantments.lore.not-empty")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_LORE_ITEM = Message("enchant-conflict.element.enchantments.lore.item", "name")
    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_LORE_AND_MORE = Message("enchant-conflict.element.enchantments.lore.and-more", "count")

    @JvmField val ENCHANTMENT_CONFLICT_ELEMENT_EXCLUDED_GROUPS_TITLE = Message("enchant-conflict.element.excluded-groups.title")

    @JvmField val MATERIAL_GROUP_TITLE = Message("material-group.title", null, "page", "max_page")
    @JvmField val MATERIAL_GROUP_GENERIC_NAME = Message("material-group.generic-name")
    @JvmField val MATERIAL_GROUP_NAME = Message("material-group.name", "name")
    @JvmField val MATERIAL_GROUP_LORE = Message("material-group.lore", "groups", "materials", "size")
    @JvmField val MATERIAL_GROUP_ELEMENT_SELECTED_MATERIALS = Message("material-group.element.selected-materials", "group", null, null)
    @JvmField val MATERIAL_GROUP_ELEMENT_SELECTED_SUB_GROUPS = Message("material-group.element.selected-sub-groups", "group")

    @JvmField val MATERIAL_GROUP_ELEMENT_DELETE_TITLE = Message("material-group.element.delete.title", "type")
    @JvmField val MATERIAL_GROUP_ELEMENT_DELETE_DESCRIPTION = Message("material-group.element.delete.description", null)
    @JvmField val MATERIAL_GROUP_ELEMENT_DELETE_BUTTON_NAME = Message("material-group.element.delete.button.name")
    @JvmField val MATERIAL_GROUP_ELEMENT_DELETE_BUTTON_LORE = Message("material-group.element.delete.button.lore")

    @JvmField val MATERIAL_GROUP_ELEMENT_USED_HEADER = Message("material-group.element.used.header", "group")
    @JvmField val MATERIAL_GROUP_ELEMENT_USED_ITEM = Message("material-group.element.used.item", "name")
    @JvmField val MATERIAL_GROUP_ELEMENT_USED_AND_MORE = Message("material-group.element.used.and-more", "count")
    @JvmField val MATERIAL_GROUP_ELEMENT_USED_GROUP = Message("material-group.element.used.group", "group")
    @JvmField val MATERIAL_GROUP_ELEMENT_USED_CONFLICT = Message("material-group.element.used.conflict", "conflict")

    @JvmField val MATERIAL_GROUP_ELEMENT_SELECT_MATERIALS = Message("material-group.element.select.materials")
    @JvmField val MATERIAL_GROUP_ELEMENT_SELECT_GROUPS = Message("material-group.element.select.groups")

    @JvmField val MATERIAL_SELECT_CONFIRM_TITLE = Message("material-select.new.confirm.title", "name")
    @JvmField val MATERIAL_SELECT_CONFIRM_DESCRIPTION = Message("material-select.new.confirm.description", "name")
    @JvmField val MATERIAL_SELECT_ADD_NAME = Message("material-select.add.name")
    @JvmField val MATERIAL_SELECT_ADD_LORE = Message("material-select.add.lore")

    @JvmField val MATERIAL_SELECT_INSTA_REMOVE_ENABLED_NAME = Message("material-select.insta-remove.enabled.name")
    @JvmField val MATERIAL_SELECT_INSTA_REMOVE_ENABLED_LORE = Message("material-select.insta-remove.enabled.lore")
    @JvmField val MATERIAL_SELECT_INSTA_REMOVE_DISABLED_NAME = Message("material-select.insta-remove.disabled.name")
    @JvmField val MATERIAL_SELECT_INSTA_REMOVE_DISABLED_LORE = Message("material-select.insta-remove.disabled.lore")

    @JvmField val MATERIAL_SELECT_ITEM_TITLE = Message("material-select.item.name", "name")
    @JvmField val MATERIAL_SELECT_ITEM_LORE = Message("material-select.item.lore")

    @JvmField val ENCHANT_CONFIG_TITLE = Message("enchant-config.title", "name")
    @JvmField val ENCHANT_CONFIG_NAME = Message("enchant-config.name", "name")
    @JvmField val ENCHANT_CONFIG_MULTIPLES_NAME = Message("enchant-config.multiples-name")

    @JvmField val ITEM_CONFIG_TITLE = Message("item-config.title", "name")
    @JvmField val ITEM_CONFIG_NAME = Message("item-config.name", "name")

    /*
     * ------------------
     *  Basic Config Gui
     * ------------------
     */
    @JvmField val MAIN_TITLE = Message("main.title")
    @JvmField val MAIN_BASIC_TITLE = Message("main.basic.title")
    @JvmField val MAIN_BASIC_LORE = Message("main.basic.lore")
    @JvmField val MAIN_ENCHANT_LEVEL_LIMIT_TITLE = Message("main.enchant-level-limit.title")
    @JvmField val MAIN_ENCHANT_LEVEL_LIMIT_LORE = Message("main.enchant-level-limit.lore")
    @JvmField val MAIN_ENCHANT_MERGE_LIMIT_TITLE = Message("main.enchant-merge-limit.title")
    @JvmField val MAIN_ENCHANT_MERGE_LIMIT_LORE = Message("main.enchant-merge-limit.lore")
    @JvmField val MAIN_ENCHANT_COST_TITLE = Message("main.enchant-cost.title")
    @JvmField val MAIN_ENCHANT_COST_LORE = Message("main.enchant-cost.lore")
    @JvmField val MAIN_ENCHANT_CONFLICT_TITLE = Message("main.enchant-conflict.title")
    @JvmField val MAIN_ENCHANT_CONFLICT_LORE = Message("main.enchant-conflict.lore")
    @JvmField val MAIN_ITEM_GROUP_TITLE = Message("main.item-group.title")
    @JvmField val MAIN_ITEM_GROUP_LORE = Message("main.item-group.lore")
    @JvmField val MAIN_UNIT_REPAIR_TITLE = Message("main.unit-repair.title")
    @JvmField val MAIN_UNIT_REPAIR_LORE = Message("main.unit-repair.lore")
    @JvmField val MAIN_CUSTOM_RECIPE_TITLE = Message("main.custom-recipe.title")
    @JvmField val MAIN_CUSTOM_RECIPE_LORE = Message("main.custom-recipe.lore")

    @JvmField val BASIC_TITLE = Message("basic-config.title")

    @JvmField val BASIC_CAP_ANVIL_COST_TITLE = Message("basic-config.cap-anvil-cost.title", null)
    @JvmField val BASIC_CAP_ANVIL_COST_DESCRIPTION = Message("basic-config.cap-anvil-cost.description", null)
    @JvmField val BASIC_CAP_ANVIL_COST_ITEM = Message("basic-config.cap-anvil-cost.item")
    @JvmField val BASIC_CAP_ANVIL_COST_DISABLED_TITLE = Message("basic-config.cap-anvil-cost.disabled.title")
    @JvmField val BASIC_CAP_ANVIL_COST_DISABLED_DESCRIPTION = Message("basic-config.cap-anvil-cost.disabled.description")

    @JvmField val BASIC_MAX_ANVIL_COST_TITLE = Message("basic-config.max-anvil-cost.title", null)
    @JvmField val BASIC_MAX_ANVIL_COST_DESCRIPTION = Message("basic-config.max-anvil-cost.description", null)
    @JvmField val BASIC_MAX_ANVIL_COST_ITEM = Message("basic-config.max-anvil-cost.item")
    @JvmField val BASIC_MAX_ANVIL_COST_DISABLED_TITLE = Message("basic-config.max-anvil-cost.disabled.title")
    @JvmField val BASIC_MAX_ANVIL_COST_DISABLED_DESCRIPTION = Message("basic-config.max-anvil-cost.disabled.description")

    @JvmField val BASIC_REMOVE_COST_LIMIT_TITLE = Message("basic-config.remove-cost-limit.title", null)
    @JvmField val BASIC_REMOVE_COST_LIMIT_DESCRIPTION = Message("basic-config.remove-cost-limit.description", null)
    @JvmField val BASIC_REMOVE_COST_LIMIT_ITEM = Message("basic-config.remove-cost-limit.item")

    @JvmField val BASIC_REPLACE_TOO_EXPENSIVE_TITLE = Message("basic-config.remove-too-expensive.title", null)
    @JvmField val BASIC_REPLACE_TOO_EXPENSIVE_DESCRIPTION = Message("basic-config.remove-too-expensive.description", null)
    @JvmField val BASIC_REPLACE_TOO_EXPENSIVE_DESCRIPTION_NO_NMS = Message("basic-config.remove-too-expensive.description-no-nms")

    @JvmField val BASIC_ITEM_REPAIR_COST_TITLE = Message("basic-config.item-repair-cost.title", null)
    @JvmField val BASIC_ITEM_REPAIR_COST_DESCRIPTION = Message("basic-config.item-repair-cost.description", null)

    @JvmField val BASIC_ITEM_RENAME_COST_TITLE = Message("basic-config.item-rename-cost.title", null)
    @JvmField val BASIC_ITEM_RENAME_COST_DESCRIPTION = Message("basic-config.item-rename-cost.description", null)

    @JvmField val BASIC_UNIT_REPAIR_COST_TITLE = Message("basic-config.unit-repair-cost.title", null)
    @JvmField val BASIC_UNIT_REPAIR_COST_DESCRIPTION = Message("basic-config.unit-repair-cost.description", null)

    @JvmField val BASIC_SACRIFICE_ILLEGAL_COST_TITLE = Message("basic-config.sacrifice-illegal-cost.title", null)
    @JvmField val BASIC_SACRIFICE_ILLEGAL_COST_DESCRIPTION = Message("basic-config.sacrifice-illegal-cost.description", null)


    @JvmField val BASIC_COLOUR_CODE_LIMIT_TITLE = Message("basic-config.colour-code.title", null)
    @JvmField val BASIC_COLOUR_CODE_LIMIT_DESCRIPTION = Message("basic-config.colour-code.description", null)

    @JvmField val BASIC_COLOUR_HEX_LIMIT_TITLE = Message("basic-config.colour-hex.title", null)
    @JvmField val BASIC_COLOUR_HEX_LIMIT_DESCRIPTION = Message("basic-config.colour-hex.description", null)

    @JvmField val BASIC_COLOUR_PERMISSION_TITLE = Message("basic-config.colour-permission.title", null)
    @JvmField val BASIC_COLOUR_PERMISSION_DESCRIPTION = Message("basic-config.colour-permission.description", null)
    @JvmField val BASIC_COLOUR_PERMISSION_DISABLED_TITLE = Message("basic-config.colour-permission.disabled.title")
    @JvmField val BASIC_COLOUR_PERMISSION_DISABLED_DESCRIPTION = Message("basic-config.colour-permission.disabled.description")

    @JvmField val BASIC_COLOUR_COST_TITLE = Message("basic-config.colour-cost.title", null)
    @JvmField val BASIC_COLOUR_COST_DESCRIPTION = Message("basic-config.colour-cost.description", null)
    @JvmField val BASIC_COLOUR_COST_ITEM = Message("basic-config.colour-cost.item")
    @JvmField val BASIC_COLOUR_COST_DISABLED_TITLE = Message("basic-config.colour-cost.disabled.title")
    @JvmField val BASIC_COLOUR_COST_DISABLED_DESCRIPTION = Message("basic-config.colour-cost.disabled.description")

    @JvmField val BASIC_WORK_PENALTY_TITLE = Message("basic-config.work-penalty.title")
    @JvmField val BASIC_WORK_PENALTY_ITEM = Message("basic-config.work-penalty.item")
    @JvmField val BASIC_WORK_PENALTY_LORE = Message("basic-config.work-penalty.lore")
    @JvmField val BASIC_WORK_PENALTY_LORE_BREAK = Message("basic-config.work-penalty.lore-break")
    @JvmField val BASIC_WORK_PENALTY_EXPLAIN_INCREASING = Message("basic-config.work-penalty.explanation.increasing")
    @JvmField val BASIC_WORK_PENALTY_EXPLAIN_ADDITIVE = Message("basic-config.work-penalty.explanation.additive")
    @JvmField val BASIC_WORK_PENALTY_EXPLAIN_SHARED = Message("basic-config.work-penalty.explanation.shared")
    @JvmField val BASIC_WORK_PENALTY_EXPLAIN_EXCLUSIVE = Message("basic-config.work-penalty.explanation.exclusive")

    @JvmField val WORK_PENALTY_INCREASING_ENABLED = Message("basic-config.work-penalty.penalty-increasing.enabled")
    @JvmField val WORK_PENALTY_INCREASING_DISABLED = Message("basic-config.work-penalty.penalty-increasing.disabled")
    @JvmField val WORK_PENALTY_ADDITIVE_ENABLED = Message("basic-config.work-penalty.penalty-additive.enabled")
    @JvmField val WORK_PENALTY_ADDITIVE_DISABLED = Message("basic-config.work-penalty.penalty-additive.disabled")
    @JvmField val WORK_PENALTY_LORE_SHARED = Message("basic-config.work-penalty.lore-shared", "additive", "increasing")
    @JvmField val WORK_PENALTY_LORE_EXCLUSIVE = Message("basic-config.work-penalty.lore-exclusive", "additive", "increasing")
    @JvmField val WORK_PENALTY_TYPE_NAME = Message("basic-config.work-penalty.type-name", "name")

    @JvmField val SETTING_VALUE_NAME = Message("setting.value-name", "value")
    @JvmField val SETTING_VALUE_ARROW_NAME = Message("setting.value-arrow-name", "now", "planned", "delta")
    @JvmField val SETTING_VALUE_ARROW_NAME_PREFIXED = Message("setting.value-arrow-name-prefixed", "now", "planned", "prefix", "delta")
    @JvmField val SETTING_STEP_NAME_SELECTED = Message("setting.step-name-selected", "value")
    @JvmField val SETTING_STEP_NAME_OTHER = Message("setting.step-name-other", "value")
    @JvmField val SETTING_STEP_LORE_CHANGE = Message("setting.step-lore-change", "value")
    @JvmField val SETTING_STEP_LORE_CLICK = Message("setting.step-lore-click", "value")
    @JvmField val SETTING_SELECTED = Message("setting.selected")
    @JvmField val SETTING_NOT_SELECTED = Message("setting.not-selected")
    @JvmField val SETTING_SELECTED_NAME = Message("setting.selected-name", "name")
    @JvmField val SETTING_UNSELECTED_NAME = Message("setting.unselected-name", "name")

    @JvmField val BOOL_SETTING_DEFAULT_YES = Message("bool-setting.default-value.yes")
    @JvmField val BOOL_SETTING_DEFAULT_NO = Message("bool-setting.default-value.no")

    @JvmField val DOUBLE_SETTING_DELETE_NAME = Message("double-setting.delete-name")
    @JvmField val DOUBLE_SETTING_DELETE_LORE = Message("double-setting.delete-lore")

    @JvmField val ITEM_SETTING_CLICK_WITH_ITEM = Message("item-setting.click-with-item")
    @JvmField val ITEM_SETTING_NO_ITEM_NAME = Message("item-setting.no-item-name")
    @JvmField val ITEM_SETTING_NAME = Message("item-setting.name", "name")

    @JvmField val ENCHANT_COST_BOOK_NAME = Message("enchant-cost-setting.book-name")
    @JvmField val ENCHANT_COST_BOOK_LORE = Message("enchant-cost-setting.book-lore")
    @JvmField val ENCHANT_COST_ITEM_NAME = Message("enchant-cost-setting.item-name")
    @JvmField val ENCHANT_COST_ITEM_LORE = Message("enchant-cost-setting.item-lore")
    @JvmField val ENCHANT_COST_DEFAULT_ITEM = Message("enchant-cost-setting.default-item", "value")
    @JvmField val ENCHANT_COST_DEFAULT_BOOK = Message("enchant-cost-setting.default-book", "value")

    @JvmField val ENCHANT_SELECT_SHOW_ALL_NAME = Message("enchant-select-setting.show-all-name")
    @JvmField val ENCHANT_SELECT_SHOW_SELECTED_NAME = Message("enchant-select-setting.show-selected-name")
    @JvmField val ENCHANT_SELECT_TOGGLE_LORE_ONLY_SELECTED = Message("enchant-select-setting.show-toggle-lore-only-selected")
    @JvmField val ENCHANT_SELECT_TOGGLE_LORE_EVERY = Message("enchant-select-setting.show-toggle-lore-every")

    @JvmField val SELECT_MATERIAL_HEADER = Message("material-selector.header", "type", "action")
    @JvmField val SELECT_MATERIAL_EMPTY = Message("material-selector.empty", "type", "action")
    @JvmField val SELECT_MATERIAL_NOT_EMPTY = Message("material-selector.not-empty", "type", "action")
    @JvmField val SELECT_MATERIAL_ITEM = Message("material-selector.item", "name")
    @JvmField val SELECT_MATERIAL_AND_MORE = Message("material-selector.and-more", "count")

    @JvmField val SELECT_GROUP_HEADER = Message("group-selector.header", "type", "action")
    @JvmField val SELECT_GROUP_EMPTY = Message("group-selector.empty", "type", "action")
    @JvmField val SELECT_GROUP_NOT_EMPTY = Message("group-selector.not-empty", "type", "action")
    @JvmField val SELECT_GROUP_ITEM = Message("group-selector.item", "name")
    @JvmField val SELECT_GROUP_AND_MORE = Message("group-selector.and-more", "count")

}