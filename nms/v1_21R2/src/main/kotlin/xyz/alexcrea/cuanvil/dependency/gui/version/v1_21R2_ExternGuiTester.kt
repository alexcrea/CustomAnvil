package xyz.alexcrea.cuanvil.dependency.gui.version

import org.bukkit.craftbukkit.inventory.CraftInventoryView
import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.gui.ExternGuiTester

class v1_21R2_ExternGuiTester : ExternGuiTester {
    override val wesjdAnvilGuiName = "Wrapper1_21_R2"

    override fun getContainerClass(view: InventoryView): Class<Any>? {
        if (view !is CraftInventoryView<*, *>) return null
        val container = view.handle

        return container.javaClass
    }

}
