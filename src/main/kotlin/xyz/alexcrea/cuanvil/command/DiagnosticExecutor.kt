package xyz.alexcrea.cuanvil.command

import io.delilaheve.CustomAnvil
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import xyz.alexcrea.cuanvil.dependency.packet.NoPacketManager
import xyz.alexcrea.cuanvil.dependency.packet.ProtocoLibWrapper
import xyz.alexcrea.cuanvil.dependency.packet.versions.PaperPacketManager
import java.util.*
import java.util.stream.Collectors


class DiagnosticExecutor: CASubCommand() {

    companion object{
        private const val NO_DIAG_PERM = "You do not have permission to diagnostic this server"
    }

    enum class DiagParams(val value: String) {
        OS_PRIVACY("os_privacy"),
        PLUGIN_PRIVACY("plugin_privacy"),
        //NO_TEST("no_anvil_test"),
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
        try {
            diagnostic(stb, params)
        } catch(e: Exception){
            stb.append("\n\nError happened trying to get diagnostic data:\n")
                .append(e.message).append("\n")
                .append(e.stackTrace.joinToString("\n"))
        }

        stb.append("\n```")

        if (sender is HumanEntity) {
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

    fun diagnostic(stb: StringBuilder, params: Set<DiagParams>){
        stb.append("Server Info\n")
        stb.append("Plugin Version: ").append(CustomAnvil.instance.description.version).append("\n")
        stb.append("Latest Update: ").append(CustomAnvil.latestVer).append('\n')
        stb.append("Server Version: ").append(Bukkit.getVersion()).append(" (").append(Bukkit.getName()).append(')').append("\n")
        stb.append("Plugin Enabled: ").append(if(CustomAnvil.instance.isEnabled) "Yes" else "No").append("\n")
        stb.append("NMS type: ").append(fetchNMSType())
        if(!params.contains(DiagParams.OS_PRIVACY)) {
            stb.append("Java Version: ").append(System.getProperty("java.version")).append("\n")
            stb.append("OS: ").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version"))
                .append(System.getProperty("os.arch"))
                .append("\n\n")
        }

        if(!params.contains(DiagParams.PLUGIN_PRIVACY)) {
            pluginListDiag(stb)
        }
        prepareAnvilListeners(stb)

        stb.append("\n\n")
    }

    private fun fetchNMSType(): String {
        val packetManager = DependencyManager.packetManager
        val packetManagerClass = packetManager.javaClass

        val result: String
        if(packetManagerClass == PaperPacketManager::class.java) {
            result = "Paper NMS"
        } else if(packetManagerClass == ProtocoLibWrapper::class.java) {
            result = "Protocolib"
        } else if(packetManagerClass == NoPacketManager::class.java) {
            result = "None"
        } else {
            result = "Version Specific"
        }

        return "$result ${if(packetManager.canSetInstantBuild) '✅' else '❌'}"
    }

    private val Plugin.pluginNameDisplay: String
        get() {
            return this.name + " v" + this.description.version
        }

    private fun pluginListDiag(stb: StringBuilder) {
        val enabledPlugins: MutableList<Plugin?> = ArrayList<Plugin?>()
        val disabledPlugins: MutableList<Plugin?> = ArrayList<Plugin?>()
        for (plugin in Bukkit.getPluginManager().plugins) {
            if (plugin.isEnabled) {
                enabledPlugins.add(plugin)
            } else {
                disabledPlugins.add(plugin)
            }
        }

        stb.append("Enabled Plugins: ").append(
            enabledPlugins.stream()
                .map { plugin -> plugin!!.pluginNameDisplay }
                .reduce { a: String?, b: String? -> "$a, $b" }.orElse("None")
        ).append("\n")

        stb.append("Disabled Plugins: ").append(
            disabledPlugins.stream()
                .map { plugin -> plugin!!.pluginNameDisplay }
                .reduce { a: String?, b: String? -> "$a, $b" }.orElse("None")
        ).append("\n")
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
        stb.append("Prepare Anvil Listeners: ").append(
            if (eventListeners.isEmpty()) "None" else eventListeners.stream()
                .map { plugin -> plugin!!.pluginNameDisplay }
                .reduce { a: String?, b: String? -> "$a, $b" }.orElse("None")
        ).append("\n\n")
    }

}