package xyz.alexcrea.cuanvil.lang

object MsgError {

    /*
     * -----------------
     *  Load and reload
     * -----------------
     */
    val LOAD_UPDATE_CHECK_FAIL = ErrorMessage("load.update.check-fail")
    val LOAD_LEGACY_FAILED = ErrorMessage("load.legacy.failed")
    val LOAD_COMMAND_REGISTER = ErrorMessage("load.command-register")
    val LOAD_COMPATIBILITY = ErrorMessage("load.compatibility")
    val LOAD_LISTENERS = ErrorMessage("load.listeners")
    val LOAD_ENCHANT_SYSTEM = ErrorMessage("load.enchant-system")
    val LOAD_NON_DEFAULT_CONFIG = ErrorMessage("load.non-default-config")

    val RELOAD_FAIL = ErrorMessage("reload.resource.fail", "path")
    val RELOAD_HARD_FAIL = ErrorMessage("reload.resource.hardfail")

    /*
     * ----
     *  UI
     * ----
     */
    val CONFIRM_ACTION_GENERIC = ErrorMessage("confirm-action.generic")


}
