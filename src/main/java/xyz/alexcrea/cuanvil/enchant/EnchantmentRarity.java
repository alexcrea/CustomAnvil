package xyz.alexcrea.cuanvil.enchant;

// because spigot (1.18) do not look like to provide access to enchantment rarity I need to do it myself...
public class EnchantmentRarity {

    public static final EnchantmentRarity NO_RARITY = new EnchantmentRarity(0, 0);
    public static final EnchantmentRarity COMMON = new EnchantmentRarity(1);
    public static final EnchantmentRarity UNCOMMON = new EnchantmentRarity(2);
    public static final EnchantmentRarity RARE = new EnchantmentRarity(4);
    public static final EnchantmentRarity VERY_RARE = new EnchantmentRarity(8);

    private final int itemValue;
    private final int bookValue;

    public EnchantmentRarity(int itemValue, int bookValue) {
        this.itemValue = itemValue;
        this.bookValue = bookValue;
    }

    public EnchantmentRarity(int itemValue) {
        this(itemValue, Math.max(1, itemValue / 2));
    }

    public final int getBookValue() {
        return bookValue;
    }

    public final int getItemValue() {
        return itemValue;
    }

}
