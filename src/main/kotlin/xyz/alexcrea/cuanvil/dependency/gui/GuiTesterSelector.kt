package xyz.alexcrea.cuanvil.dependency.gui

import xyz.alexcrea.cuanvil.update.UpdateUtils

object GuiTesterSelector {

    val spigotVersionString: String?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersionArray()
            if (versionParts[0] != 1) return null

            return when (versionParts[1]) {
                21 -> when (versionParts[2]) {
                    8 -> "1_21_R5"
                    9, 10 -> "1_21_R6"
                    else -> null
                }

                else -> null
            }
        }

}