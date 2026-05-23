package xyz.alexcrea.cuanvil.util

import net.kyori.adventure.text.Component
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.permissions.Permissible
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.componentLore
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.setComponentLore
import xyz.alexcrea.cuanvil.util.AnvilXpUtil.AnvilCost
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil
import xyz.alexcrea.cuanvil.util.config.LoreEditType
import java.util.*
import java.util.concurrent.atomic.AtomicReference

object AnvilLoreEditUtil {

    private const val LORE_BY_BOOK: String = "ca.lore_edit.book"
    private const val LORE_BY_PAPER: String = "ca.lore_edit.paper"

    private fun hasLoreEditByBookPermission(player: Permissible): Boolean {
        return !LoreEditConfigUtil.bookLoreEditNeedPermission || player.hasPermission(LORE_BY_BOOK)
    }

    private fun hasLoreEditByPaperPermission(player: Permissible): Boolean {
        return !LoreEditConfigUtil.paperLoreEditNeedPermission || player.hasPermission(LORE_BY_PAPER)
    }

    fun handleLoreAppendByBook(
        player: Permissible,
        first: ItemStack,
        book: BookMeta,
        cost: AnvilCost
    ): ItemStack? {
        if (!hasLoreEditByBookPermission(player)) return null

        val result = first.clone()
        val meta = result.itemMeta ?: return null
        val lore = meta.componentLore()

        val page = book.pages[0]
        val lines = ArrayList<String>(page.split("\n"))
        val outLines = ArrayList<Component>(lines.size)
        val colorCost = colorLines(player, LoreEditType.APPEND_BOOK,
            lines, outLines)

        lore.addAll(outLines)

        meta.setComponentLore(lore)
        result.itemMeta = meta

        if (result == first) return null

        // Handle xp
        cost.lore = colorCost // Cost of using color
        cost.lore += outLines.size * LoreEditType.APPEND_BOOK.perLineCost // per line cost
        baseEditLoreXpCost(cost, first, result, LoreEditType.APPEND_BOOK) // Fixed cost and work penalty

        return result
    }

    fun handleLoreRemoveByBook(player: Permissible, first: ItemStack, cost: AnvilCost): ItemStack? {
        if (!hasLoreEditByBookPermission(player)) return null

        // remove lore
        val result = first.clone()
        val leftMeta = result.itemMeta ?: return null
        val currentLore = DependencyManager.stripLore(result)
        if (currentLore.isEmpty()) return null

        val uncolorCost = uncolorLines(player, currentLore, LoreEditType.REMOVE_BOOK)

        leftMeta.lore = null
        result.itemMeta = leftMeta

        DependencyManager.updateLore(result)
        if (result == first) return null

        // Handle xp
        cost.lore = uncolorCost
        cost.lore+= currentLore.size * LoreEditType.REMOVE_BOOK.perLineCost
        baseEditLoreXpCost(cost, first, result, LoreEditType.REMOVE_BOOK)

        return result
    }

    // Return true if appended, false if removed, null if neither
    fun bookLoreEditIsAppend(first: ItemStack, second: ItemStack): Boolean? {
        // Test if the book & quil contain content
        val meta = second.itemMeta as BookMeta? ?: return false

        var hasContent = false
        if (meta.hasPages() && meta.pageCount >= 1) {
            // Test if the pages is ok
            for (page in meta.pages) {
                if (page.isNotBlank()) {
                    hasContent = true
                    break
                }
            }
        }

        // We don't want to "add" the first page is there is content and the first page is empty
        if (hasContent) {
            if (meta.pages[0].isEmpty()) return null
            if (LoreEditType.APPEND_BOOK.enabled)
                return true
        } else if (LoreEditType.REMOVE_BOOK.enabled) {
            if (!first.hasItemMeta()) return null

            val leftMeta = first.itemMeta!!
            return if (leftMeta.hasLore()) false
            else null
        }
        return null
    }

    fun tryLoreEditByBook(player: HumanEntity, first: ItemStack, second: ItemStack, cost: AnvilCost): ItemStack? {
        val isAppend = bookLoreEditIsAppend(first, second) ?: return null

        val meta = second.itemMeta as BookMeta
        return if (isAppend) handleLoreAppendByBook(player, first, meta, cost)
        else handleLoreRemoveByBook(player, first, cost)
    }

    // Return true if appended, false if removed, null if neither
    fun paperLoreEditIsAppend(first: ItemStack, second: ItemStack): Boolean? {
        // Test if the paper contain a display name
        val meta = second.itemMeta ?: return false

        val hasContent = meta.hasDisplayName()
        if (hasContent) {
            if (LoreEditType.APPEND_PAPER.enabled)
                return true
        } else if (LoreEditType.REMOVE_PAPER.enabled) {
            if (!first.hasItemMeta()) return null

            val leftMeta = first.itemMeta!!
            return if (leftMeta.hasLore() && leftMeta.lore!!.isNotEmpty()) false
            else null
        }
        return null
    }

    fun handleLoreAppendByPaper(
        player: Permissible,
        first: ItemStack,
        second: ItemStack,
        cost: AnvilCost
    ): ItemStack? {
        if (!hasLoreEditByPaperPermission(player)) return null

        val result = first.clone()
        val meta = result.itemMeta ?: return null
        val lore = meta.componentLore()

        val appendEnd = LoreEditConfigUtil.paperLoreOrderIsEnd

        // A bit overdone to color 1 line but hey
        val outList = ArrayList<Component>(1)
        val colorCost = colorLines(player, LoreEditType.APPEND_PAPER,
            Collections.singletonList(second.itemMeta!!.displayName),
            outList)

        val line = outList[0]
        if (appendEnd)
            lore.add(line)
        else
            lore.add(0, line)

        meta.setComponentLore(lore)
        result.itemMeta = meta

        if (result == first) return null

        // Handle xp
        cost.lore = colorCost
        baseEditLoreXpCost(cost, first, result, LoreEditType.APPEND_PAPER)

        return result
    }

    fun handleLoreRemoveByPaper(player: Permissible, first: ItemStack, cost: AnvilCost): ItemStack? {
        if (!hasLoreEditByPaperPermission(player)) return null

        // remove lore line
        val result = first.clone()
        val meta = result.itemMeta!!

        val removeEnd = LoreEditConfigUtil.paperLoreOrderIsEnd
        val lore = DependencyManager.stripLore(result)
        if (lore.isEmpty()) return null

        val line = if (removeEnd) lore.removeAt(lore.size - 1)
        else lore.removeAt(0)

        meta.lore = null
        result.itemMeta = meta

        // Update lore but make sure custom lore is put last
        DependencyManager.updateLore(result)

        val finalLore = ArrayList<Component?>()
        finalLore.addAll(meta.componentLore())
        finalLore.addAll(lore)

        meta.setComponentLore(finalLore)
        result.itemMeta = meta
        if (result == first) return null

        // Get color cost to uncolor this line
        val uncolorCost = uncolorLine(player, line, LoreEditType.REMOVE_PAPER)

        // Handle other xp
        cost.lore = uncolorCost
        baseEditLoreXpCost(cost, first, result, LoreEditType.REMOVE_PAPER)

        return result
    }

    fun tryLoreEditByPaper(
        player: HumanEntity,
        first: ItemStack,
        second: ItemStack,
        cost: AnvilCost
    ): ItemStack? {
        val isAppend = paperLoreEditIsAppend(first, second) ?: return null

        return if (isAppend) handleLoreAppendByPaper(player, first, second, cost)
        else handleLoreRemoveByPaper(player, first, cost)
    }

    private fun baseEditLoreXpCost(
        cost: AnvilCost,
        first: ItemStack,
        result: ItemStack,
        editType: LoreEditType
    ) {
        cost.lore+= editType.fixedCost

        cost.penalty = AnvilXpUtil.calculatePenalty(first, null, result, editType.useType)
    }

    fun colorPermission(player: Permissible, editType: LoreEditType): AnvilColorUtil.ColorPermissions {
        return AnvilColorUtil.calculatePermissions(player,
            false,
            editType.allowColorCode,
            editType.allowHexColor,
            editType.allowMinimessage,
            AnvilColorUtil.ColorUseType.LORE_EDIT)
    }

    private fun colorLine(line: String, permission: AnvilColorUtil.ColorPermissions): Component? {
        return AnvilColorUtil.handleColor(
            line,
            permission
        )
    }

    private fun colorLines(player: Permissible, editType: LoreEditType,
                           lines: List<String>, outLines: MutableList<Component>): Int {
        val permission = colorPermission(player, editType)
        val colorCost = editType.useColorCost

        // Handle color and minimessage of each lines
        var hasUsedColor = false
        for (line in lines) {
            val component = colorLine(line, permission)

            if (component != null) {
                hasUsedColor = true
                outLines.add(component)
            } else {
                outLines.add(Component.text(line))
            }
        }

        return if (hasUsedColor) colorCost
        else 0
    }

    fun uncolorLines(player: Permissible, lines: MutableList<Component?>, editType: LoreEditType): Int {
        val permission = colorPermission(player, editType)

        // Now handle color of each lines
        var hasUndidColor = false
        for ((index, line) in lines.withIndex()) {
            if(line == null){
                lines[index] = null
                continue
            }

            val clearedLine = AnvilColorUtil.revertColorSmallest(
                line,
                permission
            )

            val result: String
            if (clearedLine != null) {
                hasUndidColor = true
                result = clearedLine
            } else {
                result =  MiniMessageUtil.plain_text_mm.serialize(line)
            }

            lines[index] = MiniMessageUtil.plain_text_mm.deserialize(result)
        }

        return if (hasUndidColor) {
            editType.removeColorCost
        } else {
            0
        }
    }

    // do not output the uncolored line...
    fun uncolorLine(player: Permissible, line: Component?, editType: LoreEditType): Int {
        return uncolorLine(player, AtomicReference(line), editType)
    }

    fun uncolorLine(player: Permissible, line: AtomicReference<Component?>, editType: LoreEditType): Int {
        val coloredComponent = line.get() ?: return 0
        val permission = colorPermission(player, editType)

        val clearedLine = AnvilColorUtil.revertColorSmallest(
            coloredComponent,
            permission
        )

        var hasUndidColor = false
        val result: String
        if(clearedLine != null){
            hasUndidColor = true
            result = clearedLine
        } else {
            // Remove extra tags
            result = MiniMessageUtil.plain_text_mm.serialize(coloredComponent)
        }
        line.set(MiniMessageUtil.plain_text_mm.deserialize(result))

        return if (hasUndidColor) {
            editType.removeColorCost
        } else {
            0
        }
    }

}