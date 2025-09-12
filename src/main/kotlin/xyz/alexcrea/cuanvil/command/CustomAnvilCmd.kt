package xyz.alexcrea.cuanvil.command

import com.google.common.collect.ImmutableMap
import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.ArrayList
import java.util.Arrays

class CustomAnvilCmd(plugin: CustomAnvil) : CommandExecutor, TabCompleter {

    // Name of the generic command
    companion object {
        private const val genericCommandName = "customanvil"
    }

    private val editConfigCommand = EditConfigExecutor()
    private val commands: ImmutableMap<String, CASubCommand>

    init {
        commands = ImmutableMap.of<String, CASubCommand>(
            "gui", editConfigCommand,
            "reload", ReloadExecutor()
        )

        val self = plugin.getCommand(genericCommandName)!!
        self.setExecutor(this)
        self.tabCompleter = this
    }

    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean {
        // Find sub command to execute based on the provided command name
        val subcmd: CASubCommand? = if(args.isEmpty()) {
            editConfigCommand
        }else {
            commands[args[0].lowercase()]
        }

        if(subcmd == null) {
            sender.sendMessage("Invalid subcommand. run `$cmdstr help` to see available commands")
            return true
        }

        val newargs = args.copyOfRange(1, args.size)
        return subcmd.executeCommand(sender, cmd, cmdstr, newargs)
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): MutableList<String> {
        val result = ArrayList<String>()
        if(args.isEmpty()) {
            for (cmd in commands) {
                result.add(cmd.key)
            }
        } else {
            val subcmd = commands[args[0].lowercase()]
            subcmd?.tabCompleter(result)
        }

        //assumed all provided tab completed string are lowercase
        return result.stream()
            .filter { it.startsWith(args[args.size - 1]) }
            .sorted()
            .toList()
    }
}
