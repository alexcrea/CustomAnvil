package xyz.alexcrea.cuanvil.util

import io.delilaheve.util.ConfigOptions
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.permissions.Permissible

object AnvilLoreEditUtil {

    private const val LORE_BY_BOOK: String = "ca.lore_edit.book"
    private const val LORE_BY_PAPER: String = "ca.lore_edit.paper"

    private fun hasLoreEditByBookPermission(player: Permissible): Boolean {
        return ConfigOptions.BookLoreEditNeedPermission && player.hasPermission(LORE_BY_BOOK)
    }

    private fun hasLoreEditByPaperPermission(player: Permissible): Boolean {
        return ConfigOptions.PaperLoreEditNeedPermission && player.hasPermission(LORE_BY_PAPER)
    }

    fun handleLoreAppendByBook(player: Permissible, first: ItemStack, book: BookMeta): ItemStack? {
        if (!hasLoreEditByBookPermission(player)) return null

        val result = first.clone()
        val meta = result.itemMeta
        //TODO take into account previous lore
        meta?.lore = book.pages[0].split("\n") //TODO check color if color is enabled
        result.itemMeta = meta

        return result
    }

    fun handleLoreRemoveByBook(player: Permissible, first: ItemStack, second: ItemStack, book: BookMeta): ItemStack? {
        if (!hasLoreEditByBookPermission(player)) return null

        val meta = first.itemMeta
        if (meta == null || !meta.hasLore()) return null

        val bookPage = StringBuilder()
        meta.lore!!.forEach {
            if (bookPage.isNotEmpty()) bookPage.append('\n')
            bookPage.append(it)
        }

        val resultPage = bookPage.toString()
        //TODO maybe check page size ? bc it may be too big ???

        val result = second.clone()
        book.setPages(resultPage)
        result.itemMeta = book

        return result
    }

    // Return true if append, false if remove, null if neither
    fun bookLoreEditTypeAppend(first: ItemStack, second: ItemStack): Boolean? {
        // Test if the book & quil contain content
        val meta = second.itemMeta as BookMeta

        var hasContent = false
        if (meta.hasPages() && meta.pageCount >= 1) {
            // Test if the pages is ok
            for (page in meta.pages) {
                if (page.isNotEmpty()) {
                    hasContent = true
                    break
                }
            }
        }

        // We don't want to "add" the first page is there is content and the first page is empty
        if (hasContent) {
            if (meta.pages[0].isEmpty()) return null
            if (ConfigOptions.appendLoreBookAndQuil)
                return true
        } else if (ConfigOptions.removeLoreBookAndQuil) {
            if (!first.hasItemMeta()) return null

            val leftMeta = first.itemMeta!!
            return if (leftMeta.hasLore()) false
            else null
        }
        return null
    }

    fun tryLoreEditByBook(player: HumanEntity, first: ItemStack, second: ItemStack): ItemStack? {
        val bookType = bookLoreEditTypeAppend(first, second) ?: return null

        val meta = second.itemMeta as BookMeta
        return if (bookType) handleLoreAppendByBook(player, first, meta)
        else handleLoreRemoveByBook(player, first, second, meta)
    }

}