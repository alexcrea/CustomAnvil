package xyz.alexcrea.cuanvil.lang

object MsgError {

    /*
     * -----------------
     *  Load and reload
     * -----------------
     */
    @JvmField val LOAD_UPDATE_CHECK_FAIL = ErrorMessage("load.update.check-fail")
    @JvmField val LOAD_LEGACY_FAILED = ErrorMessage("load.legacy.failed")
    @JvmField val LOAD_COMMAND_REGISTER = ErrorMessage("load.command-register")
    @JvmField val LOAD_COMPATIBILITY = ErrorMessage("load.compatibility")
    @JvmField val LOAD_LISTENERS = ErrorMessage("load.listeners")
    @JvmField val LOAD_ENCHANT_SYSTEM = ErrorMessage("load.enchant-system")
    @JvmField val LOAD_NON_DEFAULT_CONFIG = ErrorMessage("load.non-default-config")

    @JvmField val RELOAD_FAIL = ErrorMessage("reload.resource.fail", "path")
    @JvmField val RELOAD_HARD_FAIL = ErrorMessage("reload.resource.hard-fail")

    /*
     * ----
     *  UI
     * ----
     */
    @JvmField val CONFIRM_ACTION_GENERIC = ErrorMessage("confirm-action.generic")
    @JvmField val UI_SAVE_FAILED = ErrorMessage("ui.save-failed")
    @JvmField val UI_CONFIG_DELETED = ErrorMessage("ui.config-deleted")

}
