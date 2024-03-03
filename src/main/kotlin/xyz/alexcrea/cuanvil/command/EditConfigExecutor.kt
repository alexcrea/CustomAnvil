package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.gui.MainConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions

class EditConfigExecutor : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(!sender.hasPermission(CustomAnvil.editConfigPermission)) {
            sender.sendMessage(GuiGlobalActions.NO_EDIT_PERM)
            return false
        }
        if(sender !is HumanEntity) return false
        MainConfigGui.INSTANCE.show(sender)

        return true
    }

}
