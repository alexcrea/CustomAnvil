package xyz.alexcrea.cuanvil.lang

import io.delilaheve.CustomAnvil
import org.bukkit.configuration.ConfigurationSection
import xyz.alexcrea.cuanvil.config.ConfigHolder

object Lang {

    private lateinit var default: Language
    private lateinit var lang: Language

    fun loadDefault() {
        default = Language(DEFAULT_LANG, false)
        lang = default
        reload()
    }

    fun reload(): Boolean {
        try {
            unsafeReload()
            return true
        } catch(e: Exception) {
            CustomAnvil.logError("Error loading language $langID", e, false)
            return false
        }
    }

    private fun unsafeReload() {
        val langID = langID
        if(lang.name == langID && lang != default)
            lang.reload()
        else
            lang = Language(langID)

        if(default != lang) default.reload()
    }

    fun getTranslated(key: String): String {
        val value = lang.get(key)
        if(value != null) return value

        CustomAnvil.log("Missing language data for ${lang.name} using default")

        return default.get(key) ?: key
    }

    fun getSection(key: String): ConfigurationSection? {
        val value = lang.getSection(key)
        if(value != null) return value

        return default.getSection(key)
    }

    /*
     * Config Options & get
     */
    const val LANG_PATH = "language"
    const val DEFAULT_LANG = "en"

    /**
     * Value of an item rename
     */
    private val langID: String
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getString(LANG_PATH, DEFAULT_LANG)!!
        }

}