package xyz.alexcrea.cuanvil.util.anvil

import io.delilaheve.util.ConfigOptions

object AnvilUseTypeUtil {

    /*
     By kotlin not having static function outside of companion object I need to do another util class only for 1 function:
     Companion object on enum class seems to get initialized AFTER the enum itself
     that mean. if you want to call a static function on enum construction YOU CAN'T. you need to do this stupid thing,
     or you can make a stupid top level function or object that at least as bad as this
     I mean, this is still an unnecessary class that should not exist but as a class is better than a random function in my opinion
     */
    /**
     * Get config path for normal anvil use
     */
    fun defaultPath(typeName: String): String {
            return "${ConfigOptions.WORK_PENALTY_ROOT}.$typeName"
    }

}