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
        val hardfail = args.isNotEmpty() && ("hard".equals(args[0],true))
        val commandSuccess = commandBody(hardfail)
        if(commandSuccess){
            sender.sendMessage("§aConfig reloaded !")
        }else{
            sender.sendMessage("§cConfig was not able to be reloaded...")
            if(hardfail){
                sender.sendMessage("§4Hard fail, plugin disabled")
            }
        }
        return commandSuccess
    }

    /**
     * Execute the command, return true if success or false otherwise
     */
    private fun commandBody(hardfail: Boolean): Boolean{
        try {
            return CustomAnvil.instance.reloadAllConfigs(hardfail)
        }catch (e: Exception){
            e.printStackTrace()
            return false
        }
    }
}
