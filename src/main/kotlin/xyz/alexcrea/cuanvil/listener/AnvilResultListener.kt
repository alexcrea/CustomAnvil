package xyz.alexcrea.cuanvil.listener

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.ItemUtil.canMergeWith
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
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
import xyz.alexcrea.cuanvil.anvil.AnvilCost
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.AnvilResult
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.CustomCraftResult
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.LoreEditResult
import xyz.alexcrea.cuanvil.anvil.AnvilMergeLogic.UnitRepairResult
import xyz.alexcrea.cuanvil.config.AnvilFinishOptions
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.economy.EconomyManager
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.setComponentDisplayName
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_LEFT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_INPUT_RIGHT
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener.Companion.ANVIL_OUTPUT_SLOT
import xyz.alexcrea.cuanvil.util.CustomRecipeUtil
import xyz.alexcrea.cuanvil.util.MiniMessageUtil
import xyz.alexcrea.cuanvil.util.anvil.AnvilLoreEditUtil
import xyz.alexcrea.cuanvil.util.anvil.AnvilXpUtil
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
        val view = event.view

        if (event.rawSlot != ANVIL_OUTPUT_SLOT) {
            return
        }

        // Test if the event should bypass custom anvil.
        if (DependencyManager.tryClickAnvilResultBypass(event, inventory)) return

        if (!player.hasPermission(CustomAnvil.affectedByPluginPermission)) return

        val output = inventory.getItem(ANVIL_OUTPUT_SLOT) ?: return
        val leftItem = inventory.getItem(ANVIL_INPUT_LEFT) ?: return
        val rightItem = inventory.getItem(ANVIL_INPUT_RIGHT)

        // Deny by default. allow if working
        event.result = Event.Result.DENY
        if (GameMode.CREATIVE != player.gameMode && inventory.repairCost >= inventory.maximumRepairCost) {
            return
        }

        // Test custom recipe
        val customRecipeResult = AnvilMergeLogic.testCustomRecipe(view, inventory, player, leftItem, rightItem)
        if (!customRecipeResult.isEmpty()) {
            onCustomCraft(
                event, player, inventory,
                leftItem, rightItem, customRecipeResult
            )
            return
        }

        // Do not continue if there was no change
        if ((output == inventory.getItem(ANVIL_INPUT_LEFT))) {
            return
        }

        // Rename
        if (rightItem == null) {
            val result = AnvilMergeLogic.doRenaming(view, inventory, player, leftItem)
            if (result.isEmpty()) return

            extractAnvilResult(
                event, player, inventory,
                null, 0,
                null, 0,
                result
            )
            return
        }

        // Merge
        val canMerge = leftItem.canMergeWith(rightItem)
        if (canMerge) {
            val result = AnvilMergeLogic.doMerge(view, inventory, player, leftItem, rightItem)

            val worked = extractAnvilResult(
                event, player, inventory,
                null, 0,
                null, 0,
                result
            )
            if(!worked) {
                CustomAnvil.verboseLog("Merge extract failed. reset the displayed price")
                // Reset the price
                AnvilXpUtil.setAnvilResult(inventory, view, player, result)
            }
            return
        }

        // Unit repair
        val unitRepairResult = AnvilMergeLogic.testUnitRepair(
            view, inventory, player,
            leftItem, rightItem
        )
        if (!unitRepairResult.isEmpty()) {
            onUnitRepairExtract(
                rightItem, event, player, inventory,
                unitRepairResult
            )
            return
        }

        // For lore edit
        val loreResult = AnvilMergeLogic.testLoreEdit(player, leftItem, rightItem)
        if (!loreResult.isEmpty()) {
            if (loreResult.type.isBook)
                handleBookLoreEdit(event, inventory, player, leftItem, rightItem, loreResult)
            else
                handlePaperLoreEdit(event, inventory, player, leftItem, rightItem, loreResult)
            return
        }
    }

    private fun onCustomCraft(
        event: InventoryClickEvent,
        player: Player,
        inventory: AnvilInventory,
        leftItem: ItemStack,
        rightItem: ItemStack?,
        result: CustomCraftResult,
    ) {
        val recipe = result.recipe!!
        val rawCost = result.customCraftCost.rawCost
        val finalCost =
            if (recipe.removeExactLinearXp) rawCost
            else AnvilXpUtil.calculateLevelForXp(rawCost)

        CustomAnvil.log(
            "gamemode: ${player.gameMode != GameMode.CREATIVE}, " +
                    "cost: $finalCost, level: ${player.level}, " +
                    "result: ${player.totalExperience < finalCost} ${player.level < finalCost}"
        )

        if (player.gameMode != GameMode.CREATIVE) {
            if (ConfigOptions.shouldUseMoney(player)) {
                result.cost.isMonetary = true
                if (!EconomyManager.economy!!.has(player, result.cost.asMonetaryCost())) return
            } else if (recipe.removeExactLinearXp) {
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
                inventory,
                player,
                leftItem,
                rightItem,
                result
            )
        ) return

        // Finally, we add the item to the player
        if (slotDestination.type == SlotType.CURSOR) {
            player.setItemOnCursor(result.item)
        } else {// We assume SlotType == SlotType.INVENTORY
            player.inventory.setItem(slotDestination.slot, result.item)
        }
    }

    private fun handleCustomCraftClick(
        event: InventoryClickEvent,
        inventory: AnvilInventory, player: Player,
        leftItem: ItemStack, rightItem: ItemStack?,
        result: CustomCraftResult
    ): Boolean {
        val amount = result.amount
        val recipe = result.recipe!!

        // We remove what should be removed
        if (rightItem != null) {
            if (recipe.rightItem == null) return false// in case it changed

            rightItem.amount -= amount * recipe.rightItem!!.amount
            inventory.setItem(ANVIL_INPUT_RIGHT, rightItem)
        }

        leftItem.amount -= amount * recipe.leftItem!!.amount
        inventory.setItem(ANVIL_INPUT_LEFT, leftItem)

        removeCustomCraftCost(player, result)

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

    private fun removeCustomCraftCost(player: Player, result: CustomCraftResult) {
        if (player.gameMode == GameMode.CREATIVE) return

        val rawCost = result.customCraftCost.rawCost
        if (result.cost.isMonetary) {
            EconomyManager.economy!!.remove(player, result.cost.asMonetaryCost())
            return
        }

        if (result.recipe!!.removeExactLinearXp) {
            val levelXp = AnvilXpUtil.calculateXpForLevel(player.level)
            val delta = AnvilXpUtil.calculateXpForLevel(player.level + 1) - levelXp
            var totalXp = levelXp + player.exp * delta
            totalXp -= rawCost

            val newLevel = AnvilXpUtil.calculateLevelForXp(totalXp.toInt())

            val newLevelXp = AnvilXpUtil.calculateXpForLevel(newLevel)
            val newDelta = AnvilXpUtil.calculateXpForLevel(newLevel + 1) - newLevelXp
            val xp = (totalXp - newLevelXp) / newDelta

            player.level = newLevel
            player.exp = xp / newDelta
        } else {
            player.level -= AnvilXpUtil.calculateLevelForXp(rawCost)
        }

    }

    private fun tryRemoveCost(player: Player, result: AnvilResult): Boolean {
        if (player.gameMode == GameMode.CREATIVE) return true

        val cost = result.cost
        if (cost.isMonetary) {
            val result = EconomyManager.economy!!.remove(player, cost.asMonetaryCost())
            if (!result) {
                CustomAnvil.verboseLog("Could not remove monetary cost ${cost.asMonetaryCost()}")
                return false
            }
        } else {
            val xpCost = cost.filteredXpCost()
            if (xpCost > AnvilXpUtil.maximumXpCost(result.ignoreXpRules)) {
                CustomAnvil.verboseLog("Cost above maximum $xpCost > ${AnvilXpUtil.maximumXpCost(result.ignoreXpRules)}")
                return false
            }
            if (player.level < xpCost) {
                CustomAnvil.verboseLog("Player do not have enough xp ${player.level} < $xpCost")
                return false
            }

            player.level -= xpCost
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
        result: AnvilResult
    ): Boolean {
        if (result.isEmpty()) {
            CustomAnvil.verboseLog("Merge result is empty")
            return false
        }

        // To avoid vanilla, we cancel the event
        event.result = Event.Result.DENY
        event.isCancelled = true
        val cost = result.cost

        processCost(inventory, player, cost)
        if (!cost.valid && player.gameMode != GameMode.CREATIVE) {
            CustomAnvil.verboseLog("Player cannot afford the cost")
            return false
        }

        // Where should we get the item
        val slotDestination = getActionSlot(event, player)
        if (slotDestination.type == SlotType.NO_SLOT) return false

        // If not creative middle click...
        if (event.click != ClickType.MIDDLE) {
            if (!tryRemoveCost(player, result)) return false

            // We remove what should be removed
            if (leftItem != null) leftItem.amount -= leftRemoveCount
            inventory.setItem(ANVIL_INPUT_LEFT, leftItem)

            if (rightItem != null) rightItem.amount -= rightRemoveCount
            inventory.setItem(ANVIL_INPUT_RIGHT, rightItem)

            inventory.setItem(ANVIL_OUTPUT_SLOT, null)

        }

        // Finally, we add the item to the player
        if (SlotType.CURSOR == slotDestination.type) {
            player.setItemOnCursor(result.item)
        } else {// We assume SlotType == SlotType.INVENTORY
            player.inventory.setItem(slotDestination.slot, result.item)
        }

        if (event.click != ClickType.MIDDLE)
            handleAnvilMechanic(player, view, player.gameMode != GameMode.CREATIVE)

        return true
    }

    private fun processCost(inventory: AnvilInventory, player: Player, cost: AnvilCost) {
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
    }

    // Process both sound & degradation
    private fun handleAnvilMechanic(player: HumanEntity, view: AnvilView, canDegrade: Boolean) {
        // ok so we do not provide a getLocation on view ?
        val inv = view.topInventory as? AnvilInventory
        val location = inv?.location

        val wasDestroyed = canDegrade && tryDegradeAnvil(view, location)
        tryPlaySound(location, player, wasDestroyed)
    }

    private fun tryDegradeAnvil(view: AnvilView, location: Location?): Boolean {
        val world = location?.world ?: return false
        if (Math.random() > AnvilFinishOptions.degradation_chance) return false

        val block = world.getBlockAt(location)

        val next = when (block.type) {
            Material.ANVIL -> Material.CHIPPED_ANVIL
            Material.CHIPPED_ANVIL -> Material.DAMAGED_ANVIL
            Material.DAMAGED_ANVIL -> Material.AIR
            else -> return false
        }

        block.type = next

        if (next == Material.AIR) {
            view.close()
            return true
        }

        return false
    }

    private fun tryPlaySound(
        location: Location?,
        player: HumanEntity,
        wasDestroyed: Boolean,
    ) {
        val world = location?.world

        if (!AnvilFinishOptions.sound_enabled) return

        val sound = AnvilFinishOptions.getAnvilSound(wasDestroyed)
        if(world == null) {
            player.world.playSound(
                player,
                sound.sound,
                sound.category,
                sound.volume,
                sound.pitch,
            )
        } else {
            world.playSound(
                location,
                sound.sound,
                sound.category,
                sound.volume,
                sound.pitch,
            )
        }
    }

    private fun onUnitRepairExtract(
        rightItem: ItemStack,
        event: InventoryClickEvent,
        player: Player,
        inventory: AnvilInventory,
        result: UnitRepairResult,
    ) {
        // We give the item manually
        extractAnvilResult(
            event, player, inventory,
            null, 0,
            rightItem, result.repairAmount,
            result
        )
    }

    private fun handleBookLoreEdit(
        event: InventoryClickEvent,
        inventory: AnvilInventory,
        player: Player,
        leftItem: ItemStack,
        rightItem: ItemStack,
        result: LoreEditResult
    ) {
        if (result.type.isAppend)
            handleBookLoreAppend(event, inventory, player, rightItem, result)
        else
            handleBookLoreRemove(event, inventory, player, leftItem, rightItem, result)
    }

    private fun handleBookLoreAppend(
        event: InventoryClickEvent,
        inventory: AnvilInventory,
        player: Player,
        rightItem: ItemStack,
        result: LoreEditResult
    ) {
        val bookMeta = rightItem.itemMeta as BookMeta? ?: return

        // Remove pages to book
        val clearedBook: ItemStack?
        if (LoreEditType.APPEND_BOOK.doConsume) {
            clearedBook = null
        } else {
            clearedBook = rightItem.clone()
            bookMeta.pages = Collections.emptyList()
            clearedBook.itemMeta = bookMeta
        }

        extractAnvilResult(
            event, player, inventory,
            null, 0,
            clearedBook, 0,
            result
        )
    }

    private fun handleBookLoreRemove(
        event: InventoryClickEvent,
        inventory: AnvilInventory,
        player: Player,
        leftItem: ItemStack,
        rightItem: ItemStack,
        result: LoreEditResult
    ) {
        val bookMeta = rightItem.itemMeta as BookMeta? ?: return

        // fill book meta
        val lore = DependencyManager.stripLore(leftItem)
        if (lore.isEmpty()) return

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

        extractAnvilResult(
            event, player, inventory,
            null, 0,
            rightCopy, 0,
            result
        )
    }

    private fun handlePaperLoreEdit(
        event: InventoryClickEvent,
        inventory: AnvilInventory,
        player: Player,
        leftItem: ItemStack,
        rightItem: ItemStack,
        result: LoreEditResult
    ) {
        if (result.type.isAppend)
            handlePaperLoreAppend(event, inventory, player, rightItem, result)
        else
            handlePaperLoreRemove(event, inventory, player, leftItem, rightItem, result)
    }

    private fun handlePaperLoreAppend(
        event: InventoryClickEvent,
        inventory: AnvilInventory,
        player: Player,
        rightItem: ItemStack,
        result: LoreEditResult
    ) {
        val paperMeta = rightItem.itemMeta ?: return


        val paperCopy: ItemStack?
        if (LoreEditType.APPEND_PAPER.doConsume) {
            paperCopy = null
        } else {
            // Remove custom name to paper
            paperCopy = rightItem.clone()
            paperCopy.amount = 1
            paperMeta.setComponentDisplayName(null)

            // Remove pcd name
            AnvilMergeLogic.processPCD(paperMeta, player, null)

            paperCopy.itemMeta = paperMeta
        }

        if (rightItem.amount > 1) {
            extractAnvilResult(
                event, player, inventory,
                paperCopy, 0,
                rightItem, 1,
                result
            )
        } else {
            extractAnvilResult(
                event, player, inventory,
                null, 0,
                paperCopy, 0,
                result
            )
        }
    }

    private fun handlePaperLoreRemove(
        event: InventoryClickEvent,
        inventory: AnvilInventory,
        player: Player,
        leftItem: ItemStack,
        rightItem: ItemStack,
        result: LoreEditResult
    ) {
        val leftMeta = leftItem.itemMeta
        if (leftMeta == null || !leftMeta.hasLore()) return

        val lore = DependencyManager.stripLore(leftItem)
        if (lore.isEmpty()) return

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

            val resultMeta = rightClone.itemMeta ?: return
            resultMeta.setComponentDisplayName(ref.get())
            rightClone.itemMeta = resultMeta
        }

        if (rightItem.amount > 1) {
            extractAnvilResult(
                event, player, inventory,
                rightClone, 0,
                rightItem, 1,
                result
            )
        } else {
            extractAnvilResult(
                event, player, inventory,
                null, 0,
                rightClone, 0,
                result
            )
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
