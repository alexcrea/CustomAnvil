package xyz.alexcrea.cuanvil.dependency.gui

import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil

interface ExternGuiTester {

    fun getContainerClass(view: InventoryView): Class<Any>?

    fun testIfGui(inventory: InventoryView): Boolean {
        // container class only allow default bukkit craft view class

        val clazz = getContainerClass(inventory)
        return clazz != null
    }

}