package xyz.alexcrea.cuanvil.dependency.scheduler

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.function.Consumer

class FoliaScheduler : TaskScheduler {
    override fun scheduleGlobally(plugin: Plugin, task: Runnable, time: Long): Any? {
        if(time < 1){
            return Bukkit.getGlobalRegionScheduler().run(
                plugin,
                Consumer { scheduledTask: ScheduledTask? -> task.run() }
            )
        }
        return Bukkit.getGlobalRegionScheduler().runDelayed(
            plugin,
            Consumer { scheduledTask: ScheduledTask? -> task.run() },
            time
        )
    }


    override fun scheduleOnEntity(plugin: Plugin, entity: Entity, task: Runnable, time: Long): Any? {
        if(time < 1){
            return entity.scheduler.run(
                plugin,
                Consumer { scheduledTask: ScheduledTask? -> task.run() },
                Runnable {}
            )
        }
        return entity.scheduler.runDelayed(
            plugin,
            Consumer { scheduledTask: ScheduledTask? -> task.run() },
            Runnable {},
            time
        )
    }

    companion object {
        fun isFolia(): Boolean {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
                return true
            } catch (e: ClassNotFoundException) {
                return false
            }
        }
    }
    
}
