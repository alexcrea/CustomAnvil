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
import xyz.alexcrea.cuanvil.dependency.economy.EconomyManager
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.setComponentDisplayName
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_LEFT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_RIGHT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_OUTPUT_SLOT
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe
import xyz.alexcrea.cuanvil.util.AnvilLoreEditUtil
import xyz.alexcrea.cuanvil.util.AnvilUseType
import xyz.alexcrea.cuanvil.util.AnvilXpUtil
import xyz.alexcrea.cuanvil.util.AnvilXpUtil.AnvilCost
import xyz.alexcrea.cuanvil.util.CustomRecipeUtil
import xyz.alexcrea.cuanvil.util.MiniMessageUtil
import xyz.alexcrea.cuanvil.util.UnitRepairUtil.getRepair
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil
import xyz.alexcrea.cuanvil.util.config.LoreEditType
import java.util.*
import java.util.concurrent.atomic.AtomicReference
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
        val inventory = event.inventory as? AnvilInventory ?: return

        if (event.rawSlot != ANVIL_OUTPUT_SLOT) {
            return
        }

        // Test if the event should bypass custom anvil.
        if (DependencyManager.tryClickAnvilResultBypass(event, inventory)) return

        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

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
            // BRUH
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
        val xpCost = recipe.determineCost(amount, leftItem, output)
        val finalCost =
            if (recipe.removeExactLinearXp) xpCost
            else AnvilXpUtil.calculateLevelForXp(xpCost)

        CustomAnvil.log("gamemode: ${player.gameMode != GameMode.CREATIVE}, cost: $finalCost, level: ${player.level}, result: ${player.totalExperience < finalCost} ${player.level < finalCost}")
        if (player.gameMode != GameMode.CREATIVE) {
            if (recipe.removeExactLinearXp) {
                val levelXp = AnvilXpUtil.calculateXpForLevel(player.level)
                val delta = AnvilXpUtil.calculateXpForLevel(player.level + 1) - levelXp
                val totalXp = levelXp + player.exp * delta
                if (totalXp < finalCost) return
            } else if (player.level < finalCost) return
        }

        // We give the item manually
        // But first we check if we should give the item
        val slotDestination = getActionSlot(event, player)
        if (slotDestination.type == SlotType.NO_SLOT) return

        // Handle not creative middle click...
        if (event.click != ClickType.MIDDLE &&
            !handleCustomCraftClick(
                event,
                recipe,
                inventory,
                player,
                leftItem,
                rightItem,
                amount,
                finalCost,
                recipe.removeExactLinearXp
            )
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
        amount: Int, xpCost: Int, linearCost: Boolean = false
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
            if (linearCost) {
                val levelXp = AnvilXpUtil.calculateXpForLevel(player.level)
                val delta = AnvilXpUtil.calculateXpForLevel(player.level + 1) - levelXp
                var totalXp = levelXp + player.exp * delta
                totalXp -= xpCost

                val newLevel = AnvilXpUtil.calculateLevelForXp(totalXp.toInt())

                val newLevelXp = AnvilXpUtil.calculateXpForLevel(newLevel)
                val newDelta = AnvilXpUtil.calculateXpForLevel(newLevel + 1) - newLevelXp
                val xp = (totalXp - newLevelXp) / newDelta

                player.level = newLevel
                player.exp = xp / newDelta
            } else {
                player.level -= xpCost
            }
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
        cost: AnvilCost,
    ): Boolean {
        // To avoid vanilla, we cancel the event
        event.result = Event.Result.DENY
        event.isCancelled = true

        if (!cost.valid) return false

        // Where should we get the item
        val slotDestination = getActionSlot(event, player)
        if (slotDestination.type == SlotType.NO_SLOT) return false

        // If not creative middle click...
        if (event.click != ClickType.MIDDLE) {
            if(cost.isMonetary) {
                val result = EconomyManager.economy!!.remove(player, cost.asMonetaryCost())
                if(!result) return false
            } else {
                player.level -= cost.asXpCost()
            }

            // We remove what should be removed
            if (leftItem != null) leftItem.amount -= leftRemoveCount
            inventory.setItem(ANVIL_INPUT_LEFT, leftItem)

            if (rightItem != null) rightItem.amount -= rightRemoveCount
            inventory.setItem(ANVIL_INPUT_RIGHT, rightItem)

            inventory.setItem(ANVIL_OUTPUT_SLOT, null)

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
    ): AnvilCost {
        if (player.gameMode == GameMode.CREATIVE) return AnvilCost(0)

        val cost = AnvilCost()
        // Get repairCost
        leftItem.itemMeta?.let { leftMeta ->
            val leftName = leftMeta.displayName
            output.itemMeta?.let {
                // Rename cost
                if (!leftName.contentEquals(it.displayName)) {
                    cost.rename += ConfigOptions.itemRenameCost

                    // Color cost
                    if (it.displayName.contains('§')) {
                        cost.rename += ConfigOptions.useOfColorCost
                    }
                }
            }
        }

        cost.workPenalty = AnvilXpUtil.calculatePenalty(leftItem, null, resultCopy, AnvilUseType.UNIT_REPAIR)
        cost.repair = resultAmount * ConfigOptions.unitRepairCost

        var sum = cost.repair

        if (
            !ConfigOptions.doRemoveCostLimit &&
            ConfigOptions.doCapCost
        ) {
            val final = min(sum, ConfigOptions.maxAnvilCost)
            cost.generic += (final - sum)

            sum = final
        }

        if (ConfigOptions.shouldUseMoney(player)) {
            cost.isMonetary = true
            if (!EconomyManager.economy!!.has(player, cost.asMonetaryCost()))
                cost.valid = false
        } else {
            if ((inventory.maximumRepairCost <= sum)
                || (player.level < sum)
            ) cost.valid = false
        }

        return cost
    }

    private fun getFromLoreEditXpCost(
        cost: AnvilCost,
        player: Player,
        inventory: AnvilInventory,
    ): AnvilCost {
        if (GameMode.CREATIVE == player.gameMode) return AnvilCost(0)

        if (ConfigOptions.shouldUseMoney(player)) {
            cost.isMonetary = true
            if (!EconomyManager.economy!!.has(player, cost.asMonetaryCost()))
                cost.valid = false
        } else {
            val repairCost = cost.asXpCost()

            if ((inventory.maximumRepairCost <= repairCost)
                || (player.level < repairCost)
            )
                cost.valid = false
        }

        return cost
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

        val cost = AnvilCost()
        if (editType) {
            if (output != AnvilLoreEditUtil.handleLoreAppendByBook(player, leftItem, bookMeta, cost)) return false

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
                output, getFromLoreEditXpCost(cost, player, inventory)
            )
        } else {
            if (output != AnvilLoreEditUtil.handleLoreRemoveByBook(player, leftItem, cost)) return false

            // fill book meta
            val lore = DependencyManager.stripLore(leftItem)
            if (lore.isEmpty()) return false

            val rightCopy: ItemStack?
            if (LoreEditType.REMOVE_BOOK.doConsume) {
                rightCopy = null
            } else {
                // Uncolor the page
                AnvilLoreEditUtil.uncolorLines(player, lore, LoreEditType.REMOVE_BOOK)

                val bookPage = StringBuilder()
                lore.forEach {
                    if (bookPage.isNotEmpty()) bookPage.append('\n')
                    if (it == null) return@forEach

                    bookPage.append(MiniMessageUtil.plain_text_mm.serialize(it))
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
                output, getFromLoreEditXpCost(cost, player, inventory)
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

        val editTypeIsAppend = AnvilLoreEditUtil.paperLoreEditIsAppend(leftItem, rightItem) ?: return false

        val cost = AnvilCost()
        if (editTypeIsAppend) {
            if (output != AnvilLoreEditUtil.handleLoreAppendByPaper(player, leftItem, rightItem, cost)) return false

            val paperCopy: ItemStack?
            if (LoreEditType.APPEND_PAPER.doConsume) {
                paperCopy = null
            } else {
                // Remove custom name to paper
                paperCopy = rightItem.clone()
                paperCopy.amount = 1
                paperMeta.setComponentDisplayName(null)
                paperCopy.itemMeta = paperMeta
            }

            return if (rightItem.amount > 1) {
                extractAnvilResult(
                    event, player, inventory,
                    paperCopy, 0,
                    rightItem, 1,
                    output, getFromLoreEditXpCost(cost, player, inventory)
                )
            } else {
                extractAnvilResult(
                    event, player, inventory,
                    null, 0,
                    paperCopy, 0,
                    output, getFromLoreEditXpCost(cost, player, inventory)
                )
            }
        } else {
            if (output != AnvilLoreEditUtil.handleLoreRemoveByPaper(player, leftItem, cost)) return false

            val leftMeta = leftItem.itemMeta
            if (leftMeta == null || !leftMeta.hasLore()) return false
            val lore = DependencyManager.stripLore(leftItem)
            if (lore.isEmpty()) return false

            // Create result item
            val rightClone: ItemStack?
            if (LoreEditType.REMOVE_PAPER.doConsume) {
                rightClone = null
            } else {
                val removeEnd = LoreEditConfigUtil.paperLoreOrderIsEnd
                val line = if (removeEnd) lore[lore.size - 1]
                else lore[0]

                // uncolor the line
                val ref = AtomicReference(line)
                AnvilLoreEditUtil.uncolorLine(player, ref, LoreEditType.REMOVE_PAPER)

                rightClone = rightItem.clone()
                rightClone.amount = 1

                val resultMeta = rightClone.itemMeta ?: return false
                resultMeta.setComponentDisplayName(ref.get())
                rightClone.itemMeta = resultMeta
            }

            return if (rightItem.amount > 1) {
                extractAnvilResult(
                    event, player, inventory,
                    rightClone, 0,
                    rightItem, 1,
                    output, getFromLoreEditXpCost(cost, player, inventory)
                )
            } else {
                extractAnvilResult(
                    event, player, inventory,
                    null, 0,
                    rightClone, 0,
                    output, getFromLoreEditXpCost(cost, player, inventory)
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
