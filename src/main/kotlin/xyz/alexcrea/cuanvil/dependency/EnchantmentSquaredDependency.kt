package xyz.alexcrea.cuanvil.dependency

import io.delilaheve.CustomAnvil
import me.athlaeos.enchantssquared.enchantments.CustomEnchant
import me.athlaeos.enchantssquared.managers.CustomEnchantManager
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEnchantSquaredEnchantment
import java.util.*

class EnchantmentSquaredDependency(private val enchantmentSquaredPlugin: Plugin) {

    init {
        CustomAnvil.instance.logger.info("Enchantment Squared Detected !")
        CustomAnvil.instance.logger.info("Please be aware that Custom Anvil is bypassing Enchantment Squared ")
        CustomAnvil.instance.logger.info(
            "compatible_with, " +
                    "disable_anvil, " +
                    "incompatible_vanilla_enchantments, " +
                    "incompatible_custom_enchantments and max_level " +
                    "configuration values.")
    }

    fun disableAnvilListener(){
        PrepareAnvilEvent.getHandlerList().unregister(this.enchantmentSquaredPlugin)
    }

    fun registerEnchantments(){
        for (enchant in CustomEnchantManager.getInstance().allEnchants.values) {
            CAEnchantmentRegistry.getInstance().register(
                CAEnchantSquaredEnchantment(
                    enchant
                )
            )
        }

    }

    fun getEnchantmentsSquared(item: ItemStack, enchantments: MutableMap<CAEnchantment, Int>) {
        val customEnchants = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(item)

        customEnchants.forEach{
                (enchantment, level ) -> enchantments[getWrappedEnchant(enchantment)] = level
        }

    }

    fun clearEnchantments(item: ItemStack) {
        CustomEnchantManager.getInstance().removeAllEnchants(item)
    }

    fun getKeyFromEnchant(enchant: CustomEnchant): NamespacedKey{
        return NamespacedKey.fromString(enchant.type.lowercase(Locale.getDefault()), this.enchantmentSquaredPlugin)!!
    }
    private fun getWrappedEnchant(enchant: CustomEnchant): CAEnchantment {
        return CAEnchantment.getByKey(getKeyFromEnchant(enchant))!!
    }


    private val IS_READY_PATH = "enchantment_square_ready"
    fun registerPluginConfiguration(){
        val defaultConfig = ConfigHolder.DEFAULT_CONFIG.config
        val isReady = defaultConfig.getBoolean(IS_READY_PATH, false)
        if(isReady) return

        CustomAnvil.instance.logger.info("Preparing configuration for Enchantment Squared...")

        // Prepare enchantments
        val esEnchantments = ArrayList<CAEnchantSquaredEnchantment>()
        CustomEnchantManager.getInstance().allEnchants.forEach { (_, enchant) ->
            esEnchantments.add(getWrappedEnchant(enchant) as CAEnchantSquaredEnchantment)
        }

        // Write default level limit and xp cost
        for (enchantment in esEnchantments) {
            defaultConfig["enchant_limits.${enchantment.key.key}"] = enchantment.defaultMaxLevel()

            val rarity = enchantment.defaultRarity()
            defaultConfig["enchant_values.${enchantment.key.key}.item"] = rarity.itemValue
            defaultConfig["enchant_values.${enchantment.key.key}.book"] = rarity.bookValue
        }

        // Write groups and conflicts
        writeMissingGroups()
        writeMaterialRestriction(esEnchantments)
        writeEnchantmentConflicts(esEnchantments)

        // Set ready
        defaultConfig[IS_READY_PATH] = true

        // Save
        ConfigHolder.DEFAULT_CONFIG.saveToDisk(true)
        ConfigHolder.ITEM_GROUP_HOLDER.saveToDisk(true)
        ConfigHolder.CONFLICT_HOLDER.saveToDisk(true)

        // Reload
        ConfigHolder.ITEM_GROUP_HOLDER.reload()

        CustomAnvil.instance.logger.info("Enchantment Squared should now work as expected !")
    }

    private fun writeMissingGroups(){
        // Write group that do not exist on custom anvil.
        // (Tools group regroup most of the tool items. I did not create a seperated group for theses)
        val groupConfig = ConfigHolder.ITEM_GROUP_HOLDER.config
        if(!groupConfig.isConfigurationSection("pickaxes")){
            groupConfig["pickaxes.type"] = "include"
            groupConfig["pickaxes.items"] = listOf("wooden_pickaxe", "stone_pickaxe", "iron_pickaxe", "diamond_pickaxe", "golden_pickaxe", "netherite_pickaxe")
        }

        if(!groupConfig.isConfigurationSection("shovels")){
            groupConfig["shovels.type"] = "include"
            groupConfig["shovels.items"] = listOf("wooden_shovel", "stone_shovel", "iron_shovel", "diamond_shovel", "golden_shovel", "netherite_shovel")
        }

        if(!groupConfig.isConfigurationSection("hoes")){
            groupConfig["hoes.type"] = "include"
            groupConfig["hoes.items"] = listOf("wooden_hoe", "stone_hoe", "iron_hoe", "diamond_hoe", "golden_hoe", "netherite_hoe")
        }

        if(!groupConfig.isConfigurationSection("shield")){
            groupConfig["shield.type"] = "include"
            groupConfig["shield.items"] = listOf("shield")
        }

        if(!groupConfig.isConfigurationSection("elytra")){
            groupConfig["elytra.type"] = "include"
            groupConfig["elytra.items"] = listOf("elytra")
        }

        if(!groupConfig.isConfigurationSection("trinkets")){
            groupConfig["trinkets.type"] = "include"
            groupConfig["trinkets.items"] = listOf("rotten_flesh")
        }

    }

    private fun writeMaterialRestriction(esEnchantments: List<CAEnchantSquaredEnchantment>){
        val conflictConfig = ConfigHolder.CONFLICT_HOLDER.config
        for (enchantment in esEnchantments) {
            val restrictionName = "restriction_${enchantment.key.key}"
            if(!conflictConfig.isConfigurationSection(restrictionName)){
                conflictConfig["$restrictionName.enchantments"] = listOf(enchantment.name)

                // Get allowed groups
                val listOfAllowed = ArrayList<String>()
                listOfAllowed.add("enchanted_book") // enchanted book is allowed in any case.

                for (esGroup in enchantment.enchant.compatibleItems) {
                    val caGroup = esGroupToCAGroup(esGroup)
                    if(caGroup == null){
                        CustomAnvil.instance.logger.info("Could not find equivalent custom anvil group for $esGroup")
                        continue
                    }
                    listOfAllowed.add(caGroup)
                }
                conflictConfig["$restrictionName.notAffectedGroups"] = listOfAllowed
            }
        }
    }

    private fun writeEnchantmentConflicts(esEnchantments: List<CAEnchantSquaredEnchantment>){
        val otherEnchants = ArrayList<CAEnchantment>()
        otherEnchants.addAll(CAEnchantmentRegistry.getInstance().values())

        for (enchantment in esEnchantments) {
            otherEnchants.remove(enchantment)

            // find conflicting enchantment.
            for (otherEnchant in otherEnchants) {
                if(enchantment.enchant.conflictsWithEnchantment(otherEnchant.name)){
                    writeConflict(enchantment, otherEnchant)
                }
            }
        }
    }

    private fun writeConflict(enchantment1: CAEnchantment, enchantment2: CAEnchantment){
        val conflictConfig = ConfigHolder.CONFLICT_HOLDER.config
        val conflictPath = "${enchantment1.name}_with_${enchantment2.name}_conflict"

        if(!conflictConfig.isConfigurationSection(conflictPath)){
            conflictConfig["$conflictPath.enchantments"] = listOf(enchantment1.name, enchantment2.name)

            val empty: List<String> = Collections.emptyList()
            conflictConfig["$conflictPath.notAffectedGroups"] = empty

            conflictConfig["$conflictPath.maxEnchantmentBeforeConflict"] = 1
        }

    }

    /**
     * Transform an Enchantment Squared group to a Custom Anvil group
     */
    private fun esGroupToCAGroup(esGroup: String): String? {
        return when(esGroup){
            "SWORDS" -> "swords"
            "BOWS" -> "bow"
            "CROSSBOWS" -> "crossbow"
            "TRIDENTS" -> "trident"
            "HELMETS" -> "helmets"
            "CHESTPLATES" -> "chestplate"
            "LEGGINGS" -> "leggings"
            "BOOTS" -> "boots"
            "SHEARS" -> "shears"
            "FLINTANDSTEEL" -> "flint_and_steel"
            "FISHINGROD" -> "fishing_rod"
            "ELYTRA" -> "elytra"
            "PICKAXES" -> "pickaxes" // not on this plugin by default
            "AXES" -> "axes"
            "SHOVELS" -> "shovels" // not on this plugin by default
            "HOES" -> "hoes" // not on this plugin by default
            "SHIELDS" -> "shield" // not on this plugin by default
            "TRINKETS" -> "trinkets" // not the idea way as it will also allow non trinkets rotten flesh to be enchanted.
            "ALL" -> "everything"
            else -> null
        }
    }

}
