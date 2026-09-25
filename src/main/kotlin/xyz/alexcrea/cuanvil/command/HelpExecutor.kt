package xyz.alexcrea.cuanvil.command

import com.google.common.collect.ImmutableMap
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import xyz.alexcrea.cuanvil.lang.Message
import xyz.alexcrea.cuanvil.lang.MsgCommand
import xyz.alexcrea.cuanvil.util.ComponentUtil.send

class HelpExecutor : CASubCommand {

    override fun description(): Message {
        return MsgCommand.HELP_DESCRIPTION
    }

    lateinit var commands: ImmutableMap<String, CASubCommand>

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>,
    ): Boolean {
        var text = MsgCommand.HELP_HEADER.formatted().first()
        for((key, cmd) in commands) {
            if(!cmd.allowed(sender)) continue

            text = text.appendNewline()
                .append(Component.text("- $key: "))
                .append(cmd.description().formatted())
        }

        text.send(sender)
        return true
    }

    override fun allowed(sender: CommandSender): Boolean {
        return true
    }

    override fun tabCompleter(
        sender: CommandSender,
        args: Array<out String>,
        list: MutableList<String>,
    ) {
    }

}