package xyz.alexcrea.cuanvil.dependency.gui

import xyz.alexcrea.cuanvil.dependency.gui.version.*;
import xyz.alexcrea.cuanvil.update.UpdateUtils

object GuiTesterSelector {

    val selectGuiTester: ExternGuiTester?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersionArray()
            if (versionParts[0] != 1) return null

            return when (versionParts[1]) {
                // Can't support 1.16.5 bc 1.16.5 paper userdev do not exist

                17 -> when (versionParts[2]) {
                    0, 1 -> v1_17R1_ExternGuiTester()
                    else -> null
                }

                18 -> when (versionParts[2]) {
                    0, 1 -> v1_18R1_ExternGuiTester()
                    2 -> v1_18R2_ExternGuiTester()
                    else -> null
                }

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
                    else -> null
                }

                else -> null
            }
        }

}