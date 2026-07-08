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
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir

class EnchantExecutor : CASubCommand {


    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.giveEnchantmentPermission)
    }

    override fun description(): String {
        return "Allows to set enchantment to holden item"
    }

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is HumanEntity) return true

        if (!allowed(sender)) {
            sender.sendMessage("No permission to execute this command")
            return true
        }

        when (args.size) {
            0 -> {
                sender.sendMessage("Missing enchantment parameter")
                return true
            }
        }

        val enchant = firstEnchantment(args[0])
        if (enchant == null) {
            sender.sendMessage("Enchantment not found: ${args[0]}")
            return true
        }

        val level = if (args.size > 1)
            args[1].toIntOrNull()?.coerceIn(0, ConfigOptions.ENCHANT_LIMIT)
        else 1

        if (level == null) {
            sender.sendMessage("Invalid number: ${args[1]}")
            return true
        }

        val inHand = sender.inventory.itemInMainHand
        if (inHand.isAir) {
            sender.sendMessage("Cannot enchant this item")
            return true
        }

        if (level == 0) {
            enchant.removeFrom(inHand)

            if (inHand.isEnchantedBook() && EnchantmentApi.getEnchantments(inHand).isEmpty())
                inHand.type = Material.BOOK

            sender.sendMessage("${enchant.prettyName} removed")
        } else {
            if (Material.BOOK == inHand.type)
                inHand.type = Material.ENCHANTED_BOOK

            enchant.addEnchantmentUnsafe(inHand, level)
            sender.sendMessage("${enchant.prettyName} set to level $level")
        }

        return true
    }

    override fun tabCompleter(sender: CommandSender, args: Array<out String>, list: MutableList<String>) {
        if (sender !is HumanEntity) return

        list.addAll(
            when (args.size) {
                1 -> allEnchantmentsByName()
                2 -> findEnchantmentLevels(sender, args[0])

                else -> listOf()
            }
        )
    }

    private fun findEnchantmentLevels(
        sender: HumanEntity,
        name: String
    ): Collection<String> {
        val enchant = firstEnchantment(name) ?: return listOf()

        val limit = ConfigOptions.enchantLimit(enchant)
        val result = mutableListOf<String>()

        for (i in 1 until limit + 1) {
            result.add(i.toString())
        }

        val inHand = sender.inventory.itemInMainHand
        if (enchant.isEnchantmentPresent(inHand))
            result.add("0")

        return result
    }

    private fun firstEnchantment(name: String): CAEnchantment? {
        val enchants = EnchantmentApi.getListByName(name.lowercase())
        if (!enchants.isEmpty())
            return enchants.iterator().next()

        return EnchantmentApi.getByKey(NamespacedKey.fromString(name))
    }

}
