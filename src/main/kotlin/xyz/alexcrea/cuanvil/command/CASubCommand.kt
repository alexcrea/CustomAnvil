package xyz.alexcrea.cuanvil.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.lang.MessageLike

interface CASubCommand {

    fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean

    fun allowed(sender: CommandSender): Boolean

    fun tabCompleter(
        sender: CommandSender,
        args: Array<out String>,
        list: MutableList<String>
    )

    fun description(): MessageLike

    fun allEnchantmentsByName(): Collection<String> {
        val names = mutableSetOf<String>()
        for (enchantment in CAEnchantmentRegistry.getInstance().values()) {
            names.add(enchantment.name)
            names.add(enchantment.key.toString())
        }

        return names
    }

}