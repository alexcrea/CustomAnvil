package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.util.logging.Level

class DebugExecutor : CASubCommand() {

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean {
        CustomAnvil.instance.logger.log(Level.SEVERE, "aaaaaaaaaaaaaaaaaaa");
        return true
    }

    override fun tabCompleter(sender: CommandSender, args: Array<out String>, list: MutableList<String>) {
        //TODO

    }

}
