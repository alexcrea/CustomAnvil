package xyz.alexcrea.cuanvil.dependency

import cz.kominekjan.disenchantment.events.ItemClickEvent
import cz.kominekjan.disenchantment.events.ItemEvent
import cz.kominekjan.disenchantment.events.SplitBookClickEvent
import cz.kominekjan.disenchantment.events.SplitBookEvent
import io.delilaheve.AnvilEventListener
import io.delilaheve.CustomAnvil
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.RegisteredListener
import xyz.alexcrea.cuanvil.util.AnvilXpUtil

class DisenchantmentDependency {

    init {
        CustomAnvil.instance.logger.info("Disenchantment Detected !")
    }

    private lateinit var splitEvent: SplitBookEvent
    private lateinit var itemEvent: ItemEvent

    private lateinit var splitBookClickEvent: SplitBookClickEvent
    private lateinit var itemClickEvent: ItemClickEvent

    fun redirectListeners() {

        val toUnregister = ArrayList<RegisteredListener>()
        // get required PrepareAnvilEvent listener
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {
            val listener = registeredListener.listener

            if(listener is SplitBookEvent){
                this.splitEvent = listener
                toUnregister.add(registeredListener)
            }

            if(listener is ItemEvent){
                itemEvent = listener
                toUnregister.add(registeredListener)
            }

        }

        for (listener in toUnregister) {
            PrepareAnvilEvent.getHandlerList().unregister(listener)
        }
        toUnregister.clear()

        // get required InventoryClickEvent listener
        for (registeredListener in InventoryClickEvent.getHandlerList().registeredListeners) {
            val listener = registeredListener.listener

            if(listener is SplitBookClickEvent){
                splitBookClickEvent = listener
                toUnregister.add(registeredListener)
            }

            if(listener is ItemClickEvent){
                itemClickEvent = listener
                toUnregister.add(registeredListener)
            }

        }

        for (listener in toUnregister) {
            InventoryClickEvent.getHandlerList().unregister(listener)
        }

    }

    fun testPrepareAnvil(event: PrepareAnvilEvent): Boolean {
        val previousResult = event.result
        event.result = null

        // Test if event change the result
        itemEvent.onDisenchantmentEvent(event)

        if(event.result != null) {
            CustomAnvil.log("Detected pre anvil item extract bypass.")
            AnvilXpUtil.setAnvilInvXp(event.inventory, event.view, event.inventory.repairCost)
            return true
        }

        splitEvent.onDisenchantmentEvent(event)
        if(event.result != null) {
            CustomAnvil.log("Detected pre anvil split enchant bypass.")
            AnvilXpUtil.setAnvilInvXp(event.inventory, event.view, event.inventory.repairCost)
            return true
        }

        event.result = previousResult
        return false
    }

    fun testAnvilResult(event: InventoryClickEvent, inventory: AnvilInventory): Boolean {
        val previousResultSlot = inventory.getItem(AnvilEventListener.ANVIL_OUTPUT_SLOT)?.clone()

        // Test event if change the result
        itemClickEvent.onDisenchantmentClickEvent(event)
        if(!testAnvilInventoryChange(inventory, previousResultSlot) || event.isCancelled) {
            CustomAnvil.log("Detected anvil click item extract bypass.")
            return true
        }

        splitBookClickEvent.onDisenchantmentClickEvent(event)
        if(!testAnvilInventoryChange(inventory, previousResultSlot) || event.isCancelled) {
            CustomAnvil.log("Detected anvil click split enchant bypass.")
            return true
        }

        return false
    }

    private fun testAnvilInventoryChange(inventory: AnvilInventory, previous: ItemStack?): Boolean {
        val currentResult = inventory.getItem(AnvilEventListener.ANVIL_OUTPUT_SLOT)

        return currentResult == previous
    }

}