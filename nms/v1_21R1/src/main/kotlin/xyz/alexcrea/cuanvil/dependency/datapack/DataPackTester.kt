package xyz.alexcrea.cuanvil.dependency.datapack

import io.papermc.paper.datapack.Datapack
import org.bukkit.Bukkit
import java.util.*

object DataPackTester {
    val legacyNames: List<String>
        get() = Bukkit.getDataPackManager().dataPacks
            .stream().filter { obj -> obj.isEnabled }
            .map { pack -> pack.key.key }
            .toList()

    val enabledPacks: List<String>
        get() {
            try {
                // will throw error if do not exist
                Bukkit::class.java.getDeclaredMethod("getDatapackManager")

                return Bukkit.getDatapackManager().enabledPacks
                    .stream().map { obj: Datapack -> obj.name }
                    .toList()
            } catch (e: NoSuchMethodException) {
                return legacyNames
            } catch (e: Exception){
                // Assume cause UnimplementedOperationException on mock server
                return Collections.emptyList()
            }
        }
}
