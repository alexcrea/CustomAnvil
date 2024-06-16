package xyz.alexcrea.cuanvil.group

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment

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
        private const val DEFAULT_GROUP_NAME = "joinedGroup"

        // 1.20.5 compatibility TODO better update system
        private val SWEEPING_EDGE_ENCHANT =
            WrappedEnchantment.getByKey(NamespacedKey.minecraft("sweeping_edge")) ?:
            WrappedEnchantment.getByKey(Enchantment.SWEEPING_EDGE.key)

    }

    private lateinit var conflictMap: HashMap<WrappedEnchantment, ArrayList<EnchantConflictGroup>>
    lateinit var conflictList: ArrayList<EnchantConflictGroup>

    // Read and prepare all conflict
    fun prepareConflicts(config: ConfigurationSection, itemManager: ItemGroupManager) {
        conflictMap = HashMap()
        conflictList = ArrayList()

        val keys = config.getKeys(false)
        for (key in keys) {
            val section = config.getConfigurationSection(key)!!
            val conflict = createConflict(section, itemManager, key)

            addToMap(conflict)
            conflictList.add(conflict)
        }

    }

    // Add the conflict to the map
    private fun addToMap(conflict: EnchantConflictGroup) {
        conflict.getEnchants().forEach { enchant ->
            addConflictToConflictMap(enchant, conflict)
        }
    }

    fun addConflictToConflictMap(enchant: WrappedEnchantment, conflict: EnchantConflictGroup) {
        if (!conflictMap.containsKey(enchant)) {
            conflictMap[enchant] = ArrayList()
        }
        conflictMap[enchant]!!.add(conflict)
    }

    fun removeConflictFromMap(enchant: WrappedEnchantment, conflict: EnchantConflictGroup): Boolean {
        return conflictMap[enchant]!!.remove(conflict)
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
                if (!futureUse) {
                    CustomAnvil.instance.logger.warning("Enchantment $enchantName do not exist but was asked for conflict $conflictName")
                }
                continue
            }
            conflict.addEnchantment(enchant)
        }
        if (conflict.getEnchants().size == 0) {
            if (!futureUse) {
                CustomAnvil.instance.logger.warning("Conflict $conflictName do not have valid enchantment, it will not do anything")
            }
        }

        return conflict
    }

    private fun getEnchantByName(enchantName: String): WrappedEnchantment? {

        // Temporary solution for 1.20.5
        when(enchantName){
            "sweeping", "sweeping_edge" -> {
                return SWEEPING_EDGE_ENCHANT
            }
        }

        val enchantKey = NamespacedKey.minecraft(enchantName)
        return WrappedEnchantment.getByKey(enchantKey)
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
            CustomAnvil.instance.logger.warning("Group $groupName do not exist but is ask by conflict $conflictName")
            return IncludeGroup("error_placeholder")
        }

        return group
    }

    fun isConflicting(base: Set<WrappedEnchantment>, mat: Material, newEnchant: WrappedEnchantment): ConflictType {
        CustomAnvil.verboseLog("Testing conflict for ${newEnchant.key} on ${mat.key}")
        val conflictList = conflictMap[newEnchant] ?: return ConflictType.NO_CONFLICT
        CustomAnvil.verboseLog("Did not get skipped")

        var result = ConflictType.NO_CONFLICT
        for (conflict in conflictList) {
            CustomAnvil.verboseLog("Is against $conflict")
            val allowed = conflict.allowed(base, mat)
            CustomAnvil.verboseLog("Was against $conflict and conflicting: ${!allowed} ")
            if (!allowed) {
                if (conflict.getEnchants().size <= 1) {
                    result = ConflictType.SMALL_CONFLICT
                    CustomAnvil.verboseLog("Small conflict, continuing")
                } else {
                    CustomAnvil.verboseLog("Big conflict, probably stoping")
                    return ConflictType.BIG_CONFLICT
                }
            }
        }
        return result
    }

}

enum class ConflictType {
    NO_CONFLICT,
    SMALL_CONFLICT,
    BIG_CONFLICT


}