package xyz.alexcrea.cuanvil.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.jetbrains.annotations.Contract

object MiniMessageUtil {

    val color_only_mm = MiniMessage.builder()
        .tags(
            TagResolver.resolver(
                StandardTags.color(),
                StandardTags.decorations()
            )
        )
        .build()

    val mm = MiniMessage.miniMessage()

    val legacy_mm = LegacyComponentSerializer.legacySection()
    val plain_text_mm = PlainTextComponentSerializer.plainText()

    // Keeping track of this as most use of this can be replaced later on v2 with pure component alternative
    fun fromLegacy(legacyText: String): TextComponent {
        return legacy_mm.deserialize(legacyText)
    }

    @Contract("!null -> !null; null -> null")
    fun stripTags(text: String?): String? {
        if(text == null) return null

        val partial = legacy_mm.deserialize(text)
        return plain_text_mm.serialize(partial)
    }

    @Contract("!null -> !null; null -> null")
    fun stripTags(component: Component?): Component? {
        if(component == null) return null

        val partial = plain_text_mm.serialize(component)
        return plain_text_mm.deserialize(partial)
    }

}
