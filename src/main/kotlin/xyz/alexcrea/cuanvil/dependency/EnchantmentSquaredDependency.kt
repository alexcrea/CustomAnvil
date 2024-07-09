package xyz.alexcrea.cuanvil.dependency

import io.delilaheve.CustomAnvil
import me.athlaeos.enchantssquared.enchantments.CustomEnchant
import me.athlaeos.enchantssquared.managers.CustomEnchantManager
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import xyz.alexcrea.cuanvil.api.ConflictBuilder
import xyz.alexcrea.cuanvil.api.EnchantmentApi
import xyz.alexcrea.cuanvil.api.MaterialGroupApi
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import xyz.alexcrea.cuanvil.enchant.wrapped.CAEnchantSquaredEnchantment
import xyz.alexcrea.cuanvil.group.IncludeGroup
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
            EnchantmentApi.registerEnchantment(CAEnchantSquaredEnchantment(enchant))
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
        for (enchantment in esEnchantments) { //TODO move to api register
            DependencyManager.writeDefaultConfig(defaultConfig, enchantment)
        }

        // Write groups and conflicts
        writeMissingGroups()
        writeMaterialRestriction(esEnchantments)
        writeEnchantmentConflicts(esEnchantments)

        // Set ready
        defaultConfig[IS_READY_PATH] = true

        // Save
        ConfigHolder.DEFAULT_CONFIG.saveToDisk(true)

        CustomAnvil.instance.logger.info("Enchantment Squared should now work as expected !")
    }

    private fun writeMissingGroups(){
        // Write group that do not exist on custom anvil.
        // (Tools group regroup most of the tool items. I did not create a seperated group for theses)
        val pickaxes = IncludeGroup("pickaxes")
        pickaxes.addAll(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE, Material.NETHERITE_PICKAXE)
        MaterialGroupApi.addMaterialGroup(pickaxes)

        val shovels = IncludeGroup("shovels")
        shovels.addAll(Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.GOLDEN_SHOVEL, Material.NETHERITE_SHOVEL)
        MaterialGroupApi.addMaterialGroup(shovels)

        val hoes = IncludeGroup("hoes")
        hoes.addAll(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.DIAMOND_HOE, Material.GOLDEN_HOE, Material.NETHERITE_HOE)
        MaterialGroupApi.addMaterialGroup(hoes)

        val shield = IncludeGroup("shield")
        hoes.addToPolicy(Material.SHIELD)
        MaterialGroupApi.addMaterialGroup(shield)

        val elytra = IncludeGroup("elytra")
        hoes.addToPolicy(Material.ELYTRA)
        MaterialGroupApi.addMaterialGroup(elytra)

        val trinkets = IncludeGroup("trinkets")
        hoes.addToPolicy(Material.ROTTEN_FLESH)
        MaterialGroupApi.addMaterialGroup(trinkets)

    }

    private fun writeMaterialRestriction(esEnchantments: List<CAEnchantSquaredEnchantment>){
        for (enchantment in esEnchantments) {
            val conflict = ConflictBuilder("restriction_${enchantment.key.key}")

            // enchanted book is allowed in any case.
            conflict.addExcludedGroup("enchanted_book")

            // Get allowed groups
            for (esGroup in enchantment.enchant.compatibleItems) {
                val caGroup = esGroupToCAGroup(esGroup)
                if(caGroup == null){
                    CustomAnvil.instance.logger.info("Could not find equivalent custom anvil group for $esGroup")
                    continue
                }
                conflict.addExcludedGroup(caGroup)
            }

            conflict.registerIfAbsent()
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
        val conflict = ConflictBuilder("${enchantment1.name}_with_${enchantment2.name}_conflict")

        conflict.addEnchantment(enchantment1).addEnchantment(enchantment2)

        conflict.setMaxBeforeConflict(1)
        conflict.registerIfAbsent()
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
