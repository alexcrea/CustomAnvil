package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.gui.config.global.*
import xyz.alexcrea.cuanvil.util.MetricsUtil

class ReloadExecutor : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, cmdstr: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(CustomAnvil.commandReloadPermission)) {
            sender.sendMessage("§cYou do not have permission to reload the config")
            return false
        }
        sender.sendMessage("§eReloading config...")
        val hardfail = args.isNotEmpty() && ("hard".equals(args[0], true))
        val commandSuccess = commandBody(hardfail)
        if (commandSuccess) {
            sender.sendMessage("§aConfig reloaded !")
        } else {
            sender.sendMessage("§cConfig was not able to be reloaded...")
            if (hardfail) {
                sender.sendMessage("§4Hard fail, plugin disabled")
            }
        }
        return commandSuccess
    }

    /**
     * Execute the command, return true if success or false otherwise
     */
    private fun commandBody(hardfail: Boolean): Boolean {
        try {
            if (!ConfigHolder.reloadAllFromDisk(hardfail)) return false

            // Then update all global gui containing value from config
            BasicConfigGui.getInstance()?.updateGuiValues()
            EnchantCostConfigGui.INSTANCE.updateGuiValues()
            EnchantLimitConfigGui.INSTANCE.updateGuiValues()

            EnchantConflictGui.INSTANCE.reloadValues()
            GroupConfigGui.INSTANCE.reloadValues()
            UnitRepairConfigGui.INSTANCE.reloadValues()
            CustomRecipeConfigGui.INSTANCE.reloadValues()

            // & update metric
            MetricsUtil.testIfConfigIsDefault()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
