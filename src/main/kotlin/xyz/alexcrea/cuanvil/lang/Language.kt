package xyz.alexcrea.cuanvil.lang

import io.delilaheve.CustomAnvil
import org.bukkit.configuration.ConfigurationSection
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

    fun getSection(key: String): ConfigurationSection? {
        return conf.getConfigurationSection(key)
    }

    fun has(key: String): Boolean {
        if(conf.isString(key)) return true

        // we want at all child key as valid numbers and valid key if claimed to be multi line
        val section = getSection(key) ?: return false
        for(key in section.getKeys(false)) {
            if(key.toUIntOrNull() == null) return false
            if(!section.isString(key)) return false
        }

        return true
    }

    fun getFilteredKeys(): Collection<String> {
        val result = ArrayList<String>()

        // First pass we ignore key from root
        for(root in conf.getKeys(false)) {
            val section = conf.getConfigurationSection(root) ?: continue

            exploreDeeper(section, root, result)
        }

        return result
    }

    private fun exploreDeeper(section: ConfigurationSection, root: String, result: ArrayList<String>) {
        for(key in section.getKeys(false)) {
            val newRoot = "$root.$key"
            if(has(key)) {
                result.add(newRoot)
                continue
            }

            val newSection = section.getConfigurationSection(key) ?: continue
            exploreDeeper(newSection, newRoot, result)
        }
    }

    val name: String
        get() = conf.getString("name", id)!!
}