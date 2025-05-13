package xyz.alexcrea.cuanvil.dependency.plugins

import lol.hyper.toolstats.tools.ItemChecker
import org.bukkit.event.inventory.InventoryClickEvent
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

    override fun testAnvilResult(event: InventoryClickEvent): Boolean {
        // Check if token changes from left with result
        val left = event.inventory.getItem(PrepareAnvilListener.ANVIL_INPUT_LEFT)
        val result = event.inventory.getItem(PrepareAnvilListener.ANVIL_OUTPUT_SLOT)

        val leftTokens = getTokenMethod.invoke(left) as Array<String>
        val resultToken = getTokenMethod.invoke(result) as Array<String>

        return !leftTokens.contentDeepEquals(resultToken);
    }
}
