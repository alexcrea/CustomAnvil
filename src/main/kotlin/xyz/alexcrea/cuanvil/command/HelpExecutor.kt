package xyz.alexcrea.cuanvil.command

import com.google.common.collect.ImmutableMap
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class HelpExecutor: CASubCommand() {

    lateinit var commands: ImmutableMap<String, CASubCommand>

    override fun executeCommand(sender: CommandSender,
                                cmd: Command,
                                cmdstr: String,
                                args: Array<out String>): Boolean {

        val stb = StringBuilder("List of available commands:")
        for ((key, cmd) in commands) {
            if(!cmd.allowed(sender)) continue

            stb.append("\n- $key: ").append(cmd.description())
        }

        sender.sendMessage(stb.toString())

        return true
    }

    override fun description(): String {
        return "Help command"
    }

}