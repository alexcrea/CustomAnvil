package xyz.alexcrea.cuanvil.dependency

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.RegisteredListener
import su.nightexpress.excellentenchants.enchantment.impl.universal.CurseOfFragilityEnchant
import su.nightexpress.excellentenchants.enchantment.listener.AnvilListener
import su.nightexpress.excellentenchants.registry.EnchantRegistry
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEEEnchantment
import java.lang.reflect.Method

class ExcellentEnchantsDependency {

    init {
        CustomAnvil.instance.logger.info("Excellent Enchants Detected !")
    }

    fun registerEnchantments() {
        CustomAnvil.instance.logger.info("Preparing Excellent Enchants compatibility...")

        for (enchantment in EnchantRegistry.getRegistered()) {
            EnchantmentApi.unregisterEnchantment(enchantment.bukkitEnchantment.key) // As excellent enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
            EnchantmentApi.registerEnchantment(CAEEEnchantment(enchantment))
        }

        CustomAnvil.instance.logger.info("Excellent Enchants should now work as expected !")
    }

    private lateinit var fragilityCurse: CurseOfFragilityEnchant

    private lateinit var anvilListener: AnvilListener
    private lateinit var handleRechargeMethod: Method
    private lateinit var handleCombineMethod: Method

    fun redirectListeners() {
        val toUnregister = ArrayList<RegisteredListener>()
        // get required PrepareAnvilEvent listener
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {
            val listener = registeredListener.listener

            if(listener is CurseOfFragilityEnchant){
                this.fragilityCurse = listener
                toUnregister.add(registeredListener)
            }

            if(listener is AnvilListener){
                this.anvilListener = listener;
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
        this.handleRechargeMethod = AnvilListener::class.java.getDeclaredMethod("handleRecharge",
            PrepareAnvilEvent::class.java, ItemStack::class.java, ItemStack::class.java)
        this.handleRechargeMethod.setAccessible(true)

        this.handleCombineMethod = AnvilListener::class.java.getDeclaredMethod("handleCombine",
            PrepareAnvilEvent::class.java, ItemStack::class.java, ItemStack::class.java, ItemStack::class.java)
        this.handleCombineMethod.setAccessible(true)

    }

    fun testPrepareAnvil(event: PrepareAnvilEvent): Boolean {
        if(event.result != null){
            this.fragilityCurse.onItemAnvil(event)
            if(event.result == null) return true
        }

        val first: ItemStack = treatInput(event.inventory.getItem(0))
        val second: ItemStack = treatInput(event.inventory.getItem(1))

        return handleRechargeMethod.invoke(this.anvilListener, event, first, second) as Boolean
    }

    fun treatAnvilResult(event: PrepareAnvilEvent, result: ItemStack) {
        val first: ItemStack = treatInput(event.inventory.getItem(0))
        val second: ItemStack = treatInput(event.inventory.getItem(1))

        handleCombineMethod.invoke(this.anvilListener, event, first, second, result)
    }

    fun testAnvilResult(event: InventoryClickEvent): Any {
        if(event.inventory.getItem(2) != null){
            this.anvilListener.onClickAnvil(event)
            return event.inventory.getItem(2) == null
        }

        return false;
    }

    fun treatInput(item: ItemStack?): ItemStack {
        if(item == null) return ItemStack(Material.AIR)
        return item
    }

}
