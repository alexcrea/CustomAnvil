package xyz.alexcrea.cuanvil.listener

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.ItemUtil.canMergeWith
import io.delilaheve.util.ItemUtil.unitRepair
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_LEFT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_RIGHT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_OUTPUT_SLOT
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe
import xyz.alexcrea.cuanvil.util.AnvilLoreEditUtil
import xyz.alexcrea.cuanvil.util.AnvilUseType
import xyz.alexcrea.cuanvil.util.AnvilXpUtil
import xyz.alexcrea.cuanvil.util.CustomRecipeUtil
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil
import xyz.alexcrea.cuanvil.util.config.LoreEditType
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.math.min

class AnvilResultListener : Listener {

    companion object {
        // static slot container
        private val NO_SLOT = SlotContainer(SlotType.NO_SLOT, 0)
        private val CURSOR_SLOT = SlotContainer(SlotType.CURSOR, 0)
    }

    /**
     * Event handler logic for when a player is trying to pull an item out of the anvil
     */
    @EventHandler(ignoreCancelled = true)
    fun anvilExtractionCheck(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return
        val inventory = event.inventory as? AnvilInventory ?: return

        if (event.rawSlot != ANVIL_OUTPUT_SLOT) {
            return
        }

        // Test if the event should bypass custom anvil.
        if (DependencyManager.tryClickAnvilResultBypass(event, inventory)) return

        val output = inventory.getItem(ANVIL_OUTPUT_SLOT) ?: return
        val leftItem = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val rightItem = inventory.getItem(ANVIL_INPUT_RIGHT)

        if (GameMode.CREATIVE != player.gameMode && inventory.repairCost >= inventory.maximumRepairCost) {
            event.result = Event.Result.DENY
            return
        }

        // Test custom recipe
        val recipe = CustomRecipeUtil.getCustomRecipe(leftItem, rightItem)
        if (recipe != null) {
            event.result = Event.Result.ALLOW
            onCustomCraft(
                event, recipe, player,
                leftItem, rightItem, output, inventory
            )
            return
        }

        // Do not continue if there was no change
        if ((output == inventory.getItem(ANVIL_INPUT_LEFT))) {
            event.result = Event.Result.DENY
            return
        }

        // Rename
        if (rightItem == null) {
            event.result = Event.Result.ALLOW
            return
        }

        // Merge
        val canMerge = leftItem.canMergeWith(rightItem)
        if (canMerge) {
            event.result = Event.Result.ALLOW
            return
        }

        // Unit repair
        val unitRepairResult = leftItem.getRepair(rightItem)
        if (unitRepairResult != null) {
            onUnitRepairExtract(
                leftItem, rightItem, output,
                unitRepairResult, event, player, inventory
            )
            return
        }

        // For lore edit
        if (handleBookLoreEdit(event, inventory, player, leftItem, rightItem, output)) {
            return
        } else if (handlePaperLoreEdit(event, inventory, player, leftItem, rightItem, output)) {
            return
        }

        // Else there was no working situation somehow so we deny
        event.result = Event.Result.DENY
    }

    private fun onCustomCraft(
        event: InventoryClickEvent,
        recipe: AnvilCustomRecipe,
        player: Player,
        leftItem: ItemStack,
        rightItem: ItemStack?,
        output: ItemStack,
        inventory: AnvilInventory
    ) {
        event.result = Event.Result.DENY

        if (recipe.leftItem == null) return // in case it changed

        val amount = CustomRecipeUtil.getCustomRecipeAmount(recipe, leftItem, rightItem)
        val xpCost = amount * recipe.xpCostPerCraft

        CustomAnvil.log("gamemode: ${player.gameMode != GameMode.CREATIVE}, cost: $xpCost, level: ${player.level}, result: ${player.level < xpCost}")
        if ((player.gameMode != GameMode.CREATIVE) && (player.level < xpCost)) return

        // We give the item manually
        // But first we check if we should give the item
        val slotDestination = getActionSlot(event, player)
        if (slotDestination.type == SlotType.NO_SLOT) return

        // Handle not creative middle click...
        if (event.click != ClickType.MIDDLE &&
            !handleCustomCraftClick(event, recipe, inventory, player, leftItem, rightItem, amount, xpCost)
        ) return

        // Finally, we add the item to the player
        if (slotDestination.type == SlotType.CURSOR) {
            player.setItemOnCursor(output)
        } else {// We assume SlotType == SlotType.INVENTORY
            player.inventory.setItem(slotDestination.slot, output)
        }
    }

    private fun handleCustomCraftClick(
        event: InventoryClickEvent, recipe: AnvilCustomRecipe,
        inventory: AnvilInventory, player: Player,
        leftItem: ItemStack, rightItem: ItemStack?,
        amount: Int, xpCost: Int
    ): Boolean {
        // We remove what should be removed
        if (rightItem != null) {
            if (recipe.rightItem == null) return false// in case it changed

            rightItem.amount -= amount * recipe.rightItem!!.amount
            inventory.setItem(ANVIL_INPUT_RIGHT, rightItem)
        }

        leftItem.amount -= amount * recipe.leftItem!!.amount
        inventory.setItem(ANVIL_INPUT_LEFT, leftItem)

        if (player.gameMode != GameMode.CREATIVE) {
            player.level -= xpCost
        }

        // Then we try to find the new values for the anvil
        val newAmount = CustomRecipeUtil.getCustomRecipeAmount(recipe, leftItem, rightItem)

        CustomAnvil.verboseLog("new amount is $newAmount")
        if (newAmount <= 0 || recipe.exactCount) {
            inventory.setItem(ANVIL_OUTPUT_SLOT, null)
        } else {
            val resultItem: ItemStack = recipe.resultItem!!.clone()
            resultItem.amount *= newAmount

            val newXp = newAmount * newAmount

            inventory.repairCost = newXp
            event.view.setProperty(InventoryView.Property.REPAIR_COST, newXp)

            inventory.setItem(ANVIL_OUTPUT_SLOT, resultItem)

            player.updateInventory()
        }
        return true
    }

    private fun extractAnvilResult(
        event: InventoryClickEvent,
        player: Player,
        inventory: AnvilInventory,
        leftItem: ItemStack?,
        leftRemoveCount: Int,
        rightItem: ItemStack?,
        rightRemoveCount: Int,
        output: ItemStack,
        repairCost: Int,
    ): Boolean {
        // To avoid vanilla, we cancel the event
        event.result = Event.Result.DENY
        event.isCancelled = true

        // Assumed if player do not have enough xp then it returned MIN_VALUE
        if (repairCost == Int.MIN_VALUE) return false

        // Where should we get the item
        val slotDestination = getActionSlot(event, player)
        if (slotDestination.type == SlotType.NO_SLOT) return false

        // If not creative middle click...
        if (event.click != ClickType.MIDDLE) {
            // We remove what should be removed
            if (leftItem != null) leftItem.amount -= leftRemoveCount
            inventory.setItem(ANVIL_INPUT_LEFT, leftItem)

            if (rightItem != null) rightItem.amount -= rightRemoveCount
            inventory.setItem(ANVIL_INPUT_RIGHT, rightItem)

            inventory.setItem(ANVIL_OUTPUT_SLOT, null)
            player.level -= repairCost
        }

        // Finally, we add the item to the player
        if (SlotType.CURSOR == slotDestination.type) {
            player.setItemOnCursor(output)
        } else {// We assume SlotType == SlotType.INVENTORY
            player.inventory.setItem(slotDestination.slot, output)
        }

        // TODO probably anvil damage & sound here ??
        return true
    }

    private fun onUnitRepairExtract(
        leftItem: ItemStack,
        rightItem: ItemStack,
        output: ItemStack,
        unitRepairResult: Double,
        event: InventoryClickEvent,
        player: Player,
        inventory: AnvilInventory
    ) {
        val resultCopy = leftItem.clone()
        val resultAmount = resultCopy.unitRepair(
            rightItem.amount, unitRepairResult
        )

        // Get repair cost
        val repairCost = getUnitRepairCost(inventory, player, leftItem, output, resultCopy, resultAmount)

        // And then we give the item manually
        extractAnvilResult(
            event, player, inventory,
            null, 0,
            rightItem, resultAmount,
            resultCopy, repairCost
        )
    }

    private fun getUnitRepairCost(
        inventory: AnvilInventory, player: Player,
        leftItem: ItemStack, output: ItemStack,
        resultCopy: ItemStack, resultAmount: Int
    ): Int {
        if (player.gameMode == GameMode.CREATIVE) return 0

        var repairCost = 0
        // Get repairCost
        leftItem.itemMeta?.let { leftMeta ->
            val leftName = leftMeta.displayName
            output.itemMeta?.let {
                // Rename cost
                if (!leftName.contentEquals(it.displayName)) {
                    repairCost += ConfigOptions.itemRenameCost

                    // Color cost
                    if (it.displayName.contains('§')) {
                        repairCost += ConfigOptions.useOfColorCost
                    }
                }
            }
        }

        repairCost += AnvilXpUtil.calculatePenalty(leftItem, null, resultCopy, AnvilUseType.UNIT_REPAIR)
        repairCost += resultAmount * ConfigOptions.unitRepairCost

        if (
            !ConfigOptions.doRemoveCostLimit &&
            ConfigOptions.doCapCost
        ) {
            repairCost = min(repairCost, ConfigOptions.maxAnvilCost)
        }

        if ((inventory.maximumRepairCost <= repairCost)
            || (player.level < repairCost)
        ) return Int.MIN_VALUE

        return repairCost
    }

    private fun handleBookLoreEdit(
        event: InventoryClickEvent,
        inventory: AnvilInventory,
        player: Player,
        leftItem: ItemStack,
        rightItem: ItemStack,
        output: ItemStack,
    ): Boolean {
        if (Material.WRITABLE_BOOK != rightItem.type) return false
        val bookMeta = rightItem.itemMeta as BookMeta? ?: return false

        val editType = AnvilLoreEditUtil.bookLoreEditIsAppend(leftItem, rightItem) ?: return false

        val xpCost = AtomicInteger()
        if (editType) {
            if (output != AnvilLoreEditUtil.handleLoreAppendByBook(player, leftItem, bookMeta, xpCost)) return false

            // Remove pages to book
            val clearedBook: ItemStack?
            if (LoreEditType.APPEND_BOOK.doConsume) {
                clearedBook = null
            } else {
                clearedBook = rightItem.clone()
                bookMeta.pages = Collections.emptyList()
                clearedBook.itemMeta = bookMeta
            }

            return extractAnvilResult(
                event, player, inventory,
                null, 0,
                clearedBook, 0,
                output, xpCost.get()
            )
        } else {
            if (output != AnvilLoreEditUtil.handleLoreRemoveByBook(player, leftItem, xpCost)) return false

            // fill book meta
            val meta = leftItem.itemMeta
            if (meta == null || !meta.hasLore()) return false
            val lore = ArrayList<String>(meta.lore!!)
            if (lore.isEmpty()) return false

            val rightCopy : ItemStack?
            if (LoreEditType.APPEND_PAPER.doConsume) {
                rightCopy = null
            } else {
                // Uncolor the page
                AnvilLoreEditUtil.uncolorLines(player, lore, LoreEditType.REMOVE_BOOK)

                val bookPage = StringBuilder()
                lore.forEach {
                    if (bookPage.isNotEmpty()) bookPage.append('\n')
                    bookPage.append(it)
                }

                val resultPage = bookPage.toString()
                //TODO maybe check page size ? bc it may be too big ???

                rightCopy = rightItem.clone()
                bookMeta.setPages(resultPage)
                rightCopy.itemMeta = bookMeta
            }

            return extractAnvilResult(
                event, player, inventory,
                null, 0,
                rightCopy, 0,
                output, xpCost.get()
            )
        }
    }

    private fun handlePaperLoreEdit(
        event: InventoryClickEvent,
        inventory: AnvilInventory,
        player: Player,
        leftItem: ItemStack,
        rightItem: ItemStack,
        output: ItemStack,
    ): Boolean {
        if (Material.PAPER != rightItem.type) return false
        val paperMeta = rightItem.itemMeta ?: return false

        val editType = AnvilLoreEditUtil.paperLoreEditIsAppend(leftItem, rightItem) ?: return false

        val xpCost = AtomicInteger()
        if (editType) {
            if (output != AnvilLoreEditUtil.handleLoreAppendByPaper(player, leftItem, rightItem, xpCost)) return false

            val paperCopy: ItemStack?
            if (LoreEditType.APPEND_PAPER.doConsume) {
                paperCopy = null
            } else {
                // Remove custom name to paper
                paperCopy = rightItem.clone()
                paperCopy.amount = 1
                paperMeta.setDisplayName(null)
                paperCopy.itemMeta = paperMeta
            }

            return if (rightItem.amount > 1) {
                extractAnvilResult(
                    event, player, inventory,
                    paperCopy, 0,
                    rightItem, 1,
                    output, xpCost.get()
                )
            } else {
                extractAnvilResult(
                    event, player, inventory,
                    null, 0,
                    paperCopy, 0,
                    output, xpCost.get()
                )
            }
        } else {
            if (output != AnvilLoreEditUtil.handleLoreRemoveByPaper(player, leftItem, xpCost)) return false

            val leftMeta = leftItem.itemMeta
            if (leftMeta == null || !leftMeta.hasLore()) return false
            val lore = leftMeta.lore!!
            if (lore.isEmpty()) return false

            // Create result item
            val rightClone: ItemStack?
            if(LoreEditType.REMOVE_PAPER.doConsume){
                rightClone = null
            }else{
                val removeEnd = LoreEditConfigUtil.paperLoreOrderIsEnd
                var line = if (removeEnd) lore[lore.size - 1]
                else lore[0]

                // Overkill but uncolor the line
                val tempList = ArrayList<String>(1)
                tempList.add(line)
                AnvilLoreEditUtil.uncolorLines(player, tempList, LoreEditType.REMOVE_PAPER)
                line = tempList[0]

                rightClone = rightItem.clone()
                rightClone.amount = 1

                val resultMeta = rightClone.itemMeta ?: return false
                resultMeta.setDisplayName(line)
                rightClone.itemMeta = resultMeta
            }

            return if (rightItem.amount > 1) {
                extractAnvilResult(
                    event, player, inventory,
                    rightClone, 0,
                    rightItem, 1,
                    output, xpCost.get()
                )
            } else {
                extractAnvilResult(
                    event, player, inventory,
                    null, 0,
                    rightClone, 0,
                    output, xpCost.get()
                )
            }
        }

    }

    /**
     * Get the destination slot or "NO_SLOT" slot container if there is no slot available
     */
    private fun getActionSlot(event: InventoryClickEvent, player: Player): SlotContainer {
        if (event.isShiftClick) {
            val inventory = player.inventory
            val firstEmpty = inventory.firstEmpty()
            if (firstEmpty == -1) {
                return NO_SLOT
            }
            //check hotbare full
            var slotIndex = 8
            while (slotIndex >= 0 && ((inventory.getItem(slotIndex)?.type ?: Material.AIR) != Material.AIR)) {
                slotIndex--
            }
            if (slotIndex >= 0) {
                return SlotContainer(SlotType.INVENTORY, slotIndex)
            }
            slotIndex = 35 //4*9 - 1 (max of player inventory)
            while (slotIndex >= 9 && ((inventory.getItem(slotIndex)?.type ?: Material.AIR) != Material.AIR)) {
                slotIndex--
            }
            if (slotIndex < 9) {
                return NO_SLOT
            }
            return SlotContainer(SlotType.INVENTORY, slotIndex)
        } else if (player.itemOnCursor.type != Material.AIR) return NO_SLOT
        return CURSOR_SLOT
    }

    private class SlotContainer(val type: SlotType, val slot: Int)
    private enum class SlotType {
        CURSOR,
        INVENTORY,
        NO_SLOT

    }

}
