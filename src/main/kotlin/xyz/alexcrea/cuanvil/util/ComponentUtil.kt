package xyz.alexcrea.cuanvil.util

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.inventory.meta.ItemMeta
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.sendPaperMessage
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.setPaperLore

object ComponentUtil {

    fun Component.serializeMM(): String {
        return MiniMessageUtil.mm.serialize(this)
    }

    fun Component.serializeMMColor(): String {
        return MiniMessageUtil.color_only_mm.serialize(this)
    }

    fun Component.serializeLegacy(): String {
        return MiniMessageUtil.legacy_mm.serialize(this)
    }

    fun Component.serializePlain(): String {
        return MiniMessageUtil.plain_text_mm.serialize(this)
    }

    fun Component.send(destination: CommandSender) {
        if(!destination.sendPaperMessage(this))
            destination.sendMessage(this.serializeLegacy())
    }

    fun List<Component>.send(destination: CommandSender) {
        for(component in this)
            component.send(destination)
    }

    fun List<Component>.applyLore(meta: ItemMeta) {
        if(!meta.setPaperLore(this))
            meta.lore = this.map {obj -> obj.serializeLegacy()}
    }

}