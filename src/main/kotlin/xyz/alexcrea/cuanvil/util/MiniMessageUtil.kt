package xyz.alexcrea.cuanvil.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil

object MiniMessageUtil {

    val color_only_mm = MiniMessage.builder()
        .tags(
            TagResolver.resolver(
                StandardTags.color(),
                StandardTags.decorations()
            )
        )
        .build()

    val mm = if (PlatformUtil.isPaper) MiniMessage.miniMessage()
    else color_only_mm

    val legacy_mm = LegacyComponentSerializer.legacySection()
    val plain_text_mm = PlainTextComponentSerializer.plainText()

    // Keeping track of this as most use of this can be replaced later on v2 with pure component alternative
    fun fromLegacy(legacyText: String): TextComponent {
        return legacy_mm.deserialize(legacyText)
    }

    private val RESET_STYLE = Component.empty().style(Style.style(
        TextColor.color(256, 256, 256),
        TextDecoration.BOLD.withState(false),
        TextDecoration.ITALIC.withState(false),
        TextDecoration.OBFUSCATED.withState(false),
        TextDecoration.STRIKETHROUGH.withState(false),
        TextDecoration.UNDERLINED.withState(false)
    ))

    fun fromLegacyWithCorrectReset(legacyText: String): Component {
        val parts = legacyText.split("§r")
        if(parts.isEmpty()) return Component.empty()

        var start = legacy_mm.deserialize(parts[0])
        for(i in 1 until parts.size) {
            val part = legacy_mm.deserialize(parts[i])
            start = start.append(RESET_STYLE.append(part))
        }
        return start
    }

}
