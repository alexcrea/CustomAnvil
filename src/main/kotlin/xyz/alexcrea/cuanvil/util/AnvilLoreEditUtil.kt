package xyz.alexcrea.cuanvil.util

import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.permissions.Permissible
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil
import xyz.alexcrea.cuanvil.util.config.LoreEditType

object AnvilLoreEditUtil {

    private const val LORE_BY_BOOK: String = "ca.lore_edit.book"
    private const val LORE_BY_PAPER: String = "ca.lore_edit.paper"

    private fun hasLoreEditByBookPermission(player: Permissible): Boolean {
        return LoreEditConfigUtil.bookLoreEditNeedPermission && player.hasPermission(LORE_BY_BOOK)
    }

    private fun hasLoreEditByPaperPermission(player: Permissible): Boolean {
        return LoreEditConfigUtil.paperLoreEditNeedPermission && player.hasPermission(LORE_BY_PAPER)
    }

    fun handleLoreAppendByBook(player: Permissible, first: ItemStack, book: BookMeta): ItemStack? {
        if (!hasLoreEditByBookPermission(player)) return null

        val result = first.clone()
        val meta = result.itemMeta
        val lore = if (meta?.hasLore() == true) {
            ArrayList<String>(meta.lore!!)
        } else ArrayList()

        //TODO check color if color if enabled
        lore.addAll(book.pages[0].split("\n"))

        meta?.lore = lore
        result.itemMeta = meta

        return result
    }

    fun handleLoreRemoveByBook(player: Permissible, first: ItemStack): ItemStack? {
        if (!hasLoreEditByBookPermission(player)) return null

        // remove lore
        val result = first.clone()
        val leftMeta = result.itemMeta!!
        leftMeta.lore = null
        result.itemMeta = leftMeta

        return result
    }

    // Return true if append, false if remove, null if neither
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

    fun tryLoreEditByBook(player: HumanEntity, first: ItemStack, second: ItemStack): ItemStack? {
        val bookType = bookLoreEditIsAppend(first, second) ?: return null

        val meta = second.itemMeta as BookMeta
        return if (bookType) handleLoreAppendByBook(player, first, meta)
        else handleLoreRemoveByBook(player, first)
    }

    // Return true if append, false if remove, null if neither
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

    fun handleLoreAppendByPaper(player: Permissible, first: ItemStack, second: ItemStack): ItemStack? {
        if (!hasLoreEditByPaperPermission(player)) return null

        val result = first.clone()
        val meta = result.itemMeta
        val lore = if (meta?.hasLore() == true) {
            ArrayList<String>(meta.lore!!)
        } else ArrayList()

        val appendEnd = LoreEditConfigUtil.paperLoreOrderIsEnd

        //TODO check color if color if enabled
        val line = second.itemMeta!!.displayName
        if(appendEnd)
            lore.add(line)
        else
            lore.add(0, line)

        meta?.lore = lore
        result.itemMeta = meta

        return result
    }

    fun handleLoreRemoveByPaper(player: Permissible, first: ItemStack): ItemStack? {
        if (!hasLoreEditByPaperPermission(player)) return null

        // remove lore line
        val result = first.clone()
        val meta = result.itemMeta!!

        val removeEnd = LoreEditConfigUtil.paperLoreOrderIsEnd
        val lore: ArrayList<String> = ArrayList(meta.lore!!)

        if(removeEnd) lore.removeAt(lore.size - 1)
        else lore.removeAt(0)

        meta.lore = if(lore.isEmpty()) null else lore
        result.itemMeta = meta

        return result
    }

    fun tryLoreEditByPaper(player: HumanEntity, first: ItemStack, second: ItemStack): ItemStack? {
        val bookType = paperLoreEditIsAppend(first, second) ?: return null

        return if (bookType) handleLoreAppendByPaper(player, first, second)
        else handleLoreRemoveByPaper(player, first)
    }

}