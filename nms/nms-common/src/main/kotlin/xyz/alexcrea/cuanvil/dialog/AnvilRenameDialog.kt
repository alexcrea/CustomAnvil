package xyz.alexcrea.cuanvil.dialog

import org.bukkit.NamespacedKey
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.PrepareAnvilEvent

interface AnvilRenameDialog {

    companion object {
        val PCD_KEEP_RENAME_TEXT_KEY = NamespacedKey.fromString("customanvil:last_rename_text")!!
    }

    fun canSendDialog(): Boolean

    fun tryShowDialog(player: HumanEntity, event: PrepareAnvilEvent)

    fun closeInventory(player: HumanEntity)

    fun currentText(player: HumanEntity): String?

}