package xyz.alexcrea.cuanvil.dependency.gui

import org.bukkit.inventory.InventoryView

interface ExternGuiTester {

    val wesjdAnvilGuiName: String?

    fun getContainerClass(inventory: InventoryView): Class<Any>?

    fun testIfGui(inventory: InventoryView): Boolean {
        val clazz = getContainerClass(inventory)
        if(clazz == null) return false

        val expectedWesjdGuiPath = "anvilgui.version.$wesjdAnvilGuiName"

        val clazzName = clazz.name
        val isWejdsGui = clazzName.contains(expectedWesjdGuiPath)

        return isWejdsGui
    }


}