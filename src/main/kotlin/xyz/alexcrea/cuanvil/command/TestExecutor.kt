package xyz.alexcrea.cuanvil.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.gui.gui.MainConfigGui

class TestExecutor : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if(sender !is HumanEntity) return false
        MainConfigGui.INSTANCE.show(sender)

        return true
    }

}
