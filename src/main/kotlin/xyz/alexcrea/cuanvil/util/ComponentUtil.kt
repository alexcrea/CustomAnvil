package xyz.alexcrea.cuanvil.util

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.sendPaperMessage

object ComponentUtil {

    fun Component.send(destination: CommandSender) {
        if(!destination.sendPaperMessage(this))
            destination.sendMessage(this.serializeLegacy())
    }

    fun List<Component>.send(destination: CommandSender) {
        for(component in this)
            component.send(destination)
    }

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


}