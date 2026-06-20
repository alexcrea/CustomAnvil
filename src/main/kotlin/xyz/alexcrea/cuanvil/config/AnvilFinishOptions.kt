package xyz.alexcrea.cuanvil.config

import io.delilaheve.CustomAnvil
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.configuration.ConfigurationSection
import java.util.Random
import java.util.logging.Level

object AnvilFinishOptions {

    data class ConfiguredSound(
        val sound: Sound,
        val category: SoundCategory,
        val volume: Float,
        val pitch: Float,
        val weight: Double,
    )

    // Paths
    private const val SOUND_ENABLED = "anvil_sound.enabled"

    private const val USE_SOUNDS_ROOT = "anvil_sound.use"
    private const val DESTROY_SOUNDS_ROOT = "anvil_sound.destroy"

    private const val ANVIL_DEGRADATION_PATH = "anvil_degradation"

    // Defaults
    private const val DEFAULT_SOUND_ENABLED = true
    private val DEFAULT_USE_SOUND = ConfiguredSound(
        Sound.BLOCK_ANVIL_USE,
        SoundCategory.BLOCKS,
        1.0f,
        1.0f,
        1.0,
    )

    private val DEFAULT_DESTROY_SOUND = ConfiguredSound(
        Sound.BLOCK_ANVIL_DESTROY,
        SoundCategory.BLOCKS,
        1.0f,
        1.0f,
        1.0,
    )
    private const val DEFAULT_ANVIL_DEGRADATION_PATH = 0.12

    // Others
    private val RANDOM = Random()

    // Fetch
    val sound_enabled: Boolean
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getBoolean(SOUND_ENABLED, DEFAULT_SOUND_ENABLED)
        }

    fun getAnvilSound(wasDestroyed: Boolean): ConfiguredSound {
        return if(wasDestroyed)
            getRandomDestroySound()
        else getRandomUseSound()
    }

    val degradation_chance: Double
        get() {
            return ConfigHolder.DEFAULT_CONFIG
                .config
                .getDouble(ANVIL_DEGRADATION_PATH, DEFAULT_ANVIL_DEGRADATION_PATH)
        }

    // Utils
    private fun getRandomUseSound(): ConfiguredSound {
        return fromListing(USE_SOUNDS_ROOT, DEFAULT_USE_SOUND)
    }

    private fun getRandomDestroySound(): ConfiguredSound {
        return fromListing(DESTROY_SOUNDS_ROOT, DEFAULT_DESTROY_SOUND)
    }

    private fun fromListing(path: String, default: ConfiguredSound): ConfiguredSound {
        val section = ConfigHolder.DEFAULT_CONFIG
            .config.getConfigurationSection(path) ?: return default

        val list = readSounds(section)
        return weightedRandom(list, default)
    }

    private fun readSounds(section: ConfigurationSection): List<ConfiguredSound> {
        val sounds = ArrayList<ConfiguredSound>()

        for (key in section.getKeys(false)) {
            if(!section.isConfigurationSection(key)) continue

            val result = readSound(section.getConfigurationSection(key)!!)
            if(result == null) {
                CustomAnvil.instance.logger.warning("Couldn't read sounds for key $key")
                continue
            }

            sounds.add(result)
        }
        return sounds
    }

    private fun readSound(section: ConfigurationSection): ConfiguredSound? {
        val name = section.getString("sound", null)?: return null
        val key = NamespacedKey.fromString(name)?: return null
        val sound = Registry.SOUNDS.get(key)?: return null

        val categoryName = section.getString("category", null)?: return null
        val category = try {
            SoundCategory.valueOf(categoryName.uppercase())
        } catch (_: IllegalArgumentException) {
            return null
        }

        val volume = section.getDouble("volume", 1.0).toFloat()
        val pitch = section.getDouble("pitch", 1.0).toFloat()
        val weight = section.getDouble("weight", 1.0)
        if(weight <= 0) return null

        return ConfiguredSound(sound, category, volume, pitch, weight)
    }

    private fun weightedRandom(sounds: List<ConfiguredSound>, default: ConfiguredSound): ConfiguredSound {
        if(sounds.isEmpty()) return default

        val weightSum = sounds.sumOf { it.weight }
        if(weightSum == .0) return sounds.first()

        val selectedWeight = RANDOM.nextDouble(weightSum)

        var currentSum = .0
        sounds.forEach {
            currentSum += it.weight
            if(currentSum > selectedWeight) return it
        }

        // What ? how
        CustomAnvil.instance.logger.log(Level.SEVERE, "Something very... very wrong happen on sound handling. please report this to the developer")
        return default
    }

}