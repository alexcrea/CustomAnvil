package xyz.alexcrea.cuanvil.dependency.gui

import org.bukkit.inventory.InventoryView

interface ExternGuiTester {

    val wesjdAnvilGuiName: String?

    fun getContainerClass(inventory: InventoryView): Class<Any>?

    fun testIfGui(inventory: InventoryView): Boolean {
        val clazz = getContainerClass(inventory) ?: return false

        val clazzName = clazz.name
        if (expectWesjd(clazzName)) return true
        if (expectXenondevUI(clazzName)) return true

        return false
    }

    fun expectWesjd(name: String): Boolean {
        val expectedWesjdGuiPath = "anvilgui.version.$wesjdAnvilGuiName"

        return name.contains(expectedWesjdGuiPath)
    }

    private val XenondevUIPrefix: String
        get() = "xyz.xenondevs.inventoryaccess."
    private val XenondevUISufix: String
        get() = ".AnvilInventoryImpl"

    fun expectXenondevUI(name: String): Boolean {
        return name.startsWith(XenondevUIPrefix)
                && name.endsWith(XenondevUISufix)
    }


}