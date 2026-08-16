@file:Suppress("DEPRECATION", "removal")
package xyz.alexcrea.cuanvil.dependency.datapack

import io.papermc.paper.datapack.Datapack
import org.bukkit.Bukkit
import org.bukkit.packs.DataPack
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
            } catch(_: NoSuchMethodException) {
                try {
                    DataPack::class.java.getDeclaredMethod("getKey")
                } catch(_: NoSuchMethodException) {
                    System.err.println("Could not find compatible datapack manager")
                    System.err.println("If you are using a datapack that should be compatible with CustomAnvil. It will not get detected...")
                    return emptyList()
                }
                return legacyNames
            } catch(_: Exception) {
                // Assume cause UnimplementedOperationException on mock server
                return Collections.emptyList()
            }
        }
}
