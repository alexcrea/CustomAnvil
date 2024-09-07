package xyz.alexcrea.cuanvil.dependency.scheduler

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

class BukkitScheduler : TaskScheduler {

    override fun scheduleGlobally(plugin: Plugin, task: Runnable, time: Long): Any? {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, time)
    }


    override fun scheduleOnEntity(plugin: Plugin, entity: Entity, task: Runnable, time: Long): Any? {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, time)
    }
}
