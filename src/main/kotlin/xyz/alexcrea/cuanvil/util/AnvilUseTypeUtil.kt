package xyz.alexcrea.cuanvil.util

import io.delilaheve.util.ConfigOptions

object AnvilUseTypeUtil {

    /*
     By stupidity of kotlin not allowing static function outside of companion object I need to do another util class only for 1 function:
     Companion object on enum class seems to get initialized AFTER the enum itself
     that mean. if you want to call a static function on the enum class itself YOU CAN'T. you need to do this stupid thing
     or you can make a stupid top level function OR object that even worse because of global scope pollution
     (btw was gpt ""solution"". fortunately I do not "vibe code" so I do what I know instead of stupid AI solution & code)
     I mean, this is still global scope pollution bc of a USELESS class that SHOULD not exist but as a class is better than a random *ss function

     sorry for the rent but this made me frustrated

     Note: I still like a lot of part of kotlin compared to java but this part is one that I hate
     */
    /**
     * Get config path for normal anvil use
     */
    fun defaultPath(typeName: String): String {
            return "${ConfigOptions.WORK_PENALTY_ROOT}.$typeName"
    }

}