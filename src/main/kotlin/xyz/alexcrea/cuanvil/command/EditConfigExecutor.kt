package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui
import xyz.alexcrea.cuanvil.gui.config.global.EnchantConfigGui
import xyz.alexcrea.cuanvil.gui.config.global.ItemConfigGui
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions
import xyz.alexcrea.cuanvil.lang.MessageLike
import xyz.alexcrea.cuanvil.lang.MsgCommand
import xyz.alexcrea.cuanvil.util.MaterialUtil.customType
import xyz.alexcrea.cuanvil.util.MaterialUtil.isAir

class EditConfigExecutor : CASubCommand {

    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.editConfigPermission)
    }

    override fun description(): MessageLike {
        return MsgCommand.CONFIG_DESCRIPTION
    }

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>,
    ): Boolean {
        if(sender !is HumanEntity) return false

        if(!allowed(sender)) {
            sender.sendMessage(GuiGlobalActions.NO_EDIT_PERM)
            return false
        }
        if(PlatformUtil.isFolia) {
            MsgCommand.CONFIG_FOLIA_ISSUE.send(sender)
            return false
        }

        if("gui".equals(cmdstr, ignoreCase = true)) {
            MsgCommand.CONFIG_LEGACY_NAME_WARNING.send(sender)
        }

        if(args.isEmpty())
            processOpen(sender)
        else when(args[0].lowercase()) {
            "open" -> processOpen(sender)
            "enchant" -> processEnchant(sender, args)
            "item" -> processItem(sender)
            else -> MsgCommand.SHARED_UNKNOWN_SUB_COMMAND.send(sender)
        }

        return true
    }

    private fun processEnchant(sender: HumanEntity, args: Array<out String>) {
        val enchantToFilter: Set<CAEnchantment>
        if(args.size <= 1) {
            val item = sender.inventory.itemInMainHand

            enchantToFilter = EnchantmentApi.getEnchantments(item).keys
            if(enchantToFilter.isEmpty()) {
                MsgCommand.CONFIG_ENCHANTMENT_NO_IN_HAND.send(sender)
                return
            }
        } else {
            enchantToFilter = HashSet(EnchantmentApi.getByName(args[1].lowercase()))

            if(enchantToFilter.isEmpty()) {
                MsgCommand.CONFIG_ENCHANTMENT_NO_NAME.send(sender, args[1])
                return
            }
        }

        EnchantConfigGui(enchantToFilter).show(sender)
    }

    private fun processItem(sender: HumanEntity) {
        val item = sender.inventory.itemInMainHand
        if(item.isAir) {
            MsgCommand.CONFIG_CANNOT_CONFIGURE_WARNING.send(sender)
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

    override fun tabCompleter(sender: CommandSender, args: Array<out String>, list: MutableList<String>) {
        list.addAll(
            when(args.size) {
                1 -> listOf("item", "enchant", "open")
                2 -> {
                    when(args[0].lowercase()) {
                        "enchant" -> allEnchantmentsByName()
                        else -> listOf()
                    }
                }

                else -> listOf()
            }
        )
    }

}
