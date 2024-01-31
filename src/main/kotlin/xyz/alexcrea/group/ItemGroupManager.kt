package xyz.alexcrea.group

import io.delilaheve.UnsafeEnchants
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.util.*
import kotlin.collections.HashMap

class ItemGroupManager {

    companion object {
        // Path for group type
        private const val GROUP_TYPE_PATH = "type"
        private const val MATERIAL_LIST_PATH = "items"
        private const val GROUP_LIST_PATH = "groups"
    }

    private lateinit var groupMap : HashMap<String,MaterialGroup>

    // Read and create material groups
    fun prepareGroups(config: YamlConfiguration){
        groupMap = HashMap()

        val keys = config.getKeys(false)
        for (key in keys) {
            if(groupMap.containsKey(key))
                continue
            createGroup(config, keys, key)
        }
    }

    // Create group by key
    private fun createGroup(config: YamlConfiguration,
                            keys: Set<String>,
                            key: String): MaterialGroup{
        val groupSection = config.getConfigurationSection(key)!!
        val groupType = groupSection.getString(GROUP_TYPE_PATH,null)

        // Create Material group according to the group type
        val group: MaterialGroup
        if(GroupType.EXCLUDE.equal(groupType)){
            group = ExcludeGroup(key)
        }else {
            group = IncludeGroup(key)
            if(!GroupType.INCLUDE.equal(groupType)){
                UnsafeEnchants.instance.logger.warning("Group $key have an invalid group type. default to Include.")
            }
        }

        groupMap[key] = group
        readGroup(group, groupSection, config, keys)
        return group
    }

    // Read Group elements
    private fun readGroup(group: MaterialGroup,
                          groupSection: ConfigurationSection,
                          config: YamlConfiguration,
                          keys: Set<String>){
        // Read material to include in this group policy
        val materialList = groupSection.getStringList(MATERIAL_LIST_PATH)
        for (materialTemp in materialList) {
            val materialName = materialTemp.uppercase(Locale.getDefault())
            val material = Material.getMaterial(materialName)
            if(material == null){
                UnsafeEnchants.instance.logger.warning(
                    "Unknown material $materialTemp on group ${group.getName()}")
                continue
            }
            group.addToPolicy(material)
        }

        // Read group to include in this group policy.
        // please note the group name is case-sensitive.
        val groupList = groupSection.getStringList(GROUP_LIST_PATH)
        for (groupName in groupList) {
            if(groupName !in keys){
                UnsafeEnchants.instance.logger.warning(
                    "Group $groupName do not exist but is included in group ${group.getName()}")
                continue
            }
            // Get other group or create it if not yet created
            val otherGroup = if(!groupMap.containsKey(groupName)){
                createGroup(config,keys,groupName)
            }else{
                groupMap[groupName]!!
            }
            // Avoid self reference or it will create an infinite loop
            if(otherGroup.isReferencing(group)){
                UnsafeEnchants.instance.logger.warning(
                    "Group $groupName is on a reference loop with group ${group.getName()} !")
                UnsafeEnchants.instance.logger.warning(
                    "Please fix it in your item_groups config or the plugin will probably not work as expected.")
                continue
            }

            group.addToPolicy(otherGroup)
        }

    }

}

enum class GroupType(private val groupID: String) {

    INCLUDE("include"),
    EXCLUDE("exclude")
    ;

    // Test if string is equal to the groupID of this enum
    fun equal(toTest: String?): Boolean{
        if(toTest == null)
            return false
        return  groupID.contentEquals(toTest.lowercase(Locale.getDefault()))
    }

}