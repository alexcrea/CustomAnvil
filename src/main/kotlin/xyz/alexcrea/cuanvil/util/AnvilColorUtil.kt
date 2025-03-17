package xyz.alexcrea.cuanvil.util

import org.bukkit.permissions.Permissible
import java.util.regex.Matcher
import java.util.regex.Pattern

object AnvilColorUtil {
    private val HEX_PATTERN: Pattern = Pattern.compile("#[A-Fa-f0-9]{6}") // pattern to find hexadecimal string
    private val TRANSFORMED_HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}") // pattern to find minecraft hex string

    /**
     * Color a stringbuilder object depending on allowed color type and player permissions on color use type
     * @return if the stringbuilder was changed and color applied
     */
    fun handleColor(
        textToColor: StringBuilder,
        player: Permissible,
        usePermission: Boolean,
        allowColorCode: Boolean,
        allowHexadecimalColor: Boolean,
        useType: ColorUseType
    ): Boolean {
        if (!allowColorCode && !allowHexadecimalColor) return false

        val canUseColorCode =
            allowColorCode && (!usePermission || useType.colorCodePerm == null || player.hasPermission(
                useType.colorCodePerm
            ))
        val canUseHexColor =
            allowHexadecimalColor && (!usePermission || useType.hexColorPerm == null || player.hasPermission(
                useType.hexColorPerm
            ))

        if ((!canUseColorCode) && (!canUseHexColor)) return false

        var useColor = false
        // Handle color code
        if (canUseColorCode) {
            var nbReplacement = replaceAll(textToColor, "&", "§", 2)
            nbReplacement -= 2 * replaceAll(textToColor, "§§", "&", 2)

            if (nbReplacement > 0) useColor = true
        }

        if (canUseHexColor) {
            val nbReplacement = replaceHexToColor(textToColor, 7)

            if (nbReplacement > 0) useColor = true
        }

        return useColor
    }

    /**
     * Revert a stringbuilder to a state where applying handleColor with the same options would give the same result
     * @return if the stringbuilder was changed and color unapplied
     */
    fun revertColor(
        colorToText: StringBuilder,
        player: Permissible,
        usePermission: Boolean,
        allowColorCode: Boolean,
        allowHexadecimalColor: Boolean,
        useType: ColorUseType
    ): Boolean {
        if (!allowColorCode && !allowHexadecimalColor) return false

        val canUseColorCode =
            allowColorCode && (!usePermission || useType.colorCodePerm == null || player.hasPermission(
                useType.colorCodePerm
            ))
        val canUseHexColor =
            allowHexadecimalColor && (!usePermission || useType.hexColorPerm == null || player.hasPermission(
                useType.hexColorPerm
            ))

        if ((!canUseColorCode) && (!canUseHexColor)) return false
        var hasReversed = false

        // Reverse hex pattern
        if (canUseHexColor) {
            val nbReplacement = replaceHexToColor(colorToText, 14)

            if (nbReplacement > 0) hasReversed = true
        }

        if (canUseColorCode) {
            replaceAll(colorToText, "&", "&&", 1)
            val nbReplacement = replaceAll(colorToText, "§", "&", 2)

            if (nbReplacement > 0) hasReversed = true
        }

        return hasReversed
    }

    /**
     * Replace every instance of "from" to "to".
     * @param builder The builder to replace the string from.
     * @param from The source that should be replaced.
     * @param to The string that should replace.
     * @param endOffset Amount of character that should be ignored at the end.
     * @return The number of replacement was that was done.
     */
    private fun replaceAll(builder: java.lang.StringBuilder, from: String, to: String, endOffset: Int): Int {
        var index = builder.indexOf(from)
        var numberOfChanges = 0

        while (index != -1 && index < builder.length - endOffset) {
            builder.replace(index, index + from.length, to)
            index += to.length
            index = builder.indexOf(from, index)

            numberOfChanges += 1
        }

        return numberOfChanges
    }

    /**
     * Replace every hex color formatted like #000000 to the minecraft format
     * @param builder The builder to replace the hex color from.
     * @param endOffset Amount of character that should be ignored at the end.
     * @return The number of replacement was that was done.
     */
    private fun replaceHexToColor(builder: StringBuilder, endOffset: Int): Int {
        val matcher: Matcher = HEX_PATTERN.matcher(builder)

        var numberOfChanges = 0
        var startIndex = 0

        while (matcher.find(startIndex)) {
            startIndex = matcher.start()
            if (startIndex >= builder.length - endOffset) break //HOW AND WHERE WOULD THIS HAPPEN ?????

            builder.replace(startIndex, startIndex + 1, "§x")
            startIndex += 2
            for (i in 0..5) {
                builder.insert(startIndex, '§')
                startIndex += 2
            }

            numberOfChanges += 1
        }

        return numberOfChanges
    }

    /**
     * Replace every hex color from the minecraft format to a format like #000000
     * @param builder The builder to replace the minecraft hex color from.
     * @param endOffset Amount of character that should be ignored at the end.
     * @return The number of replacement was that was done.
     */
    private fun replaceColorToHex(builder: StringBuilder, endOffset: Int): Int {
        val matcher: Matcher = TRANSFORMED_HEX_PATTERN.matcher(builder)

        var numberOfChanges = 0
        var startIndex = 0

        while (matcher.find(startIndex)) {
            startIndex = matcher.start()
            if (startIndex >= builder.length - endOffset) break //HOW AND WHERE WOULD THIS HAPPEN ?????

            builder.replace(startIndex, startIndex + 2, "#")
            startIndex += 1
            for (i in 0..5) {
                builder.deleteCharAt(startIndex)
                startIndex += 1
            }

            numberOfChanges += 1
        }

        return numberOfChanges
    }

    enum class ColorUseType(
        val colorCodePerm: String?,
        val hexColorPerm: String?
    ) {
        RENAME("ca.color.code", "ca.color.hex"),
        LORE_EDIT(null, null)
    }

}