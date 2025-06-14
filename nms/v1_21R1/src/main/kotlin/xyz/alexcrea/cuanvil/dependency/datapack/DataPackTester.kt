package xyz.alexcrea.cuanvil.dependency.datapack

import io.papermc.paper.datapack.Datapack
import org.bukkit.Bukkit

object DataPackTester {
    val enabledPacks: List<String>
        get() {
            return Bukkit.getDatapackManager().enabledPacks
                .stream().map { obj: Datapack -> obj.name }
                .toList()
        }
}
