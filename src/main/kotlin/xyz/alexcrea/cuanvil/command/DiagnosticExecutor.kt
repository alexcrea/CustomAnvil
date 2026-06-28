package xyz.alexcrea.cuanvil.command

import com.github.stefvanschie.inventoryframework.inventoryview.interface_.InventoryViewUtil
import io.delilaheve.CustomAnvil
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.packet.NoPacketManager
import xyz.alexcrea.cuanvil.dependency.packet.ProtocoLibWrapper
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener
import xyz.alexcrea.cuanvil.util.MetricsUtil
import java.util.*
import java.util.stream.Collectors


class DiagnosticExecutor: CASubCommand() {

    companion object{
        const val NO_DIAG_PERM = "You do not have permission to diagnostic this server"

        fun fetchNMSType(): String {
            val packetManager = DependencyManager.packetManager
            val packetManagerClass = packetManager.javaClass

            val className = packetManagerClass.name
            val result = if(className.contains("PaperPacket")) {
                "Paper"
            } else {
                when (packetManagerClass) {
                    ProtocoLibWrapper::class.java -> "Protocolib"
                    NoPacketManager::class.java -> "None"
                    else -> "Version Specific"
                }
            }


            return "$result ${if(packetManager.canSetInstantBuild) '✅' else '❌'}"
        }
    }

    enum class DiagParams(val value: String) {
        OS_PRIVACY("os_privacy"),
        PLUGIN_PRIVACY("plugin_privacy"),
        NO_MERGE_TEST("no_merge_test"),
        FULL_ENCHANTMENT_DATA("full_enchantment_data"),
        INCLUDE_LAST_ERROR("include_last_error"),
    }

    private fun fetchParameters(args: Array<out String>): EnumSet<DiagParams> {
        val result = EnumSet.noneOf(DiagParams::class.java)
        val argSet = HashSet<String>()

        for (string in args) {
            argSet.add(string.lowercase())
        }

        for (param in DiagParams.entries) {
            if(argSet.contains(param.value))
                result.add(param)
        }

        return result
    }

    override fun tabCompleter(
        sender: CommandSender,
        args: Array<out String>,
        list: MutableList<String>) {
        if(!allowed(sender)) return

        val map = fetchParameters(args)
        for (param in DiagParams.entries) {
            if(!map.contains(param))
                list.add(param.value)
        }

    }

    override fun executeCommand(
        sender: CommandSender,
        cmd: Command,
        cmdstr: String,
        args: Array<out String>
    ): Boolean {
        if (!allowed(sender)) {
            sender.sendMessage(NO_DIAG_PERM)
            return false
        }

        val stb = StringBuilder("```\n")
        val params = fetchParameters(args)
        var hasError = false
        try {
            diagnostic(sender, stb, params)
        } catch(e: Throwable){
            stb.append("\n\nError happened trying to get diagnostic data:\n")
                .append(e.message).append("\n")
                .append(e.stackTrace.joinToString("\n"))
            hasError = true
            e.printStackTrace()
        }

        stb.append("\n```")

        if (sender is HumanEntity) {
            if(hasError)
                sender.spigot().sendMessage(TextComponent(ChatColor.RED.toString() + "There was an error running the diagnostic"))
            val message = TextComponent(ChatColor.GREEN.toString() + "Click to copy diagnostic data")

            message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, stb.toString())
            message.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("§7Click to copy"))

            sender.spigot().sendMessage(message);
        } else {
            sender.sendMessage(stb.toString());
        }

        return true
    }

    override fun allowed(sender: CommandSender): Boolean {
        return sender.hasPermission(CustomAnvil.diagnosticPermission)
    }

    fun diagnostic(sender: CommandSender, stb: StringBuilder, params: Set<DiagParams>){
        stb.append("Server Info\n")
        val version = CustomAnvil.instance.description.version
        stb.append("\nPlugin Version: ").append(version)
        if(version.contains("dev")) stb.append(" (alpha)")

        stb.append("\nLatest Update: ").append(CustomAnvil.latestVer)
        stb.append("\nServer Version: ").append(Bukkit.getVersion()).append(" (").append(Bukkit.getName()).append(')')
        stb.append("\nPlugin Enabled: ").append(if(CustomAnvil.instance.isEnabled) "Yes" else "No")
        stb.append("\nNMS type: ").append(fetchNMSType())
        if(!params.contains(DiagParams.OS_PRIVACY)) {
            stb.append("\nJava Version: ").append(System.getProperty("java.version"))
            stb.append("\nOS: ").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version"))
                .append(System.getProperty("os.arch"))
        }

        stb.append("\nHad detect error: ").append(if(MetricsUtil.lastError != null) "Yes" else "No")

        if(!params.contains(DiagParams.PLUGIN_PRIVACY)) {
            pluginListDiag(sender, stb)
        }
        prepareAnvilListeners(stb)

        if(!params.contains(DiagParams.NO_MERGE_TEST)){
            if(sender is Player) testMerge(sender, stb)
        }

        stb.append("\n\nEnchantments data:")
        partialEnchantmentData(stb)
        if(params.contains(DiagParams.FULL_ENCHANTMENT_DATA)){
            fullEnchantmentData(stb)
        }

        if(params.contains(DiagParams.INCLUDE_LAST_ERROR)){
            includeLastError(stb)
        }
    }

    private fun testMerge(player: Player, stb: StringBuilder) {
        val sword = ItemStack(Material.DIAMOND_SWORD)
        val damagedSword = sword.clone()
        val enchantedSword = sword.clone()
        val enchantedBook = ItemStack(Material.ENCHANTED_BOOK)
        val unitForRepair = ItemStack(Material.DIAMOND)

        var meta = damagedSword.itemMeta
        (meta as Damageable).damage = 5
        damagedSword.itemMeta = meta

        meta = enchantedSword.itemMeta
        meta!!.addEnchant(Enchantment.DAMAGE_ALL, 1, true)
        enchantedSword.itemMeta = meta

        meta = enchantedBook.itemMeta
        (meta as EnchantmentStorageMeta).addStoredEnchant(Enchantment.DAMAGE_ALL, 1, true)
        enchantedBook.itemMeta = meta

        stb.append("\n\nItem to Item repair:")
        simulateAnvil(player, stb, damagedSword, damagedSword, sword)

        stb.append("\n\nUnit repair:")
        simulateAnvil(player, stb, damagedSword, unitForRepair, sword)

        stb.append("\n\nEnchanting an item:")
        simulateAnvil(player, stb, sword, enchantedBook, enchantedSword)
    }

    private val Plugin.pluginNameDisplay: String
        get() {
            return this.name + " v" + this.description.version
        }

    override fun description(): String {
        return "Basic diagnostic of this plugin"
    }

    private fun pluginListDiag(sender: CommandSender, stb: StringBuilder) {
        val enabledPlugins: MutableList<Plugin?> = ArrayList<Plugin?>()
        val disabledPlugins: MutableList<Plugin?> = ArrayList<Plugin?>()
        for (plugin in Bukkit.getPluginManager().plugins) {
            if (plugin.isEnabled) {
                enabledPlugins.add(plugin)
            } else {
                disabledPlugins.add(plugin)
            }
        }

        stb.append("\nEnabled Plugins: ").append(
            enabledPlugins.stream()
                .map { plugin -> plugin!!.pluginNameDisplay }
                .reduce { a: String?, b: String? -> "$a, $b" }.orElse("None")
        )

        stb.append("\nDisabled Plugins: ").append(
            disabledPlugins.stream()
                .map { plugin -> plugin!!.pluginNameDisplay }
                .reduce { a: String?, b: String? -> "$a, $b" }.orElse("None")
        )
    }

    fun prepareAnvilListeners(stb: StringBuilder) {
        val eventListeners: MutableSet<Plugin?> = Arrays
            .stream(
                PrepareAnvilEvent
                    .getHandlerList()
                    .getRegisteredListeners()
            )
            .map { obj: RegisteredListener? -> obj!!.plugin }
            .collect(Collectors.toSet())

        eventListeners.remove(CustomAnvil.instance)
        stb.append("\nPrepare Anvil Listeners: ").append(
            if (eventListeners.isEmpty()) "None" else eventListeners.stream()
                .map { plugin -> plugin!!.pluginNameDisplay }
                .reduce { a: String?, b: String? -> "$a, $b" }.orElse("None")
        )
    }

    fun simulateAnvil(player: Player, stb: StringBuilder, left: ItemStack?, right: ItemStack?, result: ItemStack?) {
        var invView: InventoryView
        var event: PrepareAnvilEvent
        try {
            val fakeInv = Bukkit.createInventory(player, InventoryType.ANVIL)
            invView = player.openInventory(fakeInv)!!
            event = PrepareAnvilEvent(invView, result)
        } catch (e: Throwable) {
            // Help
            val menuTypeClazz = Class.forName("org.bukkit.inventory.MenuType")
            val anvilTypeField = menuTypeClazz.getField("ANVIL")
            val anvilType = anvilTypeField.get(null)
            val createMethod = anvilType.javaClass.getMethod("create", HumanEntity::class.java)
            invView = createMethod.invoke(anvilType, player) as InventoryView

            player.openInventory(invView)

            val anvilViewClass = Class.forName("org.bukkit.inventory.view.AnvilView")
            val constructor = PrepareAnvilEvent::class.java.getConstructor(anvilViewClass, ItemStack::class.java)
            event = constructor.newInstance(invView, result)
        }

        val fakeInv = InventoryViewUtil.getInstance().getTopInventory(invView) as AnvilInventory
        fakeInv.setItem(0, left)
        fakeInv.setItem(1, right)

        val xp = fakeInv.repairCost
        val maxXp = fakeInv.maximumRepairCost
        val mergeResult = fakeInv.getItem(2)
        stb.append("\n${if(result == mergeResult) "E" else "Une"}xpected Result")

        PrepareAnvilListener().anvilCombineCheck(event)
        // Now we check if item and xp same
        stb.append("\nXP/Max XP: ")
            .append(if(fakeInv.repairCost == xp) "Correct" else "Incorrect")
            .append("/")
            .append(if(fakeInv.maximumRepairCost == maxXp) "Correct" else "Incorrect")
            .append(" (${fakeInv.repairCost} $xp|${fakeInv.maximumRepairCost} $maxXp)")
            .append("\nMerge result: ")
            .append(if(fakeInv.getItem(2) == mergeResult) "Correct" else "Incorrect")

        PrepareAnvilListener.IS_EMPTY_TEST = true
        Bukkit.getPluginManager().callEvent(event)
        stb.append("\nNull result test: ")
            .append(if(event.result == null) "Correct" else "Incorrect")

        fakeInv.setItem(0, null)
        fakeInv.setItem(1, null)
        fakeInv.setItem(2, null)
        player.closeInventory()
    }

    private fun fullEnchantmentData(stb: StringBuilder) {
        for (enchantment in CAEnchantmentRegistry.getInstance().values()) {
            stb.append("\n- ").append(enchantment.key.toString())
                .append(" ").append(enchantment.name)
                .append(" ").append(enchantment.defaultMaxLevel())
        }
    }

    private fun partialEnchantmentData(stb: StringBuilder) {
        val map = HashMap<String, Int>()
        for (enchant in CAEnchantmentRegistry.getInstance().values()) {
            map[enchant.key.namespace] = map.getOrDefault(enchant.key.namespace, 0) + 1
        }

        stb.append("\nNamespaces: ${
            map.entries.stream()
                .map { (key, value) -> "$key ($value)" }
                .reduce { a, b -> "$a, $b" }.get()
        }")

    }

    private fun includeLastError(stb: StringBuilder) {
        val e = MetricsUtil.lastError ?: return

        stb.append("\n\nLast stack trace: ${e.stackTraceToString()}")



    }
    
}