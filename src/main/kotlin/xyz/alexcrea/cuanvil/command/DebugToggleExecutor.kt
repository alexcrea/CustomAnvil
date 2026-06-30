package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.alexcrea.cuanvil.command.DiagnosticExecutor.Companion.NO_DIAG_PERM

class DebugToggleExecutor : CASubCommand {

    override fun description(): String {
        return "Used to toggle debug logs and retrieve it"
    }

    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.diagnosticPermission)
    }

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean {
        if (!allowed(sender)) {
            sender.sendMessage(NO_DIAG_PERM)
            return false
        }

        if (args.isEmpty()) {
            sender.sendMessage("Need to specify a subcommand: \"toggle\" or \"get\"")
            return true
        }
        when (args[0].lowercase()) {
            "toggle" -> executeToggle(sender, args)
            "get" -> executeGet(sender)
            "get-and-clear" -> {
                executeGet(sender)
                CustomAnvil.debugStorageQueue.clear()
            }

            "clear" -> {
                CustomAnvil.debugStorageQueue.clear()
                sender.sendMessage("Log Cleared")
            }

            else -> return false
        }
        return true
    }

    private fun executeToggle(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("Need to specify which type of debug to toggle: \"default\" or \"verbose\"")
            return
        }
        when (args[1].lowercase()) {
            "default" -> {
                ConfigOptions.OVERRIDE_DEBUG_LOG = !ConfigOptions.debugLog
                sender.sendMessage("Debug toggle to: ${ConfigOptions.debugLog}")
            }

            "verbose" -> {
                ConfigOptions.OVERRIDE_VERBOSE_DEBUG_LOG = !ConfigOptions.verboseDebugLog
                sender.sendMessage("Debug toggle to: ${ConfigOptions.verboseDebugLog}")
            }

            else -> sender.sendMessage("Invalid debug type: ${args[1]}")
        }

    }

    private fun executeGet(sender: CommandSender) {
        val stb = StringBuilder("Debug Log data:")
        if (CustomAnvil.debugStorageQueue.isEmpty()) {
            sender.sendMessage("No log to show ? make sure you tried with debug log toggled (/ca debug toggle)")
            return
        }

        stb.append("\nFound ${CustomAnvil.debugStorageQueue.size} lines\n")
        for (log in CustomAnvil.debugStorageQueue) {
            stb.append('\n').append(log)
        }

        if (sender is Player) {
            val message = TextComponent(ChatColor.GREEN.toString() + "Click to copy log data")

            message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, stb.toString())
            message.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("§7Click to copy"))

            sender.spigot().sendMessage(message);
        } else {
            sender.sendMessage(stb.toString())
        }
    }

    override fun tabCompleter(sender: CommandSender, args: Array<out String>, list: MutableList<String>) {
        if (!allowed(sender)) return

        list.addAll(
            when (args.size) {
                1 -> listOf("toggle", "get", "get-and-clear", "clear")
                2 -> when (args[0].lowercase()) {
                    "toggle" -> listOf("default", "verbose")
                    else -> listOf()
                }

                else -> listOf()
            }
        )
    }

}
