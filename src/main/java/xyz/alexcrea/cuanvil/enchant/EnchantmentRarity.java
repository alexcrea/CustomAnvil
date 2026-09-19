package xyz.alexcrea.cuanvil.enchant;

import org.jetbrains.annotations.NotNullByDefault;

// because spigot (1.21) do not look like to provide access to enchantment rarity I need to do it myself...
//TODO in 2035 when Valhalla got released,
// then minecraft use a java compatible,
// then my plugin catch up minimum version to the one mentioned above,
// then finally, finally, can use value record...
@SuppressWarnings("unused")
@NotNullByDefault
public record EnchantmentRarity(int itemValue, int bookValue) {

    public static final EnchantmentRarity NO_RARITY = new EnchantmentRarity(0, 0);
    public static final EnchantmentRarity COMMON = new EnchantmentRarity(1);
    public static final EnchantmentRarity UNCOMMON = new EnchantmentRarity(2);
    public static final EnchantmentRarity RARE = new EnchantmentRarity(4);
    public static final EnchantmentRarity VERY_RARE = new EnchantmentRarity(8);

    public EnchantmentRarity(int itemValue) {
        this(itemValue, Math.max(1, itemValue / 2));
    }

}
