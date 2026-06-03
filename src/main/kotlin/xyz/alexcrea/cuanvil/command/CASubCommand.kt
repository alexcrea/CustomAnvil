package xyz.alexcrea.cuanvil.command

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry

abstract class CASubCommand : CommandExecutor {

    private var alreadySaid = false;
    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean {
        if (!alreadySaid) {
            sender.sendMessage(
                ChatColor.RED.toString() +
                        "Please not that this command will be replaced as a subcommand of `/customanvil` or `/ca`"
            )
            alreadySaid = true
        }

        return executeCommand(sender, cmd, cmdstr, args)
    }

    abstract fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean

    open fun allowed(sender: CommandSender): Boolean {
        return true
    }

    open fun tabCompleter(
        sender: CommandSender,
        args: Array<out String>,
        list: MutableList<String>
    ) {
    }

    open fun description(): String {
        return "no description"
    }

    protected fun allEnchantmentsByName(): Collection<String> {
        val names = mutableSetOf<String>()
        for (enchantment in CAEnchantmentRegistry.getInstance().values()) {
            names.add(enchantment.name)
            names.add(enchantment.key.toString())
        }

        return names
    }

}