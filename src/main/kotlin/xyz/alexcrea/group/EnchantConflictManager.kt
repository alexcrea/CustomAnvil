package xyz.alexcrea.group

import io.delilaheve.UnsafeEnchants
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import kotlin.collections.ArrayList

class EnchantConflictManager {

    companion object {
        // Path for the enchantments list
        private const val ENCH_LIST_PATH = "enchantments"
        // Path for group list related to the conflict
        private const val CONFLICT_GROUP_PATH = "notAffectedGroups"
        // Path for the maximum number of enchantment before validating the conflict
        private const val ENCH_MAX_PATH = "maxEnchantmentBeforeConflict"
        // Path for a flag: if the enchantment will be used in the last supported version
        // TODO maybe replace this system by a list of "future" enchantment.
        private const val FUTURE_USE_PATH = "useInFuture"
        // Default name for an empty Material group
        private val DEFAULT_EMPTY_GROUP = IncludeGroup("empty")
        // Default name for a joining group
        private const val DEFAULT_GROUP_NAME = "joinedGroup"
    }

    private lateinit var conflictMap: HashMap<Enchantment, ArrayList<EnchantConflictGroup>>

    // Read and prepare all conflict
    fun prepareConflicts(config: YamlConfiguration, itemManager: ItemGroupManager){
        conflictMap = HashMap()

        val keys = config.getKeys(false)
        for (key in keys) {
            val section = config.getConfigurationSection(key)!!
            val conflict = createConflict(section,itemManager,key)
            if(conflict != null){
                addToMap(conflict)
            }

        }

    }

    // Add the conflict to the map
    private fun addToMap(conflict: EnchantConflictGroup){
        conflict.getEnchants().forEach{ enchant ->
            if(!conflictMap.containsKey(enchant)){
                conflictMap[enchant] = ArrayList()
            }
            conflictMap[enchant]!!.add(conflict)
        }
    }

    // create and read a conflict from a yaml section
    private fun createConflict(section: ConfigurationSection,
                               itemManager: ItemGroupManager,
                               conflictName: String): EnchantConflictGroup? {
        // Is it planed for the future
        val futureUse = section.getBoolean(FUTURE_USE_PATH,false)
        // Create conflict
        val conflict = createConflictObject(section,itemManager,conflictName)
        // Read and add enchantment to conflict
        val enchantList = section.getStringList(ENCH_LIST_PATH)
        for (enchantName in enchantList) {
            val enchantKey = NamespacedKey.minecraft(enchantName)
            val enchant = Enchantment.getByKey(enchantKey)
            if(enchant == null){
                if(!futureUse){
                    UnsafeEnchants.instance.logger.warning("Enchantment $enchantName do not exist but was asked for conflict $conflictName")
                }
                continue
            }
            conflict.addEnchantment(enchant)
        }
        if(conflict.getEnchants().size == 0){
            if(!futureUse){
                UnsafeEnchants.instance.logger.warning("Conflict $conflictName do not have valid enchantment, it will not work")
            }
            return null
        }

        return conflict
    }

    private fun createConflictObject(section: ConfigurationSection,
                               itemManager: ItemGroupManager,
                               conflictName: String): EnchantConflictGroup {
        // Get the maximum number of enchantment before validating the conflict
        var minBeforeBlock = section.getInt(ENCH_MAX_PATH,0)
        if(minBeforeBlock < 0){
            minBeforeBlock = 0
            UnsafeEnchants.instance.logger.warning("Conflict $conflictName have an invalid value of $ENCH_MAX_PATH")
            UnsafeEnchants.instance.logger.warning("It should be more or equal to 0. default to 0")
        }
        // Find or create the selected group for the conflict
        val groupList = section.getStringList(CONFLICT_GROUP_PATH)
        val finalGroup: AbstractMaterialGroup
        if(groupList.size < 1){
            finalGroup = DEFAULT_EMPTY_GROUP
        }else if(groupList.size == 1){
            finalGroup = findGroup(groupList[0], itemManager, conflictName)
        }else{
            finalGroup = IncludeGroup(DEFAULT_GROUP_NAME)
            for (groupName in groupList) {
                finalGroup.addToPolicy(findGroup(groupName, itemManager, conflictName))
            }
        }
        // Return conflict
        return EnchantConflictGroup(finalGroup, minBeforeBlock)
    }

    private fun findGroup(groupName: String,itemManager: ItemGroupManager, conflictName: String): AbstractMaterialGroup {
        val group = itemManager.get(groupName)
        if(group == null){
            UnsafeEnchants.instance.logger.warning("Group $groupName do not exist but is ask by conflict $conflictName")
            return DEFAULT_EMPTY_GROUP
        }

        return group
    }

    fun isConflicting(base: Set<Enchantment>,mat: Material, newEnchant: Enchantment): ConflictType{
        val conflictList = conflictMap[newEnchant] ?: return ConflictType.NO_CONFLICT

        var result = ConflictType.NO_CONFLICT
        for (conflict in conflictList) {
            if(!conflict.allowed(base,mat)) {
                if(conflict.getEnchants().size <= 1){
                    result = ConflictType.SMALL_CONFLICT
                }else{
                    return ConflictType.BIG_CONFLICT
                }
            }
        }
        return result
    }

}

enum class ConflictType(){
    NO_CONFLICT,
    SMALL_CONFLICT,
    BIG_CONFLICT


}