package xyz.alexcrea.cuanvil.dependency.scheduler

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

//TODO replace usage of this to in code correct version
class FoliaScheduler {

    fun scheduleGlobally(plugin: Plugin, task: Runnable, time: Long): Any {
        if (time < 1) {
            return Bukkit.getGlobalRegionScheduler().run(
                plugin
            ) { scheduledTask: ScheduledTask? -> task.run() }
        }
        return Bukkit.getGlobalRegionScheduler().runDelayed(
            plugin,
            { scheduledTask: ScheduledTask? -> task.run() },
            time
        )
    }

    fun scheduleGlobally(plugin: Plugin, task: Runnable): Any?{
        return scheduleGlobally(plugin, task, 0L)
    }

    fun scheduleOnEntity(plugin: Plugin, entity: Entity, task: Runnable, time: Long): Any? {
        if (time < 1) {
            return entity.scheduler.run(
                plugin,
                { scheduledTask: ScheduledTask? -> task.run() },
                {}
            )
        }
        return entity.scheduler.runDelayed(
            plugin,
            { scheduledTask: ScheduledTask? -> task.run() },
            {},
            time
        )
    }

    fun scheduleOnEntity(plugin: Plugin, entity: Entity, task: Runnable): Any?{
        return scheduleOnEntity(plugin, entity, task, 0L)
    }
}
