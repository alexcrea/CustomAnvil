package xyz.alexcrea.cuanvil.util

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.annotations.NotNullByDefault
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.sendPaperMessage
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.setComponentDisplayName
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil.setPaperLore
import xyz.alexcrea.cuanvil.lang.Message

@Suppress("unused")
@NotNullByDefault
object ComponentUtil {

    @JvmStatic
    fun Component.serializeMM(): String {
        return MiniMessageUtil.mm.serialize(this)
    }

    @JvmStatic
    fun Component.serializeMMColor(): String {
        return MiniMessageUtil.colour_only_mm.serialize(this)
    }

    @JvmStatic
    fun Component.serializeLegacy(): String {
        return MiniMessageUtil.legacy_mm.serialize(this)
    }

    fun Component.serializePlain(): String {
        return MiniMessageUtil.plain_text_mm.serialize(this)
    }

    @JvmStatic
    fun Component.send(destination: CommandSender) {
        if(!destination.sendPaperMessage(this))
            destination.sendMessage(this.serializeLegacy())
    }

    @JvmStatic
    fun Collection<Component>.send(destination: CommandSender) {
        for(component in this)
            component.send(destination)
    }

    @JvmStatic
    fun List<Component>.applyLore(meta: ItemMeta) {
        if(!meta.setPaperLore(this))
            meta.lore = this.map {obj -> obj.serializeLegacy()}
    }

    @JvmStatic
    fun ItemMeta.applyLore(message: Message, vararg args: Any) {
        message.formatted(args).applyLore(this)
    }

    @JvmStatic
    fun ItemMeta.setMessageName(message: Message, vararg params: Any?) {
        this.setComponentDisplayName(message.formattedConcatenated(*params))
    }

    @JvmStatic
    fun List<Message>.asComponents(vararg params: Any?): List<Component> {
        val result = ArrayList<Component>()
        for(message in this) {
            result.addAll(message.formatted(*params))
        }
        return result
    }

    @JvmStatic
    fun Array<Message>.asComponents(vararg params: Any?): List<Component> {
        val result = ArrayList<Component>()
        for(message in this) {
            result.addAll(message.formatted(*params))
        }
        return result
    }


}