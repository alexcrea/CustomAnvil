package xyz.alexcrea.cuanvil.dependency

import com.willfp.ecoenchants.enchant.EcoEnchants
import io.delilaheve.CustomAnvil
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import java.io.File

class EcoEnchantDependency(private val ecoEnchantPlugin: Plugin) {

    init {
        CustomAnvil.instance.logger.info("Eco Enchant Detected !")
    }

    fun disableAnvilListener(){
        PrepareAnvilEvent.getHandlerList().unregister(this.ecoEnchantPlugin)
    }

    fun registerEnchantments() {
        for (ecoEnchant in EcoEnchants.values()) {
            EnchantmentApi.unregisterEnchantment(ecoEnchant.enchantment) // As eco enchants is loaded before ca, we need to unregister old "vanilla" enchant.
            EnchantmentApi.registerEnchantment(ecoEnchant.enchantment)
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

                DependencyManager.writeDefaultConfig(defaultConfig, enchantment)
            }
        }

        if(doSave){
            config.save(compatibilityFile)
            ConfigHolder.DEFAULT_CONFIG.saveToDisk(true)

            CustomAnvil.instance.logger.info("Saved default for new eco enchant enchantments.")
        }

    }

}
