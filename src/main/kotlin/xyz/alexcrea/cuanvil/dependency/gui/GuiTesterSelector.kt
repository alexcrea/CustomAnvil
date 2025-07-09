package xyz.alexcrea.cuanvil.dependency.gui

import xyz.alexcrea.cuanvil.update.UpdateUtils

object GuiTesterSelector {

    val spigotVersionString: String?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersionArray()
            if (versionParts[0] != 1) return null

            return when (versionParts[1]) {
                21 -> when (versionParts[2]) {
                    0, 1 -> "1_21_R1"
                    2, 3 -> "1_21_R2"
                    4 -> "1_21_R3"
                    5 -> "1_21_R4"
                    6, 7, 8 -> "1_21_R5"
                    else -> null
                }

                else -> null
            }
        }

}