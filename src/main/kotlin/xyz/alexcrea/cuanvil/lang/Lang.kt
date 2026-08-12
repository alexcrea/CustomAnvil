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

    fun String.translate(): String {
        val value = lang.get(this)
        if(value != null) return value

        CustomAnvil.log("Missing language data for ${lang.name} using default")

        return default.get(this) ?: this
    }

    fun String.translate(vararg params: Pair<String, Any>): String {
        return translate(
            params.asSequence()
                .map { Pair(it.first, it.second.toString()) }
                .toMap()
        )
    }

    fun String.translate(vararg params: Pair<Char, Any>): String {
        return translate(
            params.asSequence()
                .map { Pair(it.first.toString(), it.second.toString()) }
                .toMap()
        )
    }

    fun String.translate(vararg params: Pair<String, String>): String {
        return translate(params.toMap())
    }

    fun String.translate(vararg params: Pair<Char, String>): String {
        return translate(
            params.asSequence()
                .map { Pair(it.first.toString(), it.second) }
                .toMap()
        )
    }

    fun String.translate(params: Map<String, String>): String {
        val builder = StringBuilder(translate())

        // replace all placeholder thingy %key -> value
        for((key, replacement) in params) {
            var current = 0
            while(true) {
                current = builder.indexOf('%', current) + 1
                if(current < 0 || current + key.length > builder.length) break // may be able to be removed if bound checked in startsWith ?
                if(!builder.startsWith(key, current, false)) continue

                builder.replace(current - 1, current + key.length, replacement)
                current = current - 1 + replacement.length
            }
        }

        return builder.toString()
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