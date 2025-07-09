package xyz.alexcrea.cuanvil.dependency.scheduler

import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

interface TaskScheduler {

    fun scheduleGlobally(plugin: Plugin, task: Runnable, time: Long): Any?
    fun scheduleGlobally(plugin: Plugin, task: Runnable): Any?{
        return scheduleGlobally(plugin, task, 0L)
    }

    fun scheduleOnEntity(plugin: Plugin, entity: Entity, task: Runnable, time: Long): Any?
    fun scheduleOnEntity(plugin: Plugin, entity: Entity, task: Runnable): Any?{
        return scheduleOnEntity(plugin, entity, task, 0L)
    }

}