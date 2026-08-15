package xyz.alexcrea.cuanvil.util

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.sendPaperMessage

object ComponentUtil {

    fun Component.send(destination: CommandSender) {
        if(!destination.sendPaperMessage(this))
            destination.sendMessage(MiniMessageUtil.legacy_mm.serialize(this))
    }

}