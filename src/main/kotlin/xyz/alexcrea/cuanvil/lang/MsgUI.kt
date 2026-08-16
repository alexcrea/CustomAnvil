package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.UIMessage as Message

object MsgUI {

    val SHARED_CONFIG_NO_EDIT_PERM = Message("shared.no-permission")

    val CONFIRM_ACTION_FAILED = Message("confirm-action.fail")

    val ELEMENT_LIST_INSTRUCTION_NEW = Message("element-list.instruction-new", "type")
    val ELEMENT_LIST_CANCELLED_NEW = Message("element-list.cancelled-new", "type")
    val ELEMENT_LIST_DUPLICATED_NEW = Message("element-list.duplicated-new", "type")

    val UNIT_REPAIR_ELEMENT_TITLE = Message("unit-repair.element.title")
    val UNIT_REPAIR_ELEMENT_DESCRIPTION = Message("unit-repair.element.description")
    val UNIT_REPAIR_ELEMENT_CANNOT_REPAIR_NEW = Message("unit-repair.element.cannot-damage-new")
    val UNIT_REPAIR_ELEMENT_SAME_TYPE_NEW = Message("unit-repair.element.same-type-new")


}