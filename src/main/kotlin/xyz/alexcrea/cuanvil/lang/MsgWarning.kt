package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.WarningMessage as Message

object MsgWarning {

    /*
     * -----------------
     *  Load and reload
     * -----------------
     */
    val LOAD_UPDATE_AVAILABLE = Message("load.update.available", "version")

    val LOAD_LEGACY_OLD_NAME = Message("load.legacy.old-name")
    val LOAD_LEGACY_SPIGOT = Message("load.legacy.spigot")
    val LOAD_LEGACY_SPIGOT_OLD = Message("load.legacy.spigot-old")

    val ANVIL_GENERIC_EXCEPTION = Message("anvil.generic")

}