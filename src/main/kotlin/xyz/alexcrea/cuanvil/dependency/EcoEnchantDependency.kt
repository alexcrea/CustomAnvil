package xyz.alexcrea.cuanvil.dependency

import com.willfp.ecoenchants.enchant.EcoEnchants
import io.delilaheve.CustomAnvil
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEcoEnchant
import java.io.File

class EcoEnchantDependency(private val ecoEnchantPlugin: Plugin) {

    init {
        CustomAnvil.instance.logger.info("Eco Enchant Detected !")
    }

    fun disableAnvilListener(){
        PrepareAnvilEvent.getHandlerList().unregister(this.ecoEnchantPlugin)
    }

    fun registerEnchantments() {
        val registery = CAEnchantmentRegistry.getInstance()
        for (ecoEnchant in EcoEnchants.values()) {
            val enchantments: CAEnchantment = CAEcoEnchant(ecoEnchant)

            registery.unregister(registery.getByKey(ecoEnchant.enchantment.key)) // As eco enchants are considered real enchantment, we need to unregister it.
            registery.register(enchantments)
        }
    }

    fun registerPluginConfiguration(folder: File){
        val compatibilityFile = File(folder, "ecoEnchant.yml")

        if(compatibilityFile.exists()){
            folder.mkdirs()
            compatibilityFile.createNewFile()
        }

        val config = YamlConfiguration.loadConfiguration(compatibilityFile)
        val defaultConfig = ConfigHolder.DEFAULT_CONFIG.config
        var doSave = false

        for (ecoEnchant in EcoEnchants.values()) {
            val enchantment = CAEnchantmentRegistry.getInstance().getByKey(ecoEnchant.enchantmentKey)

            if(enchantment == null){
                CustomAnvil.instance.logger.warning("Could not find " + ecoEnchant.enchantmentKey + "testing compatibility.")
                continue
            }

            // Write enchantment value if needed
            val testPath = "default.${enchantment.key.key}"
            if(!config.getBoolean(testPath, false)){
                doSave = true
                config[testPath] = true

                defaultConfig["enchant_limits.${enchantment.key.key}"] = enchantment.defaultMaxLevel()

                val rarity = enchantment.defaultRarity()
                defaultConfig["enchant_values.${enchantment.key.key}.item"] = rarity.itemValue
                defaultConfig["enchant_values.${enchantment.key.key}.book"] = rarity.bookValue

            }
        }

        if(doSave){
            config.save(compatibilityFile)
            ConfigHolder.DEFAULT_CONFIG.saveToDisk(true)

            CustomAnvil.instance.logger.info("Saved default for new eco enchant enchantments.")
        }

    }

}
