package xyz.alexcrea.cuanvil.dependency.scheduler

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

//TODO first, add to mockbukkit theses then do next todo
//TODO replace usage of this to in code correct version
class FoliaScheduler: TaskScheduler {

    override fun scheduleGlobally(plugin: Plugin, task: Runnable, time: Long): Any {
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

    override fun scheduleOnEntity(plugin: Plugin, entity: Entity, task: Runnable, time: Long): Any? {
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

}
