package xyz.alexcrea.cuanvil.dependency.gui

import xyz.alexcrea.cuanvil.update.UpdateUtils

object GuiTesterSelector {

    val selectGuiTester: ExternGuiTester?
        get() {
            val versionParts = UpdateUtils.currentMinecraftVersionArray()
            if (versionParts[0] != 1) return null

            return GenericExternGuiTester()
        }

}