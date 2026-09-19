package xyz.alexcrea.cuanvil.lang

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import io.delilaheve.CustomAnvil
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import xyz.alexcrea.cuanvil.util.ComponentUtil.send
import xyz.alexcrea.cuanvil.util.ComponentUtil.serializeLegacy
import xyz.alexcrea.cuanvil.util.ComponentUtil.serializePlain
import xyz.alexcrea.cuanvil.util.MiniMessageUtil
import java.util.Collections
import java.util.logging.Level
import kotlin.math.max
import kotlin.math.min

open class Message(val key: String, vararg val params: String?, register: Boolean = true) {

    companion object {
        private val values = ArrayList<Message>()

        fun getValues(): Collection<Message> {
            return Collections.unmodifiableCollection(values)
        }
    }

    init {
        if(register) values.add(this)
    }

    protected fun replaceParameters(stb: StringBuilder, vararg values: Any?) {
        // replace all placeholder thingy %key -> value
        if(params.size != values.size) {
            CustomAnvil.log("Wrong number of argument for parameter for key $key (${params.size}/${values.size})")
            for(i in 0 until max(params.size, values.size)) {
                val key = if(i >= params.size) "NOT KEY"
                else params[i]
                val value = if(i >= values.size) "NOT VALUE"
                else values[i]

                CustomAnvil.log("Parameter ${i + 1} is $key with value $value")
            }
        }

        var foundBackslashPercent = false
        for(i in 0 until min(params.size, values.size)) {
            val key = params[i] ?: continue
            val value = values[i]
            if(value == null) {
                CustomAnvil.log("Passed a null value for key ${this.key} for Parameter $key (${i + 1})")
                continue
            }

            val replacement = value.toString()

            var current = 0
            while(true) {
                current = stb.indexOf('%', current)
                if(current > 0 && stb[current - 1] == '\\') {
                    foundBackslashPercent = true
                    current++
                    continue
                }
                if(++current <= 0 || current + key.length > stb.length) break // may be able to be removed if bound checked in startsWith ?
                if(!stb.startsWith(key, current, false)) continue

                stb.replace(current - 1, current + key.length, replacement)
                current = current - 1 + replacement.length
            }
        }

        if(foundBackslashPercent) {
            // Remove the \% to \
            var current = 0
            while(true) {
                current = stb.indexOf("\\%", current)
                if(current < 0) break

                stb.replace(current, current + 2, "%")
                current++
            }
        }
    }

    private fun unformattedMonoline(vararg params: Any?): String {
        val translated = Lang.getTranslated(key)
        if(params.isEmpty() && this.params.isEmpty()) return translated

        val stb = StringBuilder(translated)
        replaceParameters(stb, *params)
        return stb.toString()
    }

    private fun unformattedMultiline(section: ConfigurationSection, vararg params: Any?): String {
        val stb = StringBuilder()

        for(key in section.getKeys(false)) {
            if(!section.isString(key)) continue
            if(!stb.isEmpty()) stb.append('\n')

            stb.append(section.getString(key))
        }

        replaceParameters(stb, *params)
        return stb.toString()
    }

    fun unformatted(vararg params: Any?): String {
        val section = Lang.getSection(key)
        if(section != null) return unformattedMultiline(section, *params)

        return unformattedMonoline(*params)
    }

    private fun formattedMultiline(section: ConfigurationSection, vararg params: Any?): MutableList<Component> {
        val result = ArrayList<Component>()

        for(key in section.getKeys(false)) {
            if(!section.isString(key)) continue

            val stb = StringBuilder(section.getString(key))
            replaceParameters(stb, *params)

            result.add(MiniMessageUtil.mm.deserialize(stb.toString()))
        }

        if(result.isEmpty()) return mutableListOf(Component.text(key))
        return result
    }

    // return a list of AT LEAST 1 element. calling first is safe
    fun formatted(vararg params: Any?): MutableList<Component> {
        val section = Lang.getSection(key)
        if(section != null) return formattedMultiline(section, *params)

        val translated = unformattedMonoline(*params)

        return mutableListOf(MiniMessageUtil.mm.deserialize(translated))
    }

    fun formattedConcatenated(vararg params: Any?): Component {
        val formated = formatted(*params)

        var result = formated.first()
        for(i in 1 until formated.size) {
            result = result.appendNewline().append(formated[i])
        }

        return result
    }

    fun textHolder(vararg params: Any?): TextHolder {
        return ComponentHolder.of(formattedConcatenated(*params))
    }

    fun legacy(vararg params: Any?): String {
        val formated = formatted(*params)

        val stb = StringBuilder()
        for(component in formated) {
            if(!stb.isEmpty()) stb.append('\n')
            stb.append(component.serializeLegacy())
        }
        return stb.toString()
    }

    open fun log(vararg params: Any?) {
        val texts = formatted(*params)

        for(component in texts) {
            CustomAnvil.instance.logger.info(component.serializePlain())
        }
    }

    open fun send(destination: CommandSender, vararg params: Any?) {
        formatted(*params).send(destination)
    }
}

class WarningMessage(key: String, vararg params: String?): Message("warning.$key", *params) {

    override fun log(vararg params: Any?) {
        val texts = formatted(*params)

        for(component in texts) {
            CustomAnvil.instance.logger.warning(component.serializePlain())
        }
    }
}

class ErrorMessage(key: String, vararg params: String?): Message("error.$key", *params) {

    override fun log(vararg params: Any?) {
        val texts = formatted(*params)

        for(component in texts) {
            CustomAnvil.logError(component.serializePlain())
        }
    }

    fun log(e: Throwable, vararg params: Any?, level: Level = Level.SEVERE, track: Boolean = true) {
        val texts = formatted(*params)

        for(component in texts) {
            CustomAnvil.logError(component.serializePlain(), e, track, level)
        }
    }
}

class CommandMessage(key: String, vararg params: String?): Message("command.$key", *params)
class UIMessage(key: String, vararg params: String?): Message("config-ui.$key", *params)