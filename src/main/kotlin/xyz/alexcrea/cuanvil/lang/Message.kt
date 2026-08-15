package xyz.alexcrea.cuanvil.lang

import io.delilaheve.CustomAnvil
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import xyz.alexcrea.cuanvil.util.ComponentUtil.send
import xyz.alexcrea.cuanvil.util.MiniMessageUtil
import java.util.logging.Level
import kotlin.math.min

interface MessageLike {

    fun log(vararg params: Any)

    fun send(destination: CommandSender, vararg params: Any)

    fun unformatted(vararg params: Any): String

    fun formatted(vararg params: Any): Component

    fun legacy(vararg params: Any): String {
        val formated = formatted(*params)
        return MiniMessageUtil.legacy_mm.serialize(formated)
    }
}

enum class MessageType {
    DEFAULT, WARNING, ERROR, COMMAND, UI,
}

open class Message(val key: String, vararg val params: String) : MessageLike {

    protected fun replaceParameters(stb: StringBuilder, vararg values: Any) {
        // replace all placeholder thingy %key -> value
        if(params.size != values.size) {
            CustomAnvil.log("Wrong number of argument for parameter for key $key (${params.size}/${values.size})")
        }

        for(i in 0 until min(params.size, values.size)) {
            val key = params[i]
            val replacement = values[i].toString()

            var current = 0
            while(true) {
                current = stb.indexOf('%', current) + 1
                if(current <= 0 || current + key.length > stb.length) break // may be able to be removed if bound checked in startsWith ?
                if(!stb.startsWith(key, current, false)) continue

                stb.replace(current - 1, current + key.length, replacement)
                current = current - 1 + replacement.length
            }
        }
    }

    override fun unformatted(vararg params: Any): String {
        val translated = Lang.getTranslated(key)
        if(params.isEmpty() && this.params.isEmpty()) return translated

        val stb = StringBuilder(translated)
        replaceParameters(stb, *params)
        return stb.toString()
    }

    override fun formatted(vararg params: Any): Component {
        val unformatted = unformatted(*params)

        return MiniMessageUtil.mm.deserialize(unformatted)
    }

    override fun log(vararg params: Any) {
        val text = unformatted(*params)

        CustomAnvil.instance.logger.info(text)
    }

    override fun send(destination: CommandSender, vararg params: Any) {
        formatted(*params).send(destination)
    }
}

class WarningMessage(key: String, vararg params: String) : Message("warning.$key", *params) {

    override fun log(vararg params: Any) {
        val text = unformatted(*params)

        CustomAnvil.instance.logger.warning(text)
    }

}

class ErrorMessage(key: String, vararg params: String) : Message("error.$key", *params) {

    override fun log(vararg params: Any) {
        val text = unformatted(*params)

        CustomAnvil.logError(text)
    }

    fun log(e: Throwable, vararg params: Any, level: Level = Level.SEVERE, track: Boolean = true) {
        val text = unformatted(*params)

        CustomAnvil.logError(text, e, track, level)
    }
}


class CommandMessage(key: String, vararg params: String) : Message("command.$key", *params)
class UIMessage(key: String, vararg params: String) : Message("ui.$key", *params)

class MultiLineMessage(type: MessageType, baseKey: String, count: Int, vararg params: String) : MessageLike {

    private val messages = ArrayList<MessageLike>()

    init {
        for(i in 1 until count + 1) {
            val message = createNew(type, "$baseKey.$i", *params)
            messages.add(message)
        }
    }

    private fun createNew(type: MessageType, key: String, vararg params: String): MessageLike {
        return when(type) {
            MessageType.DEFAULT -> Message(key, *params)
            MessageType.WARNING -> WarningMessage(key, *params)
            MessageType.ERROR -> ErrorMessage(key, *params)
            MessageType.COMMAND -> CommandMessage(key, *params)
            MessageType.UI -> UIMessage(key, *params)
        }
    }

    override fun log(vararg params: Any) {
        for(message in messages) {
            message.log(*params)
        }
    }

    override fun send(destination: CommandSender, vararg params: Any) {
        for(message in messages) {
            message.send(destination, *params)
        }
    }

    override fun unformatted(vararg params: Any): String {
        val stb = StringBuilder()
        stb.append(messages[0].unformatted(*params))

        for(i in 1 until messages.size) {
            stb.append('\n').append(messages[i].unformatted(*params))
        }

        return stb.toString()
    }

    override fun formatted(vararg params: Any): Component {
        val component = messages[0].formatted(*params)

        for(i in 1 until messages.size) {
            component.appendNewline().append(messages[i].formatted(*params))
        }

        return component
    }
}