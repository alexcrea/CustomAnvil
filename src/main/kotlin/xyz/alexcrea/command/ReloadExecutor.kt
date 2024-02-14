package xyz.alexcrea.command

import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadExecutor : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, cmdstr: String, args: Array<out String>): Boolean {
        if(!sender.hasPermission(CustomAnvil.commandReloadPermission)) {
            sender.sendMessage("§cYou do not have permission to reload the config")
            return false
        }
        sender.sendMessage("§eReloading config...")
        val commandSuccess = commandBody()
        if(commandSuccess){
            sender.sendMessage("§aConfig reloaded !")
        }else{
            sender.sendMessage("§cConfig was not able to be reloaded...")
        }
        return commandSuccess
    }

    /**
     * Execute the command, return true if success or false otherwise
     */
    private fun commandBody(): Boolean{
        try {
            CustomAnvil.instance.reloadAllConfigs()
            return true
        }catch (e: Exception){
            e.printStackTrace()
            return false
        }
    }
}
