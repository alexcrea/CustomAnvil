package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.CommandMessage as Message

object MsgCommand {

    // Root
    val ROOT_UNKNOWN_SUBCOMMAND = Message("root.warning.unknown-sub", "command")
    val ROOT_ERROR_SUBCOMMAND = Message("root.error.generic")

    // Shared
    val SHARED_NO_DIAG_PERM = Message("shared.no-diag-permission")
    val SHARED_HOVER_COPY = Message("shared.hover-copy")
    val SHARED_MISSING_SUBCOMMAND = Message("shared.warning.missing-subcmd", "example1", "example2")
    val SHARED_UNKNOWN_SUB_COMMAND = Message("shared.unknown-subcmd", "command")
    val SHARED_NO_PERMISSION = Message("shared.no-permission")

    // Debug
    val DEBUG_DESCRIPTION = Message("debug.description")
    val DEBUG_LOG_CLEARED = Message("debug.log-cleared")
    val DEBUG_TOGGLED = Message("debug.toggled", "type")
    val DEBUG_COPY = Message("debug.copy")
    val DEBUG_LANG_COPY = Message("debug.copy-lang")

    val DEBUG_DATA_HEADER = Message("debug.data.header")
    val DEBUG_DATA_LINE_COUNT = Message("debug.data.line-count", "count")

    val DEBUG_WARNING_UNSPECIFIED_TYPE = Message("debug.warning.unspecified-type")
    val DEBUG_WARNING_INVALID_TYPE = Message("debug.warning.invalid-type", "type")
    val DEBUG_WARNING_NO_LOG = Message("debug.warning.no-log", "command")

    // Diagnostic
    val DIAGNOSTIC_DESCRIPTION = Message("diagnostic.description")
    val DIAGNOSTIC_ERROR_GENERIC = Message("diagnostic.had-error")
    val DIAGNOSTIC_COPY = Message("diagnostic.copy")

    // Config
    val CONFIG_DESCRIPTION = Message("config.description")
    val CONFIG_LEGACY_NAME_WARNING = Message("config.warning.legacy-name")
    val CONFIG_FOLIA_ISSUE = Message("config.folia-issue")

    val CONFIG_ENCHANTMENT_NO_IN_HAND = Message("config.enchantment.no_hand")
    val CONFIG_ENCHANTMENT_NO_NAME = Message("config.enchantment.no_name", "name")

    val CONFIG_CANNOT_CONFIGURE_WARNING = Message("config.warning.cannot_configure")

    // Enchant
    val ENCHANT_DESCRIPTION = Message("enchant.description")
    val ENCHANT_REMOVE = Message("enchant.removed", "name")
    val ENCHANT_SET = Message("enchant.set", "name", "level")

    val ENCHANT_MISSING_PARAMETER_WARNING = Message("enchant.warning.missing_parameter")
    val ENCHANT_NOT_FOUND_WARNING = Message("enchant.warning.not_found", "path")
    val ENCHANT_MALFORMED_NUMBER_WARNING = Message("enchant.warning.malformed_number", "num")
    val ENCHANT_CANNOT_ENCHANT_WARNING = Message("enchant.warning.cannot_enchant")

    // Help
    val HELP_DESCRIPTION = Message("help.description")
    val HELP_HEADER = Message("help.header")

    // Reload
    val RELOAD_DESCRIPTION = Message("reload.description")
    val RELOAD_START = Message("reload.start")
    val RELOAD_SUCCESS = Message("reload.success")
    val RELOAD_FAIL = Message("reload.fail")
    val RELOAD_HARD_FAIL = Message("reload.hard-fail")

}

