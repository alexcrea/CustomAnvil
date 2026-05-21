package xyz.alexcrea.cuanvil.util

import org.bukkit.inventory.meta.Damageable

// I LOVE support of old versions and needing to do modules like that
// That truly is my favorite activity
// TODO clean this one of legacy removal branch
object MaxDamageCheckerUtil {

    /**
     * @return max damage or int max if not set
      */
    fun getMaxDamage(meta: Damageable): Int {
        if(!meta.hasMaxDamage()) return Integer.MAX_VALUE
        return meta.maxDamage
    }

}