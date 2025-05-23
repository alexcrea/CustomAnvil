package xyz.alexcrea.cuanvil.dependency.gui.version

import org.bukkit.craftbukkit.inventory.CraftInventoryView
import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.gui.ExternGuiTester
import kotlin.jvm.javaClass

class v1_20R4_ExternGuiTester: ExternGuiTester {
    override val wesjdAnvilGuiName = "Wrapper1_20_R4"

    override fun getContainerClass(view: InventoryView): Class<Any>? {
        if (view !is CraftInventoryView) return null
        val container = view.handle

        return container.javaClass
    }
}
