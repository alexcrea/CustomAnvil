package xyz.alexcrea.cuanvil.lang

object MsgWarning {

    /*
     * ---------------
     * Load and reload
     * ---------------
     */
    val LOAD_UPDATE_AVAILABLE = WarningMessage("load.update.available", "version")

    val LOAD_LEGACY_OLD_NAME = MultiLineMessage(MessageType.WARNING, "load.legacy.old-name", 2)
    val LOAD_LEGACY_SPIGOT = MultiLineMessage(MessageType.WARNING, "load.legacy.spigot", 2)
    val LOAD_LEGACY_SPIGOT_OLD = MultiLineMessage(MessageType.WARNING, "load.legacy.spigot", 2)


}