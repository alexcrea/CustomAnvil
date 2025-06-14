package xyz.alexcrea.cuanvil.listener

import io.delilaheve.CustomAnvil
import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import xyz.alexcrea.cuanvil.dependency.DependencyManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

class ChatEventListener : Listener {

    private val playerListenMap: ConcurrentHashMap<UUID, Consumer<String?>> = ConcurrentHashMap()

    private fun setListenedCallback(playeruuid: UUID, callback: Consumer<String?>) {
        playerListenMap[playeruuid] = callback
    }

    fun setListenedCallback(player: HumanEntity, callback: Consumer<String?>) {
        setListenedCallback(player.uniqueId, callback)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val eventCallback = playerListenMap.remove(event.player.uniqueId) ?: return
        eventCallback.accept(null)

    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        if (event.isCancelled) return
        val player = event.player
        val eventCallback = playerListenMap.remove(player.uniqueId) ?: return

        event.isCancelled = true

        // sync callback with default server thread
        DependencyManager.scheduler.scheduleOnEntity(
            CustomAnvil.instance, player,
            {
                eventCallback.accept(event.message)
            }, 0L
        )
    }

}