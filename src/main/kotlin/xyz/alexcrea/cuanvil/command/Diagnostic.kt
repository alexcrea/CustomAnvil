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

class Diagnostic: CASubCommand() {

    companion object{
        private const val NO_DIAG_PERM = "You do not have permission to diagnostic this server"
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
        try {
            diagnostic(stb)
        } catch(e: Exception){
            // TODO append error message to diag
            TODO("error not handled yet $e")
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

    fun diagnostic(stb: StringBuilder){
        stb.append("Server Info\n");
        stb.append("Plugin Version: ").append(CustomAnvil.instance.description.version).append("\n");
        stb.append("Server Version: ").append(Bukkit.getVersion()).append(" (").append(Bukkit.getName()).append(')').append("\n");
        stb.append("Plugin Enabled: ").append(if(CustomAnvil.instance.isEnabled) "Yes" else "No").append("\n");
        //stb.append("NMS type: ").append(NMSMapper.hasNMS() ? "Yes" : "No").append("\n");
        stb.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        stb.append("OS: ").append(System.getProperty("os.name")).append(" ")
                            .append(System.getProperty("os.version"))
                            .append(System.getProperty("os.arch"))
                            .append("\n\n");
        stb.append("Architecture: ").append(System.getProperty("os.arch")).append("\n\n");



    }


}