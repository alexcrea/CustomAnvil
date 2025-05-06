package xyz.alexcrea.cuanvil.dependency.gui

import org.bukkit.inventory.InventoryView

interface ExternGuiTester {

    val wesjdAnvilGuiName: String?

    fun getContainerClass(inventory: InventoryView): Class<Any>?

    fun testIfGui(inventory: InventoryView): Boolean {
        // this mean we are on test
        //TODO review why needed knowing previous mitigations should works
        if(inventory.javaClass.name.endsWith("AnvilViewMock")) return false

        val clazz = getContainerClass(inventory) ?: return false

        val clazzName = clazz.name
        //TODO maybe instead of testing non default, better to be testing we are default ?
        if (expectWesjd(clazzName)) return true
        if (expectXenondevUI(clazzName)) return true
        if (expectVanePortal(clazzName)) return true

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

    fun expectVanePortal(name: String): Boolean {
        val expected = "org.oddlama.vane.core.menu.AnvilMenu\$AnvilContainer"

        return name == expected
    }

}