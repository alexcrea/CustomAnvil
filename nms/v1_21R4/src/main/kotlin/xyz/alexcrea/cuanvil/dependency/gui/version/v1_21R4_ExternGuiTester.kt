package xyz.alexcrea.cuanvil.dependency.gui.version

import org.bukkit.craftbukkit.inventory.CraftInventoryView
import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.gui.ExternGuiTester

class v1_21R4_ExternGuiTester : ExternGuiTester {
    override val wesjdAnvilGuiName = "Wrapper1_21_R4"

    var tested = false
    var possible = false

    override fun getContainerClass(view: InventoryView): Class<Any>? {
        // In case we are in a test environment
        if (!tested) testClassExist()
        if (!possible) return null

        if (view !is CraftInventoryView<*, *>) return null
        val container = view.handle

        return container.javaClass
    }

    fun testClassExist() {
        tested = true
        try {
            Class.forName("org.bukkit.craftbukkit.inventory.CraftInventoryView")
            possible = true
        } catch (e: ClassNotFoundException) {
            possible = false
        }
    }

}
