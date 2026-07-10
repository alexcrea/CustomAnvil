package xyz.alexcrea.cuanvil.dependency

import xyz.alexcrea.cuanvil.update.UpdateUtils

object MinecraftVersionUtil {

    val craftbukkitVersion: String?
        get() {
            val version = UpdateUtils.currentMinecraftVersion()
            if (version.major != 1) return null

            return when (version.minor) {
                17 -> when (version.patch) {
                    0, 1 -> "1_17R1"
                    else -> null
                }

                18 -> when (version.patch) {
                    0, 1 -> "1_18R1"
                    2 -> "1_18R2"
                    else -> null
                }

                19 -> when (version.patch) {
                    0, 1, 2 -> "1_19R1"
                    3 -> "1_19R2"
                    4 -> "1_19R3"
                    else -> null
                }

                20 -> when (version.patch) {
                    0, 1 -> "1_20R1"
                    2 -> "1_20R2"
                    3, 4 -> "1_20R3"
                    5, 6 -> "1_20R4"
                    else -> null
                }

                21 -> when (version.patch) {
                    0, 1 -> "1_21R1"
                    2, 3 -> "1_21R2"
                    4 -> "1_21R3"
                    5 -> "1_21R4"
                    6, 7, 8 -> "1_21R5"
                    9, 10 -> "1_21R6"
                    11 -> "1_21R7"
                    else -> null
                }

                else -> null
            }
        }

}