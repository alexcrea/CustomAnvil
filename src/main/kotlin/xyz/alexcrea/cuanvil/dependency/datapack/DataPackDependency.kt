package xyz.alexcrea.cuanvil.dependency.datapack

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import xyz.alexcrea.cuanvil.api.ConflictBuilder
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.api.MaterialGroupApi
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.enchant.wrapped.CABukkitEnchantment
import xyz.alexcrea.cuanvil.enchant.wrapped.CAIncompatibleAllEnchant
import xyz.alexcrea.cuanvil.group.IncludeGroup
import xyz.alexcrea.cuanvil.update.UpdateUtils
import xyz.alexcrea.cuanvil.update.Version
import java.io.InputStreamReader

object DataPackDependency {
    private val START_DETECT_VERSION = Version(1, 19, 0)

    /**
     * Map of the latest CustomAnvil update related to the pack
     */
    private val LASTEST_VERSION = mapOf(
        Pair("bracken", Version(1, 11, 0))
    )

    val enabledDatapacks: List<String>
        get() {
            val version: Version = UpdateUtils.currentMinecraftVersion()
            if (version.lesserThan(START_DETECT_VERSION)) return emptyList()

            return DataPackTester.enabledPacks
        }

    fun handleDatapackConfigs() {
        val enabledDatapack = enabledDatapacks
        for (packName in enabledDatapack) {
            // Handling of pack name is horrible: it is based on file name
            // So if someone rename a datapack it will make me sad
            if(!packName.startsWith("file/")) continue

            if (packName.contains("bp_post_scarcity", ignoreCase = true)
                || packName.contains("bracken", ignoreCase = true)) {
                handlePack("bracken")
                continue
            }

        }
    }

    private fun handlePack(pack: String){
        CustomAnvil.instance.logger.info("trying to handle datapack $pack")
        handlePackInitialConfig(pack)
        writeDefaultByNamespace(pack)
        CustomAnvil.instance.logger.info("configuration done for $pack")
    }

    private fun handlePackInitialConfig(pack: String) {
        val defConfig = ConfigHolder.DEFAULT_CONFIG
        val version = LASTEST_VERSION[pack]

        val currentVersion = Version.fromString(defConfig.config.getString("datapack.$pack"))
        if (currentVersion.greaterEqual(version!!)) {
            handleEnchantAllConflict(pack)
            return
        }

        // Add pack value or do update from previous version
        // note: update thingy is not yet implemented
        configureDatapack(pack)

        // Finally, set current pack version to config
        defConfig.config.set("datapack.$pack", version.toString())
        defConfig.saveToDisk(true)
    }

    private fun configureDatapack(pack: String) {
        val itemGroups = javaClass.getResource("/datapack/$pack/item_groups.yml")
        val itemConflict = javaClass.getResource("/datapack/$pack/item_conflict.yml")
        val enchantConflict = javaClass.getResource("/datapack/$pack/enchant_conflict.yml")

        if (itemGroups != null) {
            val reader = InputStreamReader(itemGroups.openStream())
            val yml = YamlConfiguration.loadConfiguration(reader)

            handleItemGroups(yml)
        }

        val newConflictList = ArrayList<ConflictBuilder>()
        var needSave = false
        if (itemConflict != null) {
            val reader = InputStreamReader(itemConflict.openStream())
            val yml = YamlConfiguration.loadConfiguration(reader)

            addItemConflicts(yml, newConflictList)
        }

        if (enchantConflict != null) {
            val reader = InputStreamReader(enchantConflict.openStream())
            val yml = YamlConfiguration.loadConfiguration(reader)

            needSave = addEnchantConflict(yml, newConflictList)
        }

        for (conflict in newConflictList) {
            needSave = !conflict.registerIfAbsent() && needSave
        }

        if (needSave) {
            ConfigHolder.CONFLICT_HOLDER.saveToDisk(true)
        }
    }

    // Order matter for this file
    // Could rewrite to not matter but not really important, so I keep it like that
    private fun handleItemGroups(yml: YamlConfiguration) {
        for (groupName in yml.getKeys(false)) {
            val section = yml.getConfigurationSection(groupName) ?: continue

            var group = MaterialGroupApi.getGroup(groupName)
            val exist = group != null

            if (group == null) group = IncludeGroup(groupName)

            for (name in section.getStringList("items")) {
                val mat = Material.getMaterial(name.uppercase())
                if (mat == null) {
                    CustomAnvil.instance.logger.warning("Could not find material $name for item group $groupName")
                    continue
                }
                group.addToPolicy(mat)
            }
            for (name in section.getStringList("groups")) {
                val otherGroup = MaterialGroupApi.getGroup(name)
                if (otherGroup == null) {
                    CustomAnvil.instance.logger.warning("Could not find sub group $name for group $groupName")
                    continue
                }

                group.addToPolicy(otherGroup)
            }

            group.updateMaterials()

            if (exist) {
                MaterialGroupApi.writeMaterialGroup(group)
            } else {
                MaterialGroupApi.addMaterialGroup(group, true)
            }
        }
    }

    private fun addItemConflicts(yml: FileConfiguration, conflictList: MutableList<ConflictBuilder>) {
        for (ench in yml.getKeys(false)) {
            val groups = yml.getStringList(ench)

            val conflict = ConflictBuilder(
                "restriction_${ench.replace(":", "_")}",
                CustomAnvil.instance
            )
            conflict.addEnchantment(NamespacedKey.fromString(ench)!!)
            for (group in groups) {
                conflict.addExcludedGroup(group)
            }
            conflict.addExcludedGroup("enchanted_book")

            conflictList.add(conflict)
        }
    }

    private fun addEnchantConflict(yml: YamlConfiguration, conflictList: MutableList<ConflictBuilder>): Boolean {
        var needSave = false

        val conflicts = HashMap<String, ConflictBuilder>()
        for (ench in yml.getKeys(false)) {
            val groups = yml.getStringList(ench)

            for (group in groups) {
                if (group.startsWith('#')) {
                    needSave = joinGroup(conflicts, group.substring(1), ench) || needSave
                } else {
                    createConflict(conflictList, ench, group)
                }
            }
        }

        conflictList.addAll(conflicts.values)
        return needSave
    }

    private fun createConflict(conflictList: MutableList<ConflictBuilder>, ench: String, other: String) {

        val conflict = ConflictBuilder(
            "conflict_" +
                    "${ench.replace(":", "_")}_" +
                    other.replace(":", "_"),
            CustomAnvil.instance
        )
        conflict.addEnchantment(NamespacedKey.fromString(ench)!!)
        conflict.addEnchantment(NamespacedKey.fromString(other)!!)

        conflict.setMaxBeforeConflict(1)

        conflictList.add(conflict)
    }

    private fun setEnchantAsAll(ench: String) {
        // We assume current is not null and of type CABukkitEnchantment
        val current = EnchantmentApi.getByKey(NamespacedKey.fromString(ench)!!) as CABukkitEnchantment

        // We need to replace current wrapped enchantment with the all conflict wrapper
        EnchantmentApi.unregisterEnchantment(current)
        EnchantmentApi.registerEnchantment(CAIncompatibleAllEnchant(current.bukkit, current.defaultRarity()))
    }

    private fun joinGroup(conflicts: HashMap<String, ConflictBuilder>, group: String, ench: String): Boolean {
        if ("all".equals(group, ignoreCase = true)) {
            setEnchantAsAll(ench)
            return false
        } else {
            val config = ConfigHolder.CONFLICT_HOLDER.config

            // If conflict do not yet exist
            if (!config.isConfigurationSection(group)) {
                val conflict = conflicts.getOrPut(group) {
                    val conflict = ConflictBuilder(group, CustomAnvil.instance)
                    conflict.setMaxBeforeConflict(1)
                    conflict
                }

                conflict.addEnchantment(NamespacedKey.fromString(ench)!!)
                return false
            }
            // Find current conflict
            val manager = ConfigHolder.CONFLICT_HOLDER.conflictManager

            // This assumes that:
            // - the conflict existing in the config exist in the runtime config (as configuration section exist)
            // - the enchantment exist and is provided correctly
            val conflict = manager.conflictList.find {
                it.name.equals(group, ignoreCase = true)
            }
            if(conflict == null) {
                // This should not happen as configuration section
                CustomAnvil.instance.logger.severe("Could not find  $group while its configuration section exist... this should NOT happen")
                return false
            }

            val key = NamespacedKey.fromString(ench)!!
            val enchant = EnchantmentApi.getByKey(key)
            if (enchant == null){
                CustomAnvil.instance.logger.severe("Could not find enchantment $ench while configuring pack a datapack")
                return false
            }

            conflict.addEnchantment(enchant)

            UpdateUtils.addAbsentToList(config, "$group.enchantments", ench)
            return true
        }
    }

    private fun handleEnchantAllConflict(pack: String) {
        val enchantConflict = javaClass.getResource("/datapack/$pack/enchant_conflict.yml") ?: return

        val reader = InputStreamReader(enchantConflict.openStream())
        val yml = YamlConfiguration.loadConfiguration(reader)

        for (ench in yml.getKeys(false)) {
            val groups = yml.getStringList(ench)

            if (groups.contains("#all")) {
                setEnchantAsAll(ench)
            }
        }
    }

    private fun writeDefaultByNamespace(namespace: String) {
        for (enchantment in EnchantmentApi.getRegisteredEnchantments().values) {
            if(!enchantment.key.namespace.equals(namespace, ignoreCase = true)) continue

            CustomAnvil.log("Writing default for ${enchantment.key}")
            EnchantmentApi.writeDefaultConfig(enchantment, false)
        }

    }

}
