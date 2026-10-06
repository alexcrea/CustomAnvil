package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.CommandMessage as Message

object MsgCommand {

    // Root
    @JvmField val ROOT_UNKNOWN_SUBCOMMAND = Message("root.warning.unknown-sub", "command")
    @JvmField val ROOT_ERROR_SUBCOMMAND = Message("root.error.generic")

    // Shared
    @JvmField val SHARED_NO_DIAG_PERM = Message("shared.no-diag-permission")
    @JvmField val SHARED_HOVER_COPY = Message("shared.hover-copy")
    @JvmField val SHARED_MISSING_SUBCOMMAND = Message("shared.warning.missing-subcmd", "example1", "example2")
    @JvmField val SHARED_UNKNOWN_SUB_COMMAND = Message("shared.unknown-subcmd", "command")
    @JvmField val SHARED_NO_PERMISSION = Message("shared.no-permission")

    // Debug
    @JvmField val DEBUG_DESCRIPTION = Message("debug.description")
    @JvmField val DEBUG_LOG_CLEARED = Message("debug.log-cleared")
    @JvmField val DEBUG_TOGGLED = Message("debug.toggled", "type")
    @JvmField val DEBUG_COPY = Message("debug.copy")
    @JvmField val DEBUG_LANG_COPY = Message("debug.copy-lang")

    @JvmField val DEBUG_DATA_HEADER = Message("debug.data.header")
    @JvmField val DEBUG_DATA_LINE_COUNT = Message("debug.data.line-count", "count")

    @JvmField val DEBUG_WARNING_UNSPECIFIED_TYPE = Message("debug.warning.unspecified-type")
    @JvmField val DEBUG_WARNING_INVALID_TYPE = Message("debug.warning.invalid-type", "type")
    @JvmField val DEBUG_WARNING_NO_LOG = Message("debug.warning.no-log", "command")

    // Diagnostic
    @JvmField val DIAGNOSTIC_DESCRIPTION = Message("diagnostic.description")
    @JvmField val DIAGNOSTIC_ERROR_GENERIC = Message("diagnostic.had-error")
    @JvmField val DIAGNOSTIC_COPY = Message("diagnostic.copy")

    // Config
    @JvmField val CONFIG_DESCRIPTION = Message("config.description")
    @JvmField val CONFIG_LEGACY_NAME_WARNING = Message("config.warning.legacy-name")
    @JvmField val CONFIG_FOLIA_ISSUE = Message("config.folia-issue")

    @JvmField val CONFIG_ENCHANTMENT_NO_IN_HAND = Message("config.enchantment.no_hand")
    @JvmField val CONFIG_ENCHANTMENT_NO_NAME = Message("config.enchantment.no_name", "name")

    @JvmField val CONFIG_CANNOT_CONFIGURE_WARNING = Message("config.warning.cannot_configure")

    // Enchant
    @JvmField val ENCHANT_DESCRIPTION = Message("enchant.description")
    @JvmField val ENCHANT_REMOVE = Message("enchant.removed", "name")
    @JvmField val ENCHANT_SET = Message("enchant.set", "name", "level")

    @JvmField val ENCHANT_MISSING_PARAMETER_WARNING = Message("enchant.warning.missing_parameter")
    @JvmField val ENCHANT_NOT_FOUND_WARNING = Message("enchant.warning.not_found", "path")
    @JvmField val ENCHANT_MALFORMED_NUMBER_WARNING = Message("enchant.warning.malformed_number", "num")
    @JvmField val ENCHANT_CANNOT_ENCHANT_WARNING = Message("enchant.warning.cannot_enchant")

    // Help
    @JvmField val HELP_DESCRIPTION = Message("help.description")
    @JvmField val HELP_HEADER = Message("help.header")

    // Reload
    @JvmField val RELOAD_DESCRIPTION = Message("reload.description")
    @JvmField val RELOAD_START = Message("reload.start")
    @JvmField val RELOAD_SUCCESS = Message("reload.success")
    @JvmField val RELOAD_FAIL = Message("reload.fail")
    @JvmField val RELOAD_HARD_FAIL = Message("reload.hard-fail")

}

