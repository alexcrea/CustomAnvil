package xyz.alexcrea.cuanvil.dependency.gui

import org.bukkit.craftbukkit.inventory.CraftInventoryView
import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil

object ExternGuiTester {

    object Const{
        val cannonicalPaperAnvilMenu = "net.minecraft.world.inventory.AnvilMenu"
    }


    fun getContainerClass(view: InventoryView): Class<Any>? {
        if (view !is CraftInventoryView<*, *>) return null
        val container = view.handle

        return container.javaClass
    }

    fun testIfGui(view: InventoryView): Boolean {
        if (PlatformUtil.isMockbukkit) return false
        val clazz = getContainerClass(view) ?: return false

        val clazzName = clazz.name

        // Only allow cannonical anvil menu class
        return !Const.cannonicalPaperAnvilMenu.equals(clazzName, true)
    }

}
