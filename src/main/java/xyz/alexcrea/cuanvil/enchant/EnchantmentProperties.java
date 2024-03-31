package xyz.alexcrea.cuanvil.enchant;

// to bind EnchantmentRarity to an enchantment...
public enum EnchantmentProperties {

    AQUA_AFFINITY(EnchantmentRarity.RARE),
    BANE_OF_ARTHROPODS(EnchantmentRarity.UNCOMMON),
    BINDING_CURSE(EnchantmentRarity.VERY_RARE),
    BLAST_PROTECTION(EnchantmentRarity.RARE),
    CHANNELING(EnchantmentRarity.VERY_RARE),
    DEPTH_STRIDER(EnchantmentRarity.RARE),
    EFFICIENCY(EnchantmentRarity.COMMON),
    FLAME(EnchantmentRarity.RARE),
    FEATHER_FALLING(EnchantmentRarity.UNCOMMON),
    FIRE_ASPECT(EnchantmentRarity.RARE),
    FIRE_PROTECTION(EnchantmentRarity.UNCOMMON),
    FORTUNE(EnchantmentRarity.RARE),
    FROST_WALKER(EnchantmentRarity.RARE),
    IMPALING(EnchantmentRarity.RARE),
    INFINITY(EnchantmentRarity.VERY_RARE),
    KNOCKBACK(EnchantmentRarity.UNCOMMON),
    LOOTING(EnchantmentRarity.RARE),
    LOYALTY(EnchantmentRarity.COMMON),
    LUCK_OF_THE_SEA(EnchantmentRarity.RARE),
    LURE(EnchantmentRarity.RARE),
    MENDING(EnchantmentRarity.RARE),
    MULTISHOT(EnchantmentRarity.RARE),
    PIERCING(EnchantmentRarity.COMMON),
    POWER(EnchantmentRarity.COMMON),
    PROJECTILE_PROTECTION(EnchantmentRarity.UNCOMMON),
    PROTECTION(EnchantmentRarity.COMMON),
    PUNCH(EnchantmentRarity.RARE),
    QUICK_CHARGE(EnchantmentRarity.UNCOMMON),
    RESPIRATION(EnchantmentRarity.RARE),
    RIPTIDE(EnchantmentRarity.RARE),
    SILK_TOUCH(EnchantmentRarity.VERY_RARE),
    SHARPNESS(EnchantmentRarity.COMMON),
    SMITE(EnchantmentRarity.UNCOMMON),
    SOUL_SPEED(EnchantmentRarity.VERY_RARE),
    SWIFT_SNEAK(EnchantmentRarity.VERY_RARE),
    SWEEPING(EnchantmentRarity.RARE),
    THORNS(EnchantmentRarity.VERY_RARE),
    UNBREAKING(EnchantmentRarity.UNCOMMON),
    VANISHING_CURSE(EnchantmentRarity.VERY_RARE);

    private final EnchantmentRarity rarity;

    EnchantmentProperties(EnchantmentRarity rarity) {
        this.rarity = rarity;
    }

    public EnchantmentRarity getRarity() {
        return rarity;
    }

}
