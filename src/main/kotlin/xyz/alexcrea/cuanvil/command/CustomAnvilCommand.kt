package xyz.alexcrea.cuanvil.command

import com.google.common.collect.ImmutableMap
import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import xyz.alexcrea.cuanvil.lang.MsgCommand

class CustomAnvilCommand(plugin: CustomAnvil) : CommandExecutor, TabCompleter {

    // Name of the generic command
    companion object {
        private const val genericCommandName = "customanvil"
    }

    private val editConfigCommand = EditConfigExecutor()
    private val helpCommand = HelpExecutor()
    private val commands: ImmutableMap<String, CASubCommand> = ImmutableMap.of(
        "gui", editConfigCommand,
        "config", editConfigCommand,
        "reload", ReloadExecutor(),
        "diagnostic", DiagnosticExecutor(),
        "debug", DebugToggleExecutor(),
        "help", helpCommand,
        "enchant", EnchantExecutor(),
    )

    init {
        val self = plugin.getCommand(genericCommandName)!!
        self.setExecutor(this)
        self.tabCompleter = this

        helpCommand.commands = commands
    }

    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean {
        // Find sub command to execute based on the provided command name
        val subcmd: CASubCommand?
        val subcmdStr: String

        val newargs: Array<out String>
        if (args.isEmpty()) {
            subcmdStr = "config"
            subcmd = editConfigCommand
            newargs = args
        } else {
            subcmdStr = args[0].lowercase()
            subcmd = commands[subcmdStr]
            newargs = args.copyOfRange(1, args.size)
        }

        if (subcmd == null || !subcmd.allowed(sender)) {
            MsgCommand.ROOT_UNKNOWN_SUBCOMMAND.send(sender, cmdstr)
            return true
        }

        try {
            return subcmd.executeCommand(sender, cmd, subcmdStr, newargs)
        } catch (e: Throwable) {
            CustomAnvil.logError("Error running /$cmdstr ${args.joinToString(" ")}", e)
            MsgCommand.ROOT_ERROR_SUBCOMMAND.send(sender)
            return false
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): MutableList<String> {
        val result = ArrayList<String>()
        if (args.size < 2) {
            for ((key, cmd) in commands) {
                if (!cmd.allowed(sender)) continue
                if("gui".contentEquals(key)) continue
                 result.add(key)
            }
        } else {
            val subcmd = commands[args[0].lowercase()]

            if (subcmd != null) {
                val newArgs = args.copyOfRange(1, args.size)
                if (!subcmd.allowed(sender)) return result

                subcmd.tabCompleter(sender, newArgs, result)
            }
        }

        //assumed all provided tab completed string are lowercase
        val prefix = args[args.size - 1]
        return result.stream()
            .filter { it.startsWith(prefix) }
            .sorted()
            .toList()
    }

}
