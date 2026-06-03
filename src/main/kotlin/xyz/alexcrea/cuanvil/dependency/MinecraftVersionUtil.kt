package xyz.alexcrea.cuanvil.dependency

import xyz.alexcrea.cuanvil.update.UpdateUtils

object MinecraftVersionUtil {

    val craftbukkitVersion: String?
        get() {
            val version = UpdateUtils.currentMinecraftVersion()
            if (version.major != 1) return null

            return when (version.minor) {
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
                //TODO need to continue updating that for anvil gui test dependency....

                else -> null
            }
        }

    val isTooNewForSpigot: Boolean get() {
        return UpdateUtils.currentMinecraftVersion().major != 1
    }

}