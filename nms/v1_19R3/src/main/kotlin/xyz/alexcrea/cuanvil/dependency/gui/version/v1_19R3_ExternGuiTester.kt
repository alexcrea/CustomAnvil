package xyz.alexcrea.cuanvil.dependency.gui.version

import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView
import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.gui.ExternGuiTester

class v1_19R3_ExternGuiTester: ExternGuiTester {
    override val wesjdAnvilGuiName = "Wrapper1_19_R3"

    override fun getContainerClass(view: InventoryView): Class<Any>? {
        if (view !is CraftInventoryView) return null
        val container = view.handle

        return container.javaClass
    }
}