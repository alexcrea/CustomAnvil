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
import xyz.alexcrea.cuanvil.lang.Message
import xyz.alexcrea.cuanvil.lang.MsgCommand
import xyz.alexcrea.cuanvil.util.ComponentUtil.serializePlain

class DebugToggleExecutor : CASubCommand {

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
            MsgCommand.SHARED_MISSING_SUBCOMMAND.send(sender)
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

            sender.spigot().sendMessage(message);
        } else {
            sender.sendMessage(stb.toString())
        }
    }

    override fun tabCompleter(sender: CommandSender, args: Array<out String>, list: MutableList<String>) {
        if(!allowed(sender)) return

        list.addAll(
            when(args.size) {
                1 -> listOf("toggle", "get", "get-and-clear", "clear")
                2 -> when(args[0].lowercase()) {
                    "toggle" -> listOf("default", "verbose")
                    else -> listOf()
                }

                else -> listOf()
            }
        )
    }

}
