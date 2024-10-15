package xyz.alexcrea.cuanvil.util

import io.delilaheve.util.ConfigOptions
import org.bukkit.entity.HumanEntity
import java.util.regex.Matcher
import java.util.regex.Pattern

object AnvilColorUtil {
    private val HEX_PATTERN: Pattern = Pattern.compile("#[A-Fa-f0-9]{6}") // pattern to find hexadecimal string

    fun handleRenamingColor(textToColor: StringBuilder, player: HumanEntity): Boolean {
        val usePermission = ConfigOptions.permissionNeededForColor
        val canUseColorCode = ConfigOptions.allowColorCode && (!usePermission || player.hasPermission("ca.color.code"))
        val canUseHexColor = ConfigOptions.allowHexadecimalColor && (!usePermission || player.hasPermission("ca.color.hex"))

        if((!canUseColorCode) && (!canUseHexColor)) return false

        var useColor = false
        // Handle color code
        if(canUseColorCode){
            var nbReplacement = replaceAll(textToColor, "&", "§", 2)
            nbReplacement -= 2 * replaceAll(textToColor, "§§", "&", 2)

            if(nbReplacement > 0) useColor = true
        }

        if(canUseHexColor){
            val nbReplacement = replaceHexToColor(textToColor, 7)

            if(nbReplacement > 0) useColor = true
        }

        return useColor
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

            numberOfChanges+=1
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

        while(matcher.find(startIndex)){
            startIndex = matcher.start()
            if(startIndex >= builder.length - endOffset) break

            builder.replace(startIndex, startIndex + 1, "§x")
            startIndex+=2
            for (i in 0..5) {
                builder.insert(startIndex, '§')
                startIndex+=2
            }

            numberOfChanges+=1
        }

        return numberOfChanges
    }

}