package xyz.alexcrea.cuanvil.dependency.plugins

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.RegisteredListener
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEEV5Enchantment
import java.lang.reflect.Method
import su.nightexpress.excellentenchants.api.EnchantRegistry as V5EnchantRegistry
import su.nightexpress.excellentenchants.manager.listener.AnvilListener as V5AnvilListener

class ExcellentEnchantsDependency {

    init {
        CustomAnvil.instance.logger.info("Excellent Enchants Detected !")
    }

    fun registerEnchantments() {
        CustomAnvil.instance.logger.info("Preparing Excellent Enchants compatibility...")

        // As excellent enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
        for (enchantment in V5EnchantRegistry.getRegistered()) {
            EnchantmentApi.unregisterEnchantment(enchantment.bukkitEnchantment.key)
            EnchantmentApi.registerEnchantment(CAEEV5Enchantment(enchantment))
        }

        CustomAnvil.instance.logger.info("Excellent Enchants should now work as expected !")
    }

    private lateinit var anvilListener: V5AnvilListener

    private lateinit var handleRechargeMethod: Method
    private lateinit var handleCombineMethod: Method

    fun redirectListeners() {
        val toUnregister = ArrayList<RegisteredListener>()
        // get required PrepareAnvilEvent listener
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {
            val listener = registeredListener.listener

            if (listener is V5AnvilListener) {
                this.anvilListener = listener
                toUnregister.add(registeredListener)
            }
        }

        for (listener in toUnregister) {
            PrepareAnvilEvent.getHandlerList().unregister(listener)
        }

        // Unregister inventory click event
        InventoryClickEvent.getHandlerList().unregister(this.anvilListener)

        findAnvilFunctions()
    }

    private fun findAnvilFunctions() {
        this.handleRechargeMethod = this.anvilListener.javaClass.getDeclaredMethod(
            "handleRecharge",
            PrepareAnvilEvent::class.java, ItemStack::class.java, ItemStack::class.java
        )
        this.handleRechargeMethod.setAccessible(true)

        this.handleCombineMethod = this.anvilListener.javaClass.getDeclaredMethod(
            "handleCombine",
            PrepareAnvilEvent::class.java, ItemStack::class.java, ItemStack::class.java, ItemStack::class.java
        )
        this.handleCombineMethod.setAccessible(true)

    }

    fun treatAnvilResult(event: PrepareAnvilEvent, result: ItemStack) {
        val first: ItemStack = treatInput(event.inventory.getItem(0))
        val second: ItemStack = treatInput(event.inventory.getItem(1))

        handleCombineMethod.invoke(this.anvilListener, event, first, second, result)
    }

    fun testAnvilResult(event: InventoryClickEvent): Any {
        if (event.inventory.getItem(2) != null) {
            anvilListener.onClickAnvil(event)
            return event.inventory.getItem(2) == null
        }

        return false
    }

    private fun treatInput(item: ItemStack?): ItemStack {
        if (item == null) return ItemStack(Material.AIR)
        return item
    }

}
