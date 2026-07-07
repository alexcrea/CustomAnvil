package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui
import xyz.alexcrea.cuanvil.gui.config.global.ItemConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions
import xyz.alexcrea.cuanvil.util.MaterialUtil.customType
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir

class EditConfigExecutor : CASubCommand() {

    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.editConfigPermission)
    }

    override fun description(): String {
        return "Gui to edit the plugin's config"
    }

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is HumanEntity) return false

        if (!allowed(sender)) {
            sender.sendMessage(GuiGlobalActions.NO_EDIT_PERM)
            return false
        }
        if (PlatformUtil.isFolia) {
            sender.sendMessage("§cIt look like you are using Folia. Sadly Custom Anvil do not support Config gui for Folia.")
            sender.sendMessage("§eIt is may come in a future version.")
            sender.sendMessage("")
            sender.sendMessage("§eCurrently you need to edit manually the config or copy from another server (spigot or better)")
            sender.sendMessage("§eThen /ca reload after config file is edited")
            return false
        }

        if ("gui".equals(cmdstr, ignoreCase = true)) {
            sender.sendMessage("§c/ca gui has been moved to /ca config")
        }

        if (args.isEmpty())
            processOpen(sender)
        else when (args[0].lowercase()) {
            "open" -> processOpen(sender)
            "enchant" -> processEnchant(sender, args)
            "item" -> processItem(sender)
            else -> sender.sendMessage("Unknown subcommand \"${args[0]}\"")
        }

        return true
    }

    private fun processEnchant(sender: HumanEntity, args: Array<out String>) {
        val enchantToFilter: Set<CAEnchantment>
        if (args.size <= 1) {
            val item = sender.inventory.itemInMainHand

            enchantToFilter = EnchantmentApi.getEnchantments(item).keys
            if (enchantToFilter.isEmpty()) {
                sender.sendMessage("No enchantment found in the item you are holding")
                return
            }
        } else {
            enchantToFilter = HashSet(EnchantmentApi.getListByName(args[1].lowercase()))

            if (enchantToFilter.isEmpty()) {
                sender.sendMessage("No enchantment found with the name \"${args[1]}\"")
                return
            }
        }

        // TODO open enchantments edit gui

    }

    private fun processItem(sender: HumanEntity) {
        val item = sender.inventory.itemInMainHand
        if (item.isAir) {
            sender.sendMessage("Cannot configure the item in hand")
            return
        }

        ItemConfigGui(item.type, item.customType).show(sender)
    }

    private fun processOpen(sender: HumanEntity) {
        MainConfigGui.getInstance().show(sender)
    }

    // ---------------
    //  Tab completer
    // ---------------

    private fun allEnchantmentsByName(): Collection<String> {
        val names = mutableSetOf<String>()
        for (enchantment in CAEnchantmentRegistry.getInstance().values()) {
            names.add(enchantment.name)
            names.add(enchantment.key.toString())
        }

        return names
    }

    override fun tabCompleter(sender: CommandSender, args: Array<out String>, list: MutableList<String>) {
        list.addAll(
            when (args.size) {
                1 -> listOf("item", "enchant", "open")
                2 -> {
                    when (args[0].lowercase()) {
                        "enchant" -> allEnchantmentsByName()
                        else -> listOf()
                    }
                }

                else -> listOf()
            }
        )

    }

}
