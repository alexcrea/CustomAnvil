package xyz.alexcrea.cuanvil.dependency.gui

import xyz.alexcrea.cuanvil.dependency.gui.version.*
import xyz.alexcrea.cuanvil.update.UpdateUtils

object GuiTesterSelector {

    val selectGuiTester: ExternGuiTester?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersionArray()
            if (versionParts[0] != 1) return null

            return when (versionParts[1]) {
                // Can't support 1.16.5 bc 1.16.5 paper userdev do not exist

                21 -> when (versionParts[2]) {
                    0, 1 -> v1_21R1_ExternGuiTester()
                    2, 3 -> v1_21R2_ExternGuiTester()
                    4 -> v1_21R3_ExternGuiTester()
                    5 -> v1_21R4_ExternGuiTester()
                    else -> null
                }

                else -> null
            }
        }

}