package xyz.alexcrea.cuanvil.util.anvil

import io.delilaheve.util.ConfigOptions
import net.kyori.adventure.text.Component
import org.bukkit.permissions.Permissible
import xyz.alexcrea.cuanvil.util.MiniMessageUtil
import java.util.regex.Matcher
import java.util.regex.Pattern

object AnvilColorUtil {
    private val HEX_PATTERN: Pattern = Pattern.compile("#[A-Fa-f0-9]{6}") // pattern to find hexadecimal string
    private val TRANSFORMED_HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}") // pattern to find minecraft hex string

    class ColorPermissions(
        val canUseColorCode: Boolean,
        val canUseHexColor: Boolean,
        val canUseMinimessage: Boolean,
        val permissible: Permissible, // source of the permission. tried to avoid needing it but meh
    ) {
        fun allowed(): Boolean {
            return canUseColorCode || canUseHexColor || canUseMinimessage
        }

        fun onlyMinimessage(): Boolean {
            return canUseMinimessage && !canUseColorCode && !canUseHexColor
        }
    }

    fun calculatePermissions(
        player: Permissible,
        usePermission: Boolean,
        allowColorCode: Boolean,
        allowHexadecimalColor: Boolean,
        allowMinimessage: Boolean,
        useType: ColorUseType
    ): ColorPermissions {
        if (!allowColorCode && !allowHexadecimalColor && !allowMinimessage)
            return ColorPermissions(
                canUseColorCode = false,
                canUseHexColor = false,
                canUseMinimessage = false,
                player,
            )

        val canUseColorCode =
            allowColorCode && (!usePermission || useType.colorCodePerm == null || player.hasPermission(
                useType.colorCodePerm
            ))

        val canUseMinimessage =
            allowMinimessage && (!usePermission || useType.minimessagePerm == null || player.hasPermission(
                useType.minimessagePerm
            ))

        val canUseHexColor =
            allowHexadecimalColor && (!usePermission || useType.hexColorPerm == null || player.hasPermission(
                useType.hexColorPerm
            ))

        return ColorPermissions(canUseColorCode, canUseHexColor, canUseMinimessage, player)
    }

    fun renamePermission(player: Permissible): ColorPermissions {
        return calculatePermissions(player,
            ConfigOptions.permissionNeededForColor,
            ConfigOptions.allowColorCode, ConfigOptions.allowHexadecimalColor, ConfigOptions.allowMinimessage,
            ColorUseType.RENAME)
    }

    /**
     * Color a string depending on permitted use
     * @return colored component or null if nothing has been colored
     */
    fun handleColor(
        textToColorText: String,
        permission: ColorPermissions,

        ): Component? {
        if (!permission.allowed()) return null

        val textToColor = StringBuilder(textToColorText)
        var useColor = false
        // Handle color code
        if (permission.canUseColorCode) { // maybe should use LegacyComponentSerializer ?
            var nbReplacement = replaceAll(textToColor, "&", "§", 2)
            nbReplacement -= 2 * replaceAll(textToColor, "§§", "&", 2)

            if (nbReplacement > 0) {
                useColor = true

                if (ConfigOptions.usePerColorCodePermission)
                    filterPermissibleColorCode(textToColor, permission.permissible)
            }
        }

        if (permission.canUseHexColor) {
            val nbReplacement = replaceHexToColor(textToColor, 7, permission.canUseMinimessage)

            if (nbReplacement > 0) useColor = true
        }

        val previousStr = textToColor.toString()
        var result: Component = MiniMessageUtil.legacy_mm.deserialize(previousStr)
        if (permission.canUseMinimessage) {
            // we dance with formats here
            val toMinimessage = MiniMessageUtil.mm.serialize(result)
            val hackySolution = toMinimessage.replace("\\<", "<")
            val fromMinimessage = MiniMessageUtil.mm.deserialize(hackySolution)
            val asPlain = MiniMessageUtil.plain_text_mm.serialize(fromMinimessage)

            if (previousStr != asPlain) {
                useColor = true
                result = fromMinimessage
            }
        }

        return if (useColor) result
        else null
    }

    private fun filterPermissibleColorCode(textToColor: StringBuilder, player: Permissible) {
        var index = 0
        while (true) {
            index = textToColor.indexOf('§', index)
            if (index == -1 || index == textToColor.length - 1) return

            val next = textToColor[index + 1]
            // check permission for this color
            if(!player.hasPermission("ca.color.code.$next"))
                textToColor.replace(index, index + 1, "&")

            index++
        }
    }

    /**
     * Best effort to revert a component to the smallest allowed string
     * that would result in it getting closest as possible to handleColor
     * with current set of permitted use
     * @return a new component if had any change. null otherwise
     */
    fun revertColorSmallest(
        component: Component?,
        permission: ColorPermissions
    ): String? {
        if (!permission.allowed() || component == null) return null

        val transformed = MiniMessageUtil.mm.serialize(component)
        val plainTransform = MiniMessageUtil.plain_text_mm.serialize(component)
        if (transformed == plainTransform) return null
        if (permission.onlyMinimessage()) {
            return transformed
        }

        // smol dance so we transform the component that may contain other tag into only decoration & color for legacy
        val coloredMessage = MiniMessageUtil.color_only_mm.deserialize(transformed)
        val legacyMessage = StringBuilder(MiniMessageUtil.legacy_mm.serialize(coloredMessage))

        // Reverse hex pattern
        if (permission.canUseHexColor) {
            replaceColorToHex(legacyMessage, 14)
        }

        // Reverse color pattern
        if (permission.canUseColorCode) {
            replaceAll(legacyMessage, "&", "&&", 1)
            replaceAll(legacyMessage, "§", "&", 2)
        }

        // In case we still has some § around by lack of permission we need to convert it back from legacy
        // In other word it's time for dance #3
        val fromLegacy = MiniMessageUtil.legacy_mm.deserialize(legacyMessage.toString())
        val middleGround = MiniMessageUtil.mm.serialize(fromLegacy)
        val hackySolutionStb = StringBuilder(middleGround)
        replaceAll(hackySolutionStb, "\\<", "<", 2)
        val hackySolution = hackySolutionStb.toString()

        val result: String =
            if (permission.canUseMinimessage) hackySolution
            else MiniMessageUtil.mm.stripTags(hackySolution)

        return if (result == plainTransform) null
        else result
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
    private fun replaceHexToColor(builder: StringBuilder, endOffset: Int, checkTag: Boolean): Int {
        val matcher: Matcher = HEX_PATTERN.matcher(builder)

        var numberOfChanges = 0
        var startIndex = 0

        while (matcher.find(startIndex)) {
            startIndex = matcher.start()
            if (startIndex >= builder.length - endOffset) break //HOW AND WHERE WOULD THIS HAPPEN ?????
            if (checkTag && isInTag(builder, startIndex)) {
                startIndex += 1 // Avoid infinite loop
                continue
            }

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

    // Simple check if < > with some smart check like <> > not taken into account
    // This is easily bypassable but if the player want to bypass he has better alternative
    // AKA should avoid getting into any tag
    private fun isInTag(builder: StringBuilder, index: Int): Boolean {
        // Check left tag we have < after last >
        val left = builder.slice(0..index)
        val leftIndex = left.lastIndexOf("<")
        var rightIndex = left.lastIndexOf(">")

        // last < do not exist or is before last >
        if (leftIndex == -1 || rightIndex > leftIndex) return false

        val right = builder.slice(index..<builder.length)
        val newleftIndex = right.indexOf("<")
        rightIndex = right.indexOf(">")

        // first > do not exist or is after first < (if exist)
        if (rightIndex == -1 || (newleftIndex != -1 && newleftIndex < rightIndex)) return false

        // Then finally we use minimessage to check for tag
        val expectedTag = builder.substring(leftIndex, newleftIndex + index + 1)
        val notag = MiniMessageUtil.mm.stripTags(expectedTag)

        return notag != expectedTag
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
        val hexColorPerm: String?,
        val minimessagePerm: String?
    ) {
        RENAME("ca.color.code", "ca.color.hex", "ca.rename.minimessage"),
        LORE_EDIT(null, null, null)
    }

}