package xyz.alexcrea.cuanvil.dependency

import io.delilaheve.CustomAnvil
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.RegisteredListener
import valorless.havenbags.BagSkin
import valorless.havenbags.BagUpgrade
import valorless.havenbags.HavenBags
import valorless.havenbags.prevention.EquipPrevention
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener
import xyz.alexcrea.cuanvil.util.AnvilXpUtil

class HavenBagsDependency {

    init {
        CustomAnvil.instance.logger.info("Heaven Bags Detected !")
    }

    private lateinit var bagUpgrade: BagUpgrade
    private lateinit var bagSkin: BagSkin

    fun redirectListeners() {
        val toUnregister = ArrayList<RegisteredListener>()
        // get required PrepareAnvilEvent listener
        for (registeredListener in PrepareAnvilEvent.getHandlerList().registeredListeners) {
            val listener = registeredListener.listener

            if (listener is BagUpgrade) {
                bagUpgrade = listener
                toUnregister.add(registeredListener)
            }

            if (listener is BagSkin) {
                bagSkin = listener
                toUnregister.add(registeredListener)
            }
        }

        for (listener in toUnregister) {
            PrepareAnvilEvent.getHandlerList().unregister(listener)
            InventoryClickEvent.getHandlerList().unregister(listener)
        }

    }

    fun testPrepareAnvil(event: PrepareAnvilEvent, player: HumanEntity): Boolean {
        val previousResult = event.result
        event.result = null

        // Test if event change the result
        bagSkin.onPrepareAnvil(event)
        if (event.result != null) {
            CustomAnvil.log("Detected pre anvil heaven bag anvil skin.")
            AnvilXpUtil.setAnvilInvXp(event.inventory, event.view, player, event.inventory.repairCost)
            return true
        }

        bagUpgrade.onPrepareAnvil(event)
        if (event.result != null) {
            CustomAnvil.log("Detected pre anvil heaven bag anvil upgrade.")
            AnvilXpUtil.setAnvilInvXp(event.inventory, event.view, player, event.inventory.repairCost)
            return true
        }

        event.result = previousResult
        return false
    }

    fun testAnvilResult(event: InventoryClickEvent, inventory: AnvilInventory): Boolean {
        val result = inventory.getItem(PrepareAnvilListener.ANVIL_OUTPUT_SLOT)?.clone()

        if (HavenBags.IsBag(result)) {
            CustomAnvil.log("Detected anvil click haven bag bypass.")
            bagUpgrade.onInventoryClick(event)
            bagSkin.onInventoryClick(event)
            return true;
        }

        return false
    }

    private fun testAnvilInventoryChange(inventory: AnvilInventory, previous: ItemStack?): Boolean {
        val currentResult = inventory.getItem(PrepareAnvilListener.ANVIL_OUTPUT_SLOT)

        return currentResult == previous
    }

}
