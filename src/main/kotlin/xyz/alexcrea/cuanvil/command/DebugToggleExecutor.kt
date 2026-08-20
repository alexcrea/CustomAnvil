package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.alexcrea.cuanvil.lang.Lang
import xyz.alexcrea.cuanvil.lang.Message
import xyz.alexcrea.cuanvil.lang.MsgCommand
import xyz.alexcrea.cuanvil.lang.MsgError
import xyz.alexcrea.cuanvil.lang.MsgUI
import xyz.alexcrea.cuanvil.lang.MsgWarning
import xyz.alexcrea.cuanvil.util.ComponentUtil.serializePlain
import java.util.Locale

class DebugToggleExecutor: CASubCommand {

    override fun description(): Message {
        return MsgCommand.DEBUG_DESCRIPTION
    }

    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.diagnosticPermission)
    }

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>,
    ): Boolean {
        if(!allowed(sender)) {
            MsgCommand.SHARED_NO_DIAG_PERM.send(sender)
            return false
        }

        if(args.isEmpty()) {
            MsgCommand.SHARED_MISSING_SUBCOMMAND.send(sender, "\"toggle\"", "\"get\"")
            return true
        }
        when(args[0].lowercase()) {
            "toggle" -> executeToggle(sender, args)
            "get" -> executeGet(sender)
            "get-and-clear" -> {
                executeGet(sender)
                CustomAnvil.debugStorageQueue.clear()
            }

            "clear" -> {
                CustomAnvil.debugStorageQueue.clear()
                MsgCommand.DEBUG_LOG_CLEARED.send(sender)
            }

            "lang" -> {
                executeLanguageDebug(sender, args)
            }

            else -> {
                MsgCommand.SHARED_UNKNOWN_SUB_COMMAND.send(sender)
                return false
            }
        }
        return true
    }

    private fun executeToggle(sender: CommandSender, args: Array<out String>) {
        if(args.size < 2) {
            MsgCommand.DEBUG_WARNING_UNSPECIFIED_TYPE.send(sender)
            return
        }
        when(args[1].lowercase()) {
            "default" -> {
                ConfigOptions.OVERRIDE_DEBUG_LOG = !ConfigOptions.debugLog
                MsgCommand.DEBUG_TOGGLED.send(sender, ConfigOptions.debugLog)
            }

            "verbose" -> {
                ConfigOptions.OVERRIDE_VERBOSE_DEBUG_LOG = !ConfigOptions.verboseDebugLog
                MsgCommand.DEBUG_TOGGLED.send(sender, ConfigOptions.debugLog)
            }

            else -> MsgCommand.DEBUG_WARNING_INVALID_TYPE.send(sender, ConfigOptions.debugLog)
        }
    }

    private fun executeGet(sender: CommandSender) {
        if(CustomAnvil.debugStorageQueue.isEmpty()) {
            MsgCommand.DEBUG_WARNING_NO_LOG.send(sender, "/ca debug toggle")
            return
        }

        val stb = StringBuilder(MsgCommand.DEBUG_DATA_HEADER.unformatted()).append(' ')
        stb.append(MsgCommand.DEBUG_DATA_LINE_COUNT.unformatted(CustomAnvil.debugStorageQueue.size))
        for(log in CustomAnvil.debugStorageQueue) {
            stb.append('\n').append(log.serializePlain())
        }

        if(sender is Player) {
            val message = TextComponent(MsgCommand.DEBUG_COPY.legacy())

            message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, stb.toString())
            message.hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Text(MsgCommand.SHARED_HOVER_COPY.legacy())
            )

            sender.spigot().sendMessage(message)
        } else {
            sender.sendMessage(stb.toString())
        }
    }

    private fun executeLanguageDebug(sender: CommandSender, args: Array<out String>) {
        if(args.size > 1 && "details".contentEquals(args[1], ignoreCase = true))
            detailedLangDebug(sender)
        else
            simpleLangDebug(sender)

    }

    private fun simpleLangDebug(sender: CommandSender) {
        var validCount = 0
        // load key from all provider class
        MsgCommand.DEBUG_DATA_HEADER
        MsgUI.SHARED_CONFIG_NO_EDIT_PERM
        MsgError.LOAD_LISTENERS
        MsgWarning.ANVIL_GENERIC_EXCEPTION

        val registeredKeys = Message.getValues()

        for(message in registeredKeys)
            if(Lang.has(message.key)) validCount++

        val valid = (100.0 * validCount) / registeredKeys.size
        sender.sendMessage(
            "Translated (${Lang.currentLang()}): ${
                "%.1f".format(
                    Locale.ROOT,
                    valid
                )
            }% ($validCount/${registeredKeys.size})"
        )
    }

    private fun detailedLangDebug(sender: CommandSender) {
        simpleLangDebug(sender)

        val stb = StringBuilder("Report of potential issue for language ${Lang.currentLang()}:\n")

        var hadAny = false
        val keySet = mutableSetOf<String>()
        val registeredKeys = Message.getValues()

        for(message in registeredKeys) {
            val key = message.key
            if(!Lang.has(key)) {
                stb.append("Missing key inside translation file: $key\n")
                hadAny = true
            } else if(hashParamIssue(message, stb))
                hadAny = true

            if(keySet.contains(key)) {
                stb.append("Duplicate registered key: $key\n")
                hadAny = true
            } else keySet.add(key)
        }

        for(key in Lang.getKeys()) {
            if(!keySet.contains(key)) {
                stb.append("Found unregistered key: $key\n")
                hadAny = true
            }
        }

        if(hadAny) {
            val message = TextComponent(MsgCommand.DEBUG_LANG_COPY.legacy())

            message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, stb.toString())
            message.hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Text(MsgCommand.SHARED_HOVER_COPY.legacy())
            )

            sender.spigot().sendMessage(message)
        } else {
            sender.sendMessage("No additional issue found")
        }
    }

    private fun hashParamIssue(message: Message, stb: StringBuilder): Boolean {
        val section = Lang.getSection(message.key)
        val texts = if(section == null)
            listOf(Lang.getTranslated(message.key))
        else
            section.getValues(false).map {it.value.toString()}

        val textParams = ArrayList<String>()

        for(text in texts) {
            var index = 0
            while(true) {
                index = text.indexOf('%', index)
                if(index > 0 && text[index - 1] == '\\') {
                    index++
                    continue
                }
                if(index++ < 0) break

                var end = text.indexOf(' ', index)
                if(end == -1) end = text.length
                val param = text.substring(index, end)
                if(!textParams.contains(param)) textParams.add(param)
            }
        }

        var hadIssue = false
        // Check all parameter are valid
        val usedParam = mutableSetOf<String>()
        for(textParam in textParams) {
            var found = false
            for(param in message.params) {
                if(param == null) continue
                if(textParam.startsWith(param)) {
                    found = true
                    usedParam.add(param)
                    break
                }
            }
            if(found) continue

            hadIssue = true
            stb.append("Did not found param %$textParam in register list for ${message.key}\n")
        }

        for(param in message.params) {
            if(param == null) continue
            var found = false
            for(used in usedParam) {
                if(!used.startsWith(param)) continue
                found = true
                break
            }

            if(found) continue
            hadIssue = true
            stb.append("Param %$param is not used for key ${message.key}\n")
        }

        return hadIssue
    }

    override fun tabCompleter(sender: CommandSender, args: Array<out String>, list: MutableList<String>) {
        if(!allowed(sender)) return

        list.addAll(
            when(args.size) {
                1 -> listOf("toggle", "get", "get-and-clear", "clear", "lang")
                2 -> when(args[0].lowercase()) {
                    "toggle" -> listOf("default", "verbose")
                    "lang" -> listOf("details")
                    else -> listOf()
                }

                else -> listOf()
            }
        )
    }

}
