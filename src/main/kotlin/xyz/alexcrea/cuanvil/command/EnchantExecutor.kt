package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import io.delilaheve.util.ItemUtil.isEnchantedBook
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.lang.Message
import xyz.alexcrea.cuanvil.lang.MsgCommand
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir

class EnchantExecutor : CASubCommand {


    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.giveEnchantmentPermission)
    }

    override fun description(): Message {
        return MsgCommand.ENCHANT_DESCRIPTION
    }

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>,
    ): Boolean {
        if(sender !is HumanEntity) return true

        if(!allowed(sender)) {
            MsgCommand.SHARED_NO_PERMISSION.send(sender)
            return true
        }

        when(args.size) {
            0 -> {
                MsgCommand.ENCHANT_MISSING_PARAMETER_WARNING.send(sender)
                return true
            }
        }

        val enchant = firstEnchantment(args[0])
        if(enchant == null) {
            MsgCommand.ENCHANT_NOT_FOUND_WARNING.send(sender, args[0])
            return true
        }

        val level = if(args.size > 1)
            args[1].toIntOrNull()?.coerceIn(0, ConfigOptions.ENCHANT_LIMIT)
        else 1

        if(level == null) {
            MsgCommand.ENCHANT_MALFORMED_NUMBER_WARNING.send(sender, args[1])
            return true
        }

        val inHand = sender.inventory.itemInMainHand
        if(inHand.isAir) {
            MsgCommand.ENCHANT_CANNOT_ENCHANT_WARNING.send(sender)
            return true
        }

        if(level == 0) {
            enchant.removeFrom(inHand)

            if(inHand.isEnchantedBook() && EnchantmentApi.getEnchantments(inHand).isEmpty())
                inHand.type = Material.BOOK

            MsgCommand.ENCHANT_REMOVE.send(sender, enchant.prettyName)
        } else {
            if(Material.BOOK == inHand.type)
                inHand.type = Material.ENCHANTED_BOOK

            enchant.addEnchantmentUnsafe(inHand, level)
            MsgCommand.ENCHANT_SET.send(sender, enchant.prettyName, level)
        }

        return true
    }

    override fun tabCompleter(sender: CommandSender, args: Array<out String>, list: MutableList<String>) {
        if(sender !is HumanEntity) return

        list.addAll(
            when(args.size) {
                1 -> allEnchantmentsByName()
                2 -> findEnchantmentLevels(sender, args[0])

                else -> listOf()
            }
        )
    }

    private fun findEnchantmentLevels(
        sender: HumanEntity,
        name: String,
    ): Collection<String> {
        val enchant = firstEnchantment(name) ?: return listOf()

        val limit = ConfigOptions.enchantLimit(enchant)
        val result = mutableListOf<String>()

        for(i in 1 until limit + 1) {
            result.add(i.toString())
        }

        val inHand = sender.inventory.itemInMainHand
        if(enchant.isEnchantmentPresent(inHand))
            result.add("0")

        return result
    }

    private fun firstEnchantment(name: String): CAEnchantment? {
        val enchants = EnchantmentApi.getByName(name.lowercase())
        if(!enchants.isEmpty())
            return enchants.iterator().next()

        return EnchantmentApi.getByKey(NamespacedKey.fromString(name))
    }

}
