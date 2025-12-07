package xyz.alexcrea.cuanvil.dependency.gui

import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.MinecraftVersionUtil
import java.lang.reflect.Method

class GenericExternGuiTester: ExternGuiTester {

    companion object {
        private const val ANVIL_CLASS_NAME = "org.bukkit.craftbukkit.inventory.view.CraftAnvilView"
        private const val INV_CLASS_NAME = "org.bukkit.craftbukkit.inventory.CraftInventoryView"
        private const val HANDLE_METHOD_NAME = "getHandle"
    }

    var tested = false

    var testedClass: String? = null
    lateinit var getHandleMethod: Method

    override fun getContainerClass(view: InventoryView): Class<Any>? {
        // In case we are in a test environment
        if(!tested) testClassExist()

        if(!testedClass.contentEquals(view.javaClass.name))
            return null

        val container = getHandleMethod.invoke(view)
        return container.javaClass
    }

    fun tryFromClass(className: String) {
        val clazz = Class.forName(className)
        testedClass = className

        getHandleMethod = clazz.getMethod(HANDLE_METHOD_NAME)
    }

    fun testClassExist() {
        tested = true

        // We first try to get craft anvil interface,
        // but is absent on old version so we try craft inventory view before
        try {
            tryFromClass(ANVIL_CLASS_NAME)
            return
        }
        catch (_: ClassNotFoundException) {}
        catch (_: NoSuchMethodException) {}

        try {
            tryFromClass(INV_CLASS_NAME)
            return
        }
        catch (_: ClassNotFoundException) {}
        catch (_: NoSuchMethodException) {}
    }

}