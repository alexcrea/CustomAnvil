package xyz.alexcrea.cuanvil.lang

import io.delilaheve.CustomAnvil
import xyz.alexcrea.cuanvil.config.ConfigHolder

object Lang {

    private val default = Language(DEFAULT_LANG, false)
    private var lang = default

    fun reload() {
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