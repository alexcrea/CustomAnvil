package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.WarningMessage as Message

object MsgWarning {

    /*
     * -----------------
     *  Load and reload
     * -----------------
     */
    @JvmField val LOAD_UPDATE_AVAILABLE = Message("load.update.available", "version")

    @JvmField val LOAD_LEGACY_OLD_NAME = Message("load.legacy.old-name")
    @JvmField val LOAD_LEGACY_SPIGOT = Message("load.legacy.spigot")
    @JvmField val LOAD_LEGACY_SPIGOT_OLD = Message("load.legacy.spigot-old")

    @JvmField val ANVIL_GENERIC_EXCEPTION = Message("anvil.generic")

}