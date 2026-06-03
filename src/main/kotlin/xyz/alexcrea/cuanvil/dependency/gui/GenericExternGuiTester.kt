package xyz.alexcrea.cuanvil.dependency.gui

import org.bukkit.inventory.InventoryView
import xyz.alexcrea.cuanvil.dependency.MinecraftVersionUtil
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil
import java.lang.reflect.Method

class GenericExternGuiTester {

    companion object {
        private const val ANVIL_CLASS_NAME = "org.bukkit.craftbukkit.inventory.view.CraftAnvilView"
        private const val INV_CLASS_NAME = "org.bukkit.craftbukkit.inventory.CraftInventoryView"
        private const val HANDLE_METHOD_NAME = "getHandle"

        private const val CANONICAL_PAPER_ANVIL_MENU = "net.minecraft.world.inventory.AnvilMenu"
    }

    var testExist = false
    var inTesting = false

    var testedClass: String? = null
    lateinit var getHandleMethod: Method

    private fun getContainerClass(view: InventoryView): Class<Any>? {
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

    fun isInTest(): Boolean {
        if(!testExist) testClassExist()
        return inTesting
    }

    fun testClassExist() {
        testExist = true

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

        inTesting = true
    }

    // Try if were in another plugin anvil inventory
    fun testIfGui(view: InventoryView): Boolean {
        // In case we are in a test environment
        if(isInTest()) return false

        val clazz = getContainerClass(view) ?: return false

        val clazzName = clazz.name
        if(!PlatformUtil.isPaper){
            // Blacklist gui causing issue
            if (expectWesjd(clazzName)) return true
            if (expectXenondevUI(clazzName)) return true
            if (expectVanePortal(clazzName)) return true

            return false
        }

        // Only allow cannonical anvil menu class
        return !CANONICAL_PAPER_ANVIL_MENU.equals(clazzName, true)
    }

    // Known custom implementations
    fun expectWesjd(name: String): Boolean {
        val expectedWesjdGuiPath = "anvilgui.version.Wrapper${MinecraftVersionUtil.craftbukkitVersion}"

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