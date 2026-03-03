package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions

class EditConfigExecutor: CASubCommand() {

    override fun executeCommand(sender: CommandSender,
                                cmd: Command,
                                cmdstr: String,
                                args: Array<out String>): Boolean {
        if (sender !is HumanEntity) return false

        if (!allowed(sender)) {
            sender.sendMessage(GuiGlobalActions.NO_EDIT_PERM)
            return false
        }
        if(PlatformUtil.isFolia){
            sender.sendMessage("§cIt look like you are using Folia. Sadly Custom Anvil do not support Config gui for Folia.")
            sender.sendMessage("§eIt is may come in a future version.")
            sender.sendMessage("")
            sender.sendMessage("§eCurrently you need to edit manually the config or copy from another server (spigot or better)")
            sender.sendMessage("§eThen /anvilconfigreload after config file is edited")
            return false
        }

        MainConfigGui.getInstance().show(sender)

        return true
    }

    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.editConfigPermission)
    }

    override fun description(): String {
        return "Gui to edit the plugin's config"
    }

}
