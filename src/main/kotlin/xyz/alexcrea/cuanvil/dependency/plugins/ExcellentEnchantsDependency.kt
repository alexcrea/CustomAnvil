package xyz.alexcrea.cuanvil.dependency.plugins

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.RegisteredListener
import su.nightexpress.excellentenchants.enchantment.impl.universal.CurseOfFragilityEnchant
import su.nightexpress.excellentenchants.enchantment.listener.AnvilListener
import su.nightexpress.excellentenchants.enchantment.listener.EnchantAnvilListener
import su.nightexpress.excellentenchants.registry.EnchantRegistry
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEEEnchantment
import xyz.alexcrea.cuanvil.enchant.wrapped.CALegacyEEEnchantment
import java.lang.reflect.Method

class ExcellentEnchantsDependency {

    private val isModern: Boolean

    init {
        CustomAnvil.instance.logger.info("Excellent Enchants Detected !")

        var isModern = true;
        try {
            Class.forName("su.nightexpress.excellentenchants.enchantment.listener.AnvilListener")
        } catch (ignored: ClassNotFoundException) {
            isModern = false
        }

        this.isModern = isModern
    }

    fun registerEnchantments() {
        CustomAnvil.instance.logger.info("Preparing Excellent Enchants compatibility...")

        // As excellent enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
        if (this.isModern) {
            for (enchantment in EnchantRegistry.getRegistered()) {
                EnchantmentApi.unregisterEnchantment(enchantment.bukkitEnchantment.key)
                EnchantmentApi.registerEnchantment(CAEEEnchantment(enchantment))
            }
        } else {
            for (enchantment in su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry.getRegistered()) {
                EnchantmentApi.unregisterEnchantment(enchantment.enchantment.key)
                EnchantmentApi.registerEnchantment(CALegacyEEEnchantment(enchantment))
            }
        }

        CustomAnvil.instance.logger.info("Excellent Enchants should now work as expected !")
    }

    private var fragilityCurse: CurseOfFragilityEnchant? = null

    private var modernAnvilListener: AnvilListener? = null
    private var legacyAnvilListener: EnchantAnvilListener? = null
    private lateinit var usedAnvilListener: Listener

    private lateinit var handleRechargeMethod: Method
    private lateinit var handleCombineMethod: Method

    fun redirectListeners() {
        val toUnregister = ArrayList<RegisteredListener>()
        // get required PrepareAnvilEvent listener
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {
            val listener = registeredListener.listener

            if (listener is CurseOfFragilityEnchant) {
                this.fragilityCurse = listener
                toUnregister.add(registeredListener)
            }

            if (this.isModern) {
                if (listener is AnvilListener) {
                    this.modernAnvilListener = listener;
                    toUnregister.add(registeredListener)
                }
            } else {
                if (listener is EnchantAnvilListener) {
                    this.legacyAnvilListener = listener;
                    toUnregister.add(registeredListener)
                }
            }

        }

        for (listener in toUnregister) {
            PrepareAnvilEvent.getHandlerList().unregister(listener)
        }

        if (this.isModern) {
            this.usedAnvilListener = this.modernAnvilListener!!
        } else {
            this.usedAnvilListener = this.legacyAnvilListener!!
        }

        // Unregister inventory click event
        InventoryClickEvent.getHandlerList().unregister(this.usedAnvilListener)

        findAnvilFunctions()
    }

    private fun findAnvilFunctions() {
        this.handleRechargeMethod = this.usedAnvilListener.javaClass.getDeclaredMethod(
            "handleRecharge",
            PrepareAnvilEvent::class.java, ItemStack::class.java, ItemStack::class.java
        )
        this.handleRechargeMethod.setAccessible(true)

        this.handleCombineMethod = this.usedAnvilListener.javaClass.getDeclaredMethod(
            "handleCombine",
            PrepareAnvilEvent::class.java, ItemStack::class.java, ItemStack::class.java, ItemStack::class.java
        )
        this.handleCombineMethod.setAccessible(true)

    }

    fun testPrepareAnvil(event: PrepareAnvilEvent): Boolean {
        if (event.result != null) {
            this.fragilityCurse?.onItemAnvil(event)
            if (event.result == null) return true
        }

        val first: ItemStack = treatInput(event.inventory.getItem(0))
        val second: ItemStack = treatInput(event.inventory.getItem(1))

        return handleRechargeMethod.invoke(this.usedAnvilListener, event, first, second) as Boolean
    }

    fun treatAnvilResult(event: PrepareAnvilEvent, result: ItemStack) {
        val first: ItemStack = treatInput(event.inventory.getItem(0))
        val second: ItemStack = treatInput(event.inventory.getItem(1))

        handleCombineMethod.invoke(this.usedAnvilListener, event, first, second, result)
    }

    fun testAnvilResult(event: InventoryClickEvent): Any {
        if (event.inventory.getItem(2) != null) {
            if (this.isModern) {
                this.modernAnvilListener!!.onClickAnvil(event)
            } else {
                this.legacyAnvilListener!!.onClickAnvil(event)
            }
            return event.inventory.getItem(2) == null
        }

        return false;
    }

    private fun treatInput(item: ItemStack?): ItemStack {
        if (item == null) return ItemStack(Material.AIR)
        return item
    }

}
