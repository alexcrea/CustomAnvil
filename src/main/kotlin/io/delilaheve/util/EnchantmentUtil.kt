package io.delilaheve.util

import io.delilaheve.CustomAnvil
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import xyz.alexcrea.cuanvil.config.ConfigHolder
import xyz.alexcrea.cuanvil.group.ConflictType
import kotlin.math.max
import kotlin.math.min

/**
 * Enchantment manipulation utilities
 */
object EnchantmentUtil {

    /**
     * Enchantment name without namespace
     */
    val Enchantment.enchantmentName: String
        get() = key.key

    /**
     * Combine 2 sets of enchantments according to our configuration
     */
    fun Map<Enchantment, Int>.combineWith(
        other: Map<Enchantment, Int>,
        mat: Material,
        player: HumanEntity
    ) = mutableMapOf<Enchantment, Int>().apply {
        putAll(this@combineWith)
        other.forEach { (enchantment, level) ->
            // Enchantment not yet in result list
            if (!containsKey(enchantment)) {
                // Add the enchantment if it doesn't have conflicts, or, if player is allowed to bypass enchantment restrictions
                this[enchantment] = level
                if(!player.hasPermission(CustomAnvil.bypassFusePermission) &&
                    (ConfigHolder.CONFLICT_HOLDER.conflictManager.isConflicting(this.keys,mat,enchantment) != ConflictType.NO_CONFLICT)){
                    this.remove(enchantment)
                }

            }
            // Enchantment already in result list
            else{
                // ... and they are conflicting
                if((ConfigHolder.CONFLICT_HOLDER.conflictManager.isConflicting(this.keys,mat,enchantment) != ConflictType.NO_CONFLICT)
                    && !player.hasPermission(CustomAnvil.bypassFusePermission)){
                    return@forEach
                }

                // ... and they're not the same level
                if(this[enchantment] != other[enchantment]){
                    val newLevel = max(this[enchantment] ?: 0, other[enchantment] ?: 0)
                    // apply the greater of the two if non-zero
                    if (newLevel > 0) { this[enchantment] = newLevel }
                }
                // ... and they're the same level
                else {
                    // try to increase the enchantment level by 1
                    var newLevel = this[enchantment]!! +1
                    // Get max level or 255 if player can bypass
                    val maxLevel = if(player.hasPermission(CustomAnvil.bypassLevelPermission)){
                        255
                    }else{
                        ConfigOptions.enchantLimit(enchantment)
                    }
                    newLevel = min(newLevel, maxLevel)
                    if (newLevel > 0) { this[enchantment] = newLevel }
                }
            }
        }
    }

}
