package xyz.alexcrea.cuanvil.dependency.plugins

import com.maddoxh.superEnchants.SuperEnchants
import com.maddoxh.superEnchants.enchants.EnchantManager
import com.maddoxh.superEnchants.listeners.AnvilListener
import io.delilaheve.CustomAnvil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.RegisteredListener
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.enchant.wrapped.CASuperEnchantEnchantment
import java.util.logging.Level

class SuperEnchantDependency(override val plugin: SuperEnchants): GenericPluginDependency(plugin, false) {

    lateinit var enchManager: EnchantManager
    val enchantments = ArrayList<CASuperEnchantEnchantment>()

    fun registerEnchantments(): Boolean{
        CustomAnvil.instance.logger.info("Preparing Super Enchant compatibility...")

        val field = SuperEnchants::class.java.getDeclaredField("enchantManager")
        if(field == null) {
            CustomAnvil.instance.logger.log(Level.SEVERE, "Failed to initialize Super Enchant compatibility")
            return false
        }
        field.setAccessible(true)

        enchManager = field.get(plugin) as EnchantManager
        overrideReloadCommand()

        reload()
        return true
    }

    fun reload() {
        for (enchantment in enchantments) {
            EnchantmentApi.unregisterEnchantment(enchantment)
        }
        enchantments.clear()

        // Register enchantments
        for (enchant in enchManager.getAll()) {
            val enchantment = CASuperEnchantEnchantment(enchant, plugin, enchManager)
            enchantments.add(enchantment)

            EnchantmentApi.registerEnchantment(enchantment)
        }
    }

    private fun overrideReloadCommand() {
        val reload = CustomAnvil.instance.getCommand("sereload")

        reload?.setExecutor(ReloadInterceptor(reload.executor))
    }

    inner class ReloadInterceptor(val other: CommandExecutor): CommandExecutor {

        override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String?>
        ): Boolean {
            val result = other.onCommand(sender, command, label, args)

            CustomAnvil.log("Detected SuperEnchant reload")
            reload()

            return result
        }

    }

    override fun fillPostAnvil(postAnvil: ArrayList<RegisteredListener>, preAnvil: ArrayList<RegisteredListener>) {

        for (registeredListener in InventoryClickEvent.getHandlerList().registeredListeners) {

            if (registeredListener.listener.javaClass != AnvilListener::class.java) continue
            postAnvil.add(registeredListener)
        }
    }

}
