package xyz.alexcrea.cuanvil.group

import io.delilaheve.CustomAnvil
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import xyz.alexcrea.cuanvil.enchant.AdditionalTestEnchantment
import xyz.alexcrea.cuanvil.enchant.CAEnchantment
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry
import java.util.*

class EnchantConflictManager {

    companion object {
        // Path for the enchantments list
        const val ENCH_LIST_PATH = "enchantments"

        // Path for group list related to the conflict
        const val CONFLICT_GROUP_PATH = "notAffectedGroups"

        // Path for the maximum number of enchantment before validating the conflict
        const val ENCH_MAX_PATH = "maxEnchantmentBeforeConflict"

        // Path for a flag: if the enchantment will be used in the last supported version
        // TODO maybe replace this system by a list of "future" enchantment.
        private const val FUTURE_USE_PATH = "useInFuture"

        // Default name for a joining group
        const val DEFAULT_GROUP_NAME = "joinedGroup"

        // 1.20.5 compatibility TODO better update system
        private val SWEEPING_EDGE_ENCHANT =
            CAEnchantment.getByKey(NamespacedKey.minecraft("sweeping_edge")) ?:
            CAEnchantment.getByKey(Enchantment.SWEEPING_EDGE.key)

    }

    lateinit var conflictList: ArrayList<EnchantConflictGroup>

    // Read and prepare all conflict
    fun prepareConflicts(config: ConfigurationSection, itemManager: ItemGroupManager) {
        conflictList = ArrayList()

        // Clear conflict if exist
        for (enchant in CAEnchantmentRegistry.getInstance().values()) {
            enchant.clearConflict()
        }
        
        val keys = config.getKeys(false)
        for (key in keys) {
            val section = config.getConfigurationSection(key)!!
            val conflict = createConflict(section, itemManager, key)

            addConflict(conflict)
        }

    }

    fun addConflict(conflict: EnchantConflictGroup){
        addConflictToEnchantments(conflict)
        conflictList.add(conflict)
    }

    fun removeConflict(conflict: EnchantConflictGroup){
        removeConflictFromEnchantments(conflict)
        conflictList.remove(conflict)
    }

    // Add the conflict to enchantments
    private fun addConflictToEnchantments(conflict: EnchantConflictGroup) {
        conflict.getEnchants().forEach { enchant ->
            enchant.addConflict(conflict)
        }
    }

    // Remove the conflict from enchantments
    private fun removeConflictFromEnchantments(conflict: EnchantConflictGroup) {
        conflict.getEnchants().forEach { enchant ->
            enchant.removeConflict(conflict)
        }
    }

    // create and read a conflict from a yaml section
    private fun createConflict(
        section: ConfigurationSection,
        itemManager: ItemGroupManager,
        conflictName: String
    ): EnchantConflictGroup {
        // Is it planed for the future
        val futureUse = section.getBoolean(FUTURE_USE_PATH, false)
        // Create conflict
        val conflict = createConflictObject(section, itemManager, conflictName)
        // Read and add enchantment to conflict
        val enchantList = section.getStringList(ENCH_LIST_PATH)
        for (enchantName in enchantList) {
            val enchant = getEnchantByName(enchantName)
            if (enchant == null) {
                if (!futureUse) { //TODO future use will be deprecated once the new update system is finished
                    CustomAnvil.instance.logger.warning("Enchantment $enchantName do not exist but was asked for conflict $conflictName")
                }
                continue
            }
            conflict.addEnchantment(enchant)
        }
        if (conflict.getEnchants().isEmpty()) {
            if (!futureUse) { //TODO future use will be deprecated once the new update system is finished
                CustomAnvil.instance.logger.warning("Conflict $conflictName do not have valid enchantment, it will not do anything")
            }
        }

        return conflict
    }

    private fun getEnchantByName(enchantName: String): CAEnchantment? {

        // Temporary solution for 1.20.5
        when(enchantName){
            "sweeping", "sweeping_edge" -> {
                return SWEEPING_EDGE_ENCHANT
            }
        }

        return CAEnchantment.getByName(enchantName)
    }


    private fun createConflictObject(
        section: ConfigurationSection,
        itemManager: ItemGroupManager,
        conflictName: String
    ): EnchantConflictGroup {
        // Get the maximum number of enchantment before validating the conflict
        var minBeforeBlock = section.getInt(ENCH_MAX_PATH, 0)
        if (minBeforeBlock < 0) {
            minBeforeBlock = 0
            CustomAnvil.instance.logger.warning("Conflict $conflictName have an invalid value of $ENCH_MAX_PATH")
            CustomAnvil.instance.logger.warning("It should be more or equal to 0. default to 0")
        }
        // Find or create the selected group for the conflict
        val groupList = section.getStringList(CONFLICT_GROUP_PATH)
        val finalGroup = IncludeGroup(DEFAULT_GROUP_NAME)
        for (groupName in groupList) {
            finalGroup.addToPolicy(findGroup(groupName, itemManager, conflictName))
        }

        // Return conflict
        return EnchantConflictGroup(conflictName, finalGroup, minBeforeBlock)
    }

    private fun findGroup(
        groupName: String,
        itemManager: ItemGroupManager,
        conflictName: String
    ): AbstractMaterialGroup {
        val group = itemManager.get(groupName)
        if (group == null) {
            CustomAnvil.instance.logger.warning("Material group $groupName do not exist but is ask by conflict $conflictName")
            return IncludeGroup("error_placeholder")
        }

        return group
    }

    fun isConflicting(appliedEnchants: Map<CAEnchantment, Int>, item: ItemStack, newEnchant: CAEnchantment): ConflictType {
        val mat = item.type
        CustomAnvil.verboseLog("Testing conflict for ${newEnchant.key} on ${mat.key}")
        val conflictList = newEnchant.conflicts

        var result = ConflictType.NO_CONFLICT
        for (conflict in conflictList) {
            CustomAnvil.verboseLog("Is against $conflict")
            val allowed = conflict.allowed(appliedEnchants.keys, mat)
            CustomAnvil.verboseLog("Was against $conflict and conflicting: ${!allowed} ")
            if (!allowed) {
                if (conflict.getEnchants().size <= 1) {
                    result = ConflictType.ITEM_CONFLICT
                    CustomAnvil.verboseLog("Small conflict, continuing")
                } else {
                    CustomAnvil.verboseLog("Big conflict, probably stoping")
                    return ConflictType.ENCHANTMENT_CONFLICT
                }
            }
        }

        val immutableEnchants = Collections.unmodifiableMap(appliedEnchants)
        for (appliedEnchant in appliedEnchants) {
            if(appliedEnchant is AdditionalTestEnchantment){
                val doConflict = appliedEnchant.isEnchantConflict(immutableEnchants, mat)
                if(doConflict){
                    return ConflictType.ENCHANTMENT_CONFLICT
                }

            }
        }

        if((result != ConflictType.ITEM_CONFLICT) && (newEnchant is AdditionalTestEnchantment)){
            val partialItem = createPartialResult(item, immutableEnchants)

            if(newEnchant.isItemConflict(immutableEnchants, mat, partialItem)){
                return ConflictType.ITEM_CONFLICT
            }

        }

        return result
    }

    private fun createPartialResult(item: ItemStack, enchantments: Map<CAEnchantment, Int>): ItemStack {
       val newItem = item.clone()

       CAEnchantment.clearEnchants(newItem)
       enchantments.forEach{//TODO maybe bulk add if possible
           enchantment -> enchantment.key.addEnchantmentUnsafe(newItem, enchantment.value)
       }

       return newItem
    }

}

/**
 * Provide information about the current conflict.
 */
enum class ConflictType(private val importance: Int) {
    /**
     * Allowed to proceed the anvil process.
     */
    NO_CONFLICT(0),

    /**
     * Inform that the anvil process should not change the current applied enchantment.
     */
    ITEM_CONFLICT(1),

    /**
     * Inform that the anvil process should not change the current applied enchantment.
     * Also add sacrificeIllegalCost for every enchantment marked as big conflict.
     */
    ENCHANTMENT_CONFLICT(2);

    fun getWorstConflict(otherConflict: ConflictType): ConflictType {
        return if(this.importance > otherConflict.importance) this
        else otherConflict

    }

}