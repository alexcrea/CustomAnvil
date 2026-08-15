package xyz.alexcrea.cuanvil.lang

import xyz.alexcrea.cuanvil.lang.WarningMessage as Message

object MsgWarning {

    fun multiLine(basekey: String, count: Int): MultiLineMessage {
        return MultiLineMessage(
            MessageType.WARNING,
            basekey,
            count
        )
    }

    /*
     * -----------------
     *  Load and reload
     * -----------------
     */
    val LOAD_UPDATE_AVAILABLE = Message("load.update.available", "version")

    val LOAD_LEGACY_OLD_NAME = multiLine("load.legacy.old-name", 2)
    val LOAD_LEGACY_SPIGOT = multiLine("load.legacy.spigot", 2)
    val LOAD_LEGACY_SPIGOT_OLD = multiLine("load.legacy.spigot", 2)

}