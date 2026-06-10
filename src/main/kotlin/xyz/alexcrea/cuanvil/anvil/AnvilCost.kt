package xyz.alexcrea.cuanvil.anvil

import java.math.BigDecimal
import io.delilaheve.util.ConfigOptions.getMonetaryMultiplier as moneyMultiplier

open class AnvilCost {
    private val isAlone: Boolean
    var valid = true // Get set as invalid if cost can be satisfied
    var isMonetary = false

    var generic = 0
    var enchantment = 0
    var repair = 0
    var rename = 0
    var lore = 0
    var illegalPenalty = 0
    var workPenalty = 0
    var recipe = 0

    constructor(generic: Int) {
        this.generic = generic
        isAlone = true
    }

    constructor() {
        isAlone = false
    }

    fun asXpCost(): Int {
        return generic + enchantment + repair + rename + lore + illegalPenalty + workPenalty + recipe
    }

    open fun asMonetaryCost(): BigDecimal {
        // multiply by per use type multipliers
        return BigDecimal(generic)
            .add(BigDecimal(enchantment).multiply(moneyMultiplier("enchantment")))
            .add(BigDecimal(repair).multiply(moneyMultiplier("repair")))
            .add(BigDecimal(rename).multiply(moneyMultiplier("rename")))
            .add(BigDecimal(lore).multiply(moneyMultiplier("lore_edit")))
            .add(BigDecimal(enchantment).multiply(moneyMultiplier("enchantment")))
            .add(BigDecimal(illegalPenalty).multiply(moneyMultiplier("work_penalty")))
            .add(BigDecimal(workPenalty).multiply(moneyMultiplier("work_penalty")))
            .add(BigDecimal(recipe).multiply(moneyMultiplier("recipe")))
            .multiply(moneyMultiplier("global"))
    }
}

class CustomCraftCost(val rawCost: Int): AnvilCost() {

    override fun asMonetaryCost(): BigDecimal {
        return BigDecimal(rawCost)
            .multiply(moneyMultiplier("global"))
    }

}