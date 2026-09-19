package xyz.alexcrea.cuanvil.util.anvil

import io.delilaheve.util.ConfigOptions
import net.kyori.adventure.text.Component
import org.bukkit.permissions.Permissible
import xyz.alexcrea.cuanvil.util.ComponentUtil.serializeLegacy
import xyz.alexcrea.cuanvil.util.ComponentUtil.serializeMM
import xyz.alexcrea.cuanvil.util.ComponentUtil.serializePlain
import xyz.alexcrea.cuanvil.util.MiniMessageUtil
import java.util.regex.Matcher
import java.util.regex.Pattern

object AnvilColourUtil {
    private val HEX_PATTERN: Pattern = Pattern.compile("#[A-Fa-f0-9]{6}") // pattern to find hexadecimal string
    private val TRANSFORMED_HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}") // pattern to find minecraft hex string

    private val COLOUR_CODE_PATTERN = Pattern.compile("§[0-9a-f]")

    class ColourPermissions(
        val canUseColourCode: Boolean,
        val canUseHexColour: Boolean,
        val canUseMinimessage: Boolean,
        val permissible: Permissible, // source of the permission. tried to avoid needing it but meh
    ) {
        fun allowed(): Boolean {
            return canUseColourCode || canUseHexColour || canUseMinimessage
        }

        fun onlyMinimessage(): Boolean {
            return canUseMinimessage && !canUseColourCode && !canUseHexColour
        }
    }

    fun calculatePermissions(
        player: Permissible,
        usePermission: Boolean,
        allowColourCode: Boolean,
        allowHexadecimalColour: Boolean,
        allowMinimessage: Boolean,
        useType: ColourUseType
    ): ColourPermissions {
        if (!allowColourCode && !allowHexadecimalColour && !allowMinimessage)
            return ColourPermissions(
                canUseColourCode = false,
                canUseHexColour = false,
                canUseMinimessage = false,
                player,
            )

        val canUseColourCode =
            allowColourCode && (!usePermission || useType.colourCodePerm == null || player.hasPermission(
                useType.colourCodePerm
            ))

        val canUseMinimessage =
            allowMinimessage && (!usePermission || useType.minimessagePerm == null || player.hasPermission(
                useType.minimessagePerm
            ))

        val canUseHexColour =
            allowHexadecimalColour && (!usePermission || useType.hexColourPerm == null || player.hasPermission(
                useType.hexColourPerm
            ))

        return ColourPermissions(canUseColourCode, canUseHexColour, canUseMinimessage, player)
    }

    fun renamePermission(player: Permissible): ColourPermissions {
        return calculatePermissions(player,
            ConfigOptions.permissionNeededForColour,
            ConfigOptions.allowColourCode, ConfigOptions.allowHexadecimalColour, ConfigOptions.allowMinimessage,
            ColourUseType.RENAME)
    }

    /**
     * Colour a string depending on permitted use
     * @return coloured component or null if nothing has been coloured
     */
    fun handleColour(
        textToColourText: String,
        permission: ColourPermissions,

        ): Component? {
        if (!permission.allowed()) return null

        val textToColour = StringBuilder(textToColourText)
        var useColour = false
        // Handle colour code
        if (permission.canUseColourCode) { // maybe should use LegacyComponentSerializer ?
            var nbReplacement = replaceAll(textToColour, "&", "§", 2)
            nbReplacement -= 2 * replaceAll(textToColour, "§§", "&", 2)

            if (nbReplacement > 0) {
                useColour = true

                if (ConfigOptions.usePerColourCodePermission)
                    filterPermissibleColourCode(textToColour, permission.permissible)
            }
            if(ConfigOptions.shouldResetOnColourCode) {
                prefixColourCodes(textToColour)
            }
        }

        if (permission.canUseHexColour) {
            val nbReplacement = replaceHexToColour(textToColour, 7, permission.canUseMinimessage)

            if (nbReplacement > 0) useColour = true
        }

        val previousStr = textToColour.toString()
        var result: Component = MiniMessageUtil.fromLegacyWithCorrectReset(previousStr)
        if (permission.canUseMinimessage) {
            // we dance with formats here
            val toMinimessage = result.serializeMM()
            val hackySolution = toMinimessage.replace("\\<", "<")
            val fromMinimessage = MiniMessageUtil.mm.deserialize(hackySolution)
            val asPlain = fromMinimessage.serializePlain()

            if (previousStr != asPlain) {
                useColour = true
                result = fromMinimessage
            }
        }

        return if (useColour) result
        else null
    }

    private fun prefixColourCodes(builder: StringBuilder) {
        val matcher: Matcher = COLOUR_CODE_PATTERN.matcher(builder)

        var startIndex = 0

        while(matcher.find(startIndex)) {
            startIndex = matcher.start()

            builder.insert(startIndex, "§r")
            startIndex+=4
        }
    }

    private fun filterPermissibleColourCode(textToColour: StringBuilder, player: Permissible) {
        var index = 0
        while (true) {
            index = textToColour.indexOf('§', index)
            if (index == -1 || index == textToColour.length - 1) return

            val next = textToColour[index + 1]
            // check permission for this colour
            if(!player.hasPermission("ca.color.code.$next"))
                textToColour.replace(index, index + 1, "&")

            index++
        }
    }

    /**
     * Best effort to revert a component to the smallest allowed string
     * that would result in it getting closest as possible to handleColour
     * with current set of permitted use
     * @return a new component if had any change. null otherwise
     */
    fun revertColourSmallest(
        component: Component?,
        permission: ColourPermissions
    ): String? {
        if (!permission.allowed() || component == null) return null

        val transformed = component.serializeMM()
        val plainTransform = component.serializePlain()
        if (transformed == plainTransform) return null
        if (permission.onlyMinimessage()) {
            return transformed
        }

        // smol dance so we transform the component that may contain other tag into only decoration & colour for legacy
        val colouredMessage = MiniMessageUtil.colour_only_mm.deserialize(transformed)
        val legacyMessage = StringBuilder(colouredMessage.serializeLegacy())

        // Reverse hex pattern
        if (permission.canUseHexColour) {
            replaceColourToHex(legacyMessage, 14)
        }

        // Reverse colour pattern
        if (permission.canUseColourCode) {
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
     * Replace every hex colour formatted like #000000 to the minecraft format
     * @param builder The builder to replace the hex colour from.
     * @param endOffset Amount of character that should be ignored at the end.
     * @return The number of replacement was that was done.
     */
    private fun replaceHexToColour(builder: StringBuilder, endOffset: Int, checkTag: Boolean): Int {
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
            if(startIndex > 0 && builder[startIndex - 1] == '§') {
                builder.replace(startIndex - 1, startIndex + 1, "#")
                // Voluntarily do not update startindex
                // if we had &|#123456 (| being start index) then we get #1|23456 so won't trigger matcher again !
                continue
            }

            val replacement = "${if(ConfigOptions.shouldResetOnColourCode)"§r" else ""}§x"

            builder.replace(startIndex, startIndex + 1, replacement)
            startIndex += replacement.length
            repeat(6) {
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
     * Replace every hex colour from the minecraft format to a format like #000000
     * @param builder The builder to replace the minecraft hex colour from.
     * @param endOffset Amount of character that should be ignored at the end.
     * @return The number of replacement was that was done.
     */
    private fun replaceColourToHex(builder: StringBuilder, endOffset: Int): Int {
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

    enum class ColourUseType(
        val colourCodePerm: String?,
        val hexColourPerm: String?,
        val minimessagePerm: String?
    ) {
        RENAME("ca.color.code", "ca.color.hex", "ca.rename.minimessage"),
        LORE_EDIT(null, null, null)
    }

}