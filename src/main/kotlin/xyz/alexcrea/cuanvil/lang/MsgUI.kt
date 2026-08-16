package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.UIMessage as Message

object MsgUI {

    val SHARED_CONFIG_NO_EDIT_PERM = Message("shared.no-permission")
    val SHARED_TYPED_CONFIG_TITLE = Message("shared.typed-config-title", "type")

    val CONFIRM_ACTION_FAILED = Message("confirm-action.fail")

    val ELEMENT_LIST_INSTRUCTION_NEW = Message("element-list.instruction-new", "type")
    val ELEMENT_LIST_CANCELLED_NEW = Message("element-list.cancelled-new", "type")
    val ELEMENT_LIST_DUPLICATED_NEW = Message("element-list.duplicated-new", "type")

    val UNIT_REPAIR_TITLE = Message("unit-repair.title")
    val UNIT_REPAIR_ELEMENT_TITLE = Message("unit-repair.element_title")
    val UNIT_REPAIR_NEW_TITLE = Message("unit-repair.new.title")
    val UNIT_REPAIR_NEW_DESCRIPTION = Message("unit-repair.new.description")

    val UNIT_REPAIR_NEW_ELEMENT_TITLE = Message("unit-repair.element.new.title", "name")
    val UNIT_REPAIR_NEW_ELEMENT_DESCRIPTION = Message("unit-repair.element.new.description", "name")
    val UNIT_REPAIR_NEW_ELEMENT_CANNOT_REPAIR = Message("unit-repair.element.new.cannot-damage")
    val UNIT_REPAIR_NEW_ELEMENT_SAME_TYPE = Message("unit-repair.element.new.same-type")

    val CUSTOM_RECIPE_TITLE = Message("custom-recipe.title")

    val CUSTOM_RECIPE_ELEMENT_DELETE_TITLE = Message("custom-recipe.element.delete.title", "type")
    val CUSTOM_RECIPE_ELEMENT_DELETE_DESCRIPTION = Message("custom-recipe.element.delete.description", "type")

    val ENCHANTMENT_LEVEL_COST_TITLE = Message("enchant-level-cost.title")

    val ENCHANTMENT_LEVEL_LIMIT_TITLE = Message("enchant-level-limit.title")

    val ENCHANTMENT_MERGE_LIMIT_TITLE = Message("enchant-merge-limit.title")

    val ENCHANTMENT_CONFLICT_TITLE = Message("enchant-conflict.title")
    val ENCHANTMENT_CONFLICT_ELEMENT_ENCHANTMENTS = Message("enchant-conflict.element.selected-enchantments", "group")
    val ENCHANTMENT_CONFLICT_ELEMENT_SUB_GROUPS = Message("enchant-conflict.element.selected-sub-groups", "group")

    val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_TITLE = Message("enchant-conflict.element.delete.title", "type")
    val ENCHANTMENT_CONFLICT_ELEMENT_DELETE_DESCRIPTION = Message("enchant-conflict.element.delete.description", "type")

    val MATERIAL_GROUP_TITLE = Message("material-group.title")
    val MATERIAL_GROUP_ELEMENT_SELECTED_MATERIALS = Message("material-group.element.selected-materials", "group")
    val MATERIAL_GROUP_ELEMENT_SELECTED_SUB_GROUPS = Message("material-group.element.selected-sub-groups", "group")

    val MATERIAL_GROUP_ELEMENT_DELETE_TITLE = Message("material-group.element.delete.title", "type")
    val MATERIAL_GROUP_ELEMENT_DELETE_DESCRIPTION = Message("material-group.element.delete.description", "type")
    val MATERIAL_GROUP_ELEMENT_DELETE_BUTTON_NAME = Message("material-group.element.delete.button.name")
    val MATERIAL_GROUP_ELEMENT_DELETE_BUTTON_LORE = Message("material-group.element.delete.button.lore")

    val MATERIAL_SELECT_CONFIRM_TITLE = Message("material-select.confirm.title","name")
    val MATERIAL_SELECT_CONFIRM_DESCRIPTION = Message("material-select.confirm.description","name")

}