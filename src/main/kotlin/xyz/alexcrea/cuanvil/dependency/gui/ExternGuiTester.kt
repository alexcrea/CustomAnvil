package xyz.alexcrea.cuanvil.dependency.gui

import org.bukkit.craftbukkit.inventory.CraftInventoryView
import org.bukkit.inventory.InventoryView

object ExternGuiTester {

    fun getContainerClass(view: InventoryView): Class<Any>? {
        if (view !is CraftInventoryView<*, *>) return null
        val container = view.handle

        return container.javaClass
    }

    fun testIfGui(view: InventoryView): Boolean {
        // this mean we are on test
        //TODO review why needed knowing previous mitigations should works
        if(view.javaClass.name.endsWith("AnvilViewMock")) return false

        val clazz = getContainerClass(view) ?: return false

        val clazzName = clazz.name
        //TODO maybe instead of testing non default, better to be testing we are default ?
        if (expectWesjd(clazzName)) return true
        if (expectXenondevUI(clazzName)) return true
        if (expectVanePortal(clazzName)) return true

        return false
    }

    fun expectWesjd(name: String): Boolean {
        val spigotVer = GuiTesterSelector.spigotVersionString
        if(spigotVer == null) return false

        val expectedWesjdGuiPath = "anvilgui.version.Wrapper${spigotVer}"

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
