package xyz.alexcrea.cuanvil.dependency.plugins

import io.delilaheve.CustomAnvil
import lol.hyper.toolstats.ToolStats
import lol.hyper.toolstats.tools.ItemChecker
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener
import java.lang.reflect.Method

class ToolStatsDependency(plugin: Plugin) : GenericPluginDependency(plugin) {

    // Sadly, getTokens function is private, so I need to do that
    private val getTokenMethod: Method =
        ItemChecker::class.java.getDeclaredMethod("getTokens", ItemStack::class.java);

    init {
        getTokenMethod.trySetAccessible()
    }

    override fun postAnvilEvents(): Collection<RegisteredListener> {
        return listOf()
    }

    override fun testPrepareAnvil(event: PrepareAnvilEvent): Boolean {
        var result = super.testPrepareAnvil(event)
        CustomAnvil.verboseLog("pre anvil result: $result")

        return result
    }

    override fun testAnvilResult(event: InventoryClickEvent): Boolean {
        // Check if token changes from left with result
        val left = event.inventory.getItem(PrepareAnvilListener.ANVIL_INPUT_LEFT)
        val result = event.inventory.getItem(PrepareAnvilListener.ANVIL_OUTPUT_SLOT)

        val itemChecker = (plugin as ToolStats).itemChecker

        val leftTokens = getTokenMethod.invoke(itemChecker, left) as Array<String>
        val resultToken = getTokenMethod.invoke(itemChecker, result) as Array<String>

        val resultVal = !leftTokens.contentDeepEquals(resultToken)

        CustomAnvil.verboseLog("Test anvil result:  $resultVal")
        return resultVal
    }
}
