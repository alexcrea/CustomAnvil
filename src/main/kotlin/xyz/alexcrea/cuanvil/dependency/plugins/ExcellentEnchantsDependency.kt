package xyz.alexcrea.cuanvil.dependency.plugins

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.RegisteredListener
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEEPreV5Enchantment
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEEV5Enchantment
import xyz.alexcrea.cuanvil.enchant.wrapped.CALegacyEEEnchantment
import java.lang.reflect.Method
import su.nightexpress.excellentenchants.api.EnchantRegistry as V5EnchantRegistry
import su.nightexpress.excellentenchants.enchantment.impl.universal.CurseOfFragilityEnchant as LegacyCurseOfFragilityEnchant
import su.nightexpress.excellentenchants.manager.listener.AnvilListener as V5AnvilListener
import su.nightexpress.excellentenchants.enchantment.listener.AnvilListener as PreV5AnvilListener
import su.nightexpress.excellentenchants.enchantment.listener.EnchantAnvilListener as LegacyAnvilListener
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry as LegacyEnchantRegistry
import su.nightexpress.excellentenchants.registry.EnchantRegistry as PreV5EnchantRegistry

// I don't like that I need to support older version. if I could just drop older support it would be sooo nice
class ExcellentEnchantsDependency {

    enum class ListenerVersion(val classPath: String) {
        V5("su.nightexpress.excellentenchants.manager.listener.AnvilListener"),
        PRE_V5("su.nightexpress.excellentenchants.enchantment.listener.AnvilListener"),
        LEGACY("su.nightexpress.excellentenchants.enchantment.listener.EnchantAnvilListener"),
    }

    private val listenerVersion: ListenerVersion?
    private val isModernCurseOfFragility: Boolean

    init {
        CustomAnvil.instance.logger.info("Excellent Enchants Detected !")

        var listenerVersion: ListenerVersion? = null
        for (value in ListenerVersion.entries) {
            try {
                Class.forName(value.classPath)

                listenerVersion = value
                break
            } catch (ignored: ClassNotFoundException) {
            }
        }

        if(listenerVersion == null){
            CustomAnvil.instance.logger.severe("Found issue with listener of Excellent Enchants. compatiblity is broken. please contact CustomAnvil devs")
        }

        var isModernCurseOfFragility = true
        try {
            Class.forName("su.nightexpress.excellentenchants.enchantment.universal.CurseOfFragilityEnchant")
        } catch (ignored: ClassNotFoundException) {
            isModernCurseOfFragility = false
        }

        this.listenerVersion = listenerVersion
        this.isModernCurseOfFragility = isModernCurseOfFragility
    }

    fun registerEnchantments() {
        CustomAnvil.instance.logger.info("Preparing Excellent Enchants compatibility...")

        // As excellent enchants is loaded before custom anvil and register enchantment to registry, we need to unregister old "vanilla" enchant.
        when (listenerVersion) {
            ListenerVersion.V5 -> {
                for (enchantment in V5EnchantRegistry.getRegistered()) {
                    EnchantmentApi.unregisterEnchantment(enchantment.bukkitEnchantment.key)
                    EnchantmentApi.registerEnchantment(CAEEV5Enchantment(enchantment))
                }
            }

            ListenerVersion.PRE_V5 -> {
                for (enchantment in PreV5EnchantRegistry.getRegistered()) {
                    EnchantmentApi.unregisterEnchantment(enchantment.bukkitEnchantment.key)
                    EnchantmentApi.registerEnchantment(CAEEPreV5Enchantment(enchantment))
                }
            }

            ListenerVersion.LEGACY -> {
                for (enchantment in LegacyEnchantRegistry.getRegistered()) {
                    EnchantmentApi.unregisterEnchantment(enchantment.enchantment.key)
                    EnchantmentApi.registerEnchantment(CALegacyEEEnchantment(enchantment))
                }
            }

            null -> return

        }

        CustomAnvil.instance.logger.info("Excellent Enchants should now work as expected !")
    }

    private var legacyFragilityCurse: LegacyCurseOfFragilityEnchant? = null

    private var v5AnvilListener: V5AnvilListener? = null
    private var preV5AnvilListener: PreV5AnvilListener? = null
    private var legacyAnvilListener: LegacyAnvilListener? = null
    private lateinit var usedAnvilListener: Listener

    private lateinit var handleRechargeMethod: Method
    private lateinit var handleCombineMethod: Method

    fun redirectListeners() {
        val toUnregister = ArrayList<RegisteredListener>()
        // get required PrepareAnvilEvent listener
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {
            val listener = registeredListener.listener

            if (!isModernCurseOfFragility) {
                if (listener is LegacyCurseOfFragilityEnchant) {
                    this.legacyFragilityCurse = listener
                    toUnregister.add(registeredListener)
                }
            }

            when (listenerVersion) {
                ListenerVersion.V5 -> {
                    if (listener is V5AnvilListener) {
                        this.v5AnvilListener = listener
                        toUnregister.add(registeredListener)
                    }
                }
                ListenerVersion.PRE_V5 -> {
                    if (listener is PreV5AnvilListener) {
                        this.preV5AnvilListener = listener
                        toUnregister.add(registeredListener)
                    }
                }
                ListenerVersion.LEGACY -> {
                    if (listener is LegacyAnvilListener) {
                        this.legacyAnvilListener = listener
                        toUnregister.add(registeredListener)
                    }
                }
                null -> {
                    }
            }

        }

        for (listener in toUnregister) {
            PrepareAnvilEvent.getHandlerList().unregister(listener)
        }

        when (listenerVersion) {
            ListenerVersion.V5 -> this.usedAnvilListener = v5AnvilListener!!
            ListenerVersion.PRE_V5 -> this.usedAnvilListener = preV5AnvilListener!!
            ListenerVersion.LEGACY -> this.usedAnvilListener = legacyAnvilListener!!
            null -> {}
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
            if (!isModernCurseOfFragility) {
                this.legacyFragilityCurse?.onItemAnvil(event)
            }
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
            when (listenerVersion) {
                ListenerVersion.V5 -> v5AnvilListener!!.onClickAnvil(event)
                ListenerVersion.PRE_V5 -> preV5AnvilListener!!.onClickAnvil(event)
                ListenerVersion.LEGACY -> legacyAnvilListener!!.onClickAnvil(event)
                null -> {}
            }
            return event.inventory.getItem(2) == null
        }

        return false
    }

    private fun treatInput(item: ItemStack?): ItemStack {
        if (item == null) return ItemStack(Material.AIR)
        return item
    }

}
