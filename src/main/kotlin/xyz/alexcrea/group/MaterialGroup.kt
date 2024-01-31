package xyz.alexcrea.group

import org.bukkit.Material

interface MaterialGroup {

    // Get if a material is allowed following the group policy
    fun contain(mat : Material): Boolean

    // Get if a group is referenced by this
    fun isReferencing(other : MaterialGroup): Boolean

    // Push a material to this group to follow this group policy
    fun addToPolicy(mat : Material)

    // Push a group to this group to follow this group policy
    fun addToPolicy(other : MaterialGroup)

    // Get the group name in case something is wrong
    fun getName(): String

}