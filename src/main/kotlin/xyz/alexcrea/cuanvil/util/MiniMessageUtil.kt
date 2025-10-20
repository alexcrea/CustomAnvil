package xyz.alexcrea.cuanvil.util

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object MiniMessageUtil {

    val mm = MiniMessage.builder()
        .tags(TagResolver.resolver(
            StandardTags.color(),
            StandardTags.decorations()))
        .build()

    val legacy_mm = LegacyComponentSerializer.legacySection()

}
