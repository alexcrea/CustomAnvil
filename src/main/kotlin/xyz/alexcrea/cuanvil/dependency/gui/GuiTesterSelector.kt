package xyz.alexcrea.cuanvil.dependency.gui

import xyz.alexcrea.cuanvil.dependency.gui.version.*;
import xyz.alexcrea.cuanvil.update.UpdateUtils

object GuiTesterSelector {

    val selectGuiTester: ExternGuiTester?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersionArray()
            if (versionParts[0] != 1) return null

            return when (versionParts[1]) {
                // Can't support 1.16.5-1.18.x paper userdev do not exist or broken

                19 -> when (versionParts[2]) {
                    0, 1, 2 -> v1_19R1_ExternGuiTester()
                    3 -> v1_19R2_ExternGuiTester()
                    4 -> v1_19R3_ExternGuiTester()
                    else -> null
                }

                20 -> when (versionParts[2]) {
                    0, 1 -> v1_20R1_ExternGuiTester()
                    2 -> v1_20R2_ExternGuiTester()
                    3, 4 -> v1_20R3_ExternGuiTester()
                    5, 6 -> v1_20R4_ExternGuiTester()
                    else -> null
                }

                21 -> when (versionParts[2]) {
                    0, 1 -> v1_21R1_ExternGuiTester()
                    2, 3 -> v1_21R2_ExternGuiTester()
                    4 -> v1_21R3_ExternGuiTester()
                    5 -> v1_21R4_ExternGuiTester()
                    6, 7, 8 -> v1_21R5_ExternGuiTester()
                    9 -> v1_21R6_ExternGuiTester()
                    else -> null
                }

                else -> null
            }
        }

}