package xyz.alexcrea.cuanvil.lang

import io.delilaheve.CustomAnvil
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStreamReader
import java.util.logging.Level

class Language(private val id: String, private val default: Boolean = false) {

    private val resourcePath: String
    private val file: File

    private val conf: FileConfiguration

    init {
        conf = YamlConfiguration()
        resourcePath = "lang/$id.yml"
        file = File(CustomAnvil.instance.dataFolder, resourcePath)

        reload()
    }

    fun reload() {
        if(file.exists() && !default) try {
            conf.load(file)
            return
        } catch(e: Exception) {
            CustomAnvil.instance.logger.log(
                Level.SEVERE,
                "Could not load custom Anvil config file. using to internal resource",
                e
            )
        }

        loadInternalResource()
    }

    private fun loadInternalResource() {
        val input = CustomAnvil.instance.getResource(resourcePath)
        if(input != null)
            conf.load(InputStreamReader(input))
        else
            CustomAnvil.instance.logger.log(Level.SEVERE, "Language $id not found")

    }

    fun get(key: String): String? {
        return conf.getString(key)
    }

    val name: String
        get() = conf.getString("name", id)!!
}