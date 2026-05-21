package xyz.alexcrea.cuanvil.dialog

import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.PrepareAnvilEvent

interface AnvilRenameDialog {

    fun canSendDialog(): Boolean

    fun tryShowDialog(player: HumanEntity, event: PrepareAnvilEvent)

    fun closeInventory(player: HumanEntity)

}