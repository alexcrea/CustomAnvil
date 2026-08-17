package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import xyz.alexcrea.cuanvil.api.event.CAConfigReadyEvent
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.gui.config.global.*
import xyz.alexcrea.cuanvil.lang.Lang
import xyz.alexcrea.cuanvil.lang.Message
import xyz.alexcrea.cuanvil.lang.MsgCommand
import xyz.alexcrea.cuanvil.update.UpdateHandler

class ReloadExecutor : CASubCommand {

    override fun description(): Message {
        return MsgCommand.RELOAD_DESCRIPTION
    }

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>,
    ): Boolean {
        if(!allowed(sender)) {
            MsgCommand.SHARED_NO_PERMISSION.send(sender)
            return false
        }
        MsgCommand.RELOAD_START.send(sender)
        val hardfail = args.isNotEmpty() && ("hard".equals(args[0], true))
        val commandSuccess = commandBody(hardfail)
        if(commandSuccess) {
            MsgCommand.RELOAD_SUCCESS.send(sender)
        } else {
            MsgCommand.RELOAD_FAIL.send(sender)
            if(hardfail) {
                MsgCommand.RELOAD_HARD_FAIL.send(sender)
            }
        }
        return commandSuccess
    }

    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.commandReloadPermission)
    }

    override fun tabCompleter(
        sender: CommandSender,
        args: Array<out String>,
        list: MutableList<String>,
    ) {
    }

    /**
     * Execute the command, return true if success or false otherwise
     */
    private fun commandBody(hardfail: Boolean): Boolean {
        try {
            if(!ConfigHolder.reloadAllFromDisk(hardfail)) return false

            // reload language config
            if(!Lang.reload()) return false

            // Then update all global gui containing value from config
            BasicConfigGui.getInstance()?.updateGuiValues()
            EnchantCostConfigGui.getInstance()?.updateGuiValues()
            EnchantLimitConfigGui.getInstance()?.updateGuiValues()

            EnchantConflictGui.getCurrentInstance()?.reloadValues()
            GroupConfigGui.getCurrentInstance()?.reloadValues()
            UnitRepairConfigGui.getCurrentInstance()?.reloadValues()
            CustomRecipeConfigGui.getCurrentInstance()?.reloadValues()

            // handle minecraft version update
            UpdateHandler.handleMCVersionUpdate()

            // Handle dependency reload
            DependencyManager.handleConfigReload()

            // Call event
            val configReadyEvent = CAConfigReadyEvent()
            Bukkit.getServer().pluginManager.callEvent(configReadyEvent)

            return true
        } catch(e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
