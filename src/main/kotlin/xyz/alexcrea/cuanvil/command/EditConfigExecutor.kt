package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions

class EditConfigExecutor : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(CustomAnvil.editConfigPermission)) {
            sender.sendMessage(GuiGlobalActions.NO_EDIT_PERM)
            return false
        }
        if(DependencyManager.isFolia){
            sender.sendMessage("§cIt look like you are using Folia. Sadly Custom Anvil do not support Config gui for Folia.")
            sender.sendMessage("§eIt is may come in a future version.")
            sender.sendMessage("")
            sender.sendMessage("§eCurrently you need to edit manually the config or copy from another server (spigot or better)")
            sender.sendMessage("§eThen /anvilconfigreload after config file is edited")
            return false
        }

        if (sender !is HumanEntity) return false
        MainConfigGui.getInstance().show(sender)

        return true
    }

}
