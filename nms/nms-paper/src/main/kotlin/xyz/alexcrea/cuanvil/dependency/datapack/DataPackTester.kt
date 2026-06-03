package xyz.alexcrea.cuanvil.dependency.datapack

import io.papermc.paper.datapack.Datapack
import org.bukkit.Bukkit
import java.util.*

object DataPackTester {

    val enabledPacks: List<String>
        get() {
            return try {
                Bukkit.getDatapackManager().enabledPacks
                    .stream().map { obj: Datapack -> obj.name }
                    .toList()
            } catch (_: Exception){
                // Assume cause UnimplementedOperationException on mock server
                Collections.emptyList()
            }
        }
}
