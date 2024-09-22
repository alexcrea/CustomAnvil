package xyz.alexcrea.cuanvil.dependency.gui.version

import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView
import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.gui.ExternGuiTester

class v1_20R1_ExternGuiTester: ExternGuiTester {
    override val wesjdAnvilGuiName = "Wrapper1_20_R1"

    override fun getContainerClass(view: InventoryView): Class<Any>? {
        if (view !is CraftInventoryView) return null
        val container = view.handle

        return container.javaClass
    }
}
