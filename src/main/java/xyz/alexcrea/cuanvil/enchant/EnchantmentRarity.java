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

    private EnchantmentRarity(int itemValue, int bookValue) {
        this.itemValue = itemValue;
        this.bookValue = bookValue;
    }

    private EnchantmentRarity(int itemValue) {
        this(itemValue, Math.max(1, itemValue / 2));
    }

    public final int getBookValue() {
        return bookValue;
    }

    public final int getItemValue() {
        return itemValue;
    }


    public static EnchantmentRarity getRarity(int itemValue, int bookValue){
        int expectedBook = Math.max(1, itemValue / 2);
        if((expectedBook == bookValue) && (itemValue != 0)) return getRarity(itemValue);

        if(itemValue == 0 && bookValue == 0) return NO_RARITY;
        return new EnchantmentRarity(itemValue, bookValue);
    }

    public static EnchantmentRarity getRarity(int itemValue){
        return switch (itemValue) {
            case 0 -> NO_RARITY;
            case 1 -> COMMON;
            case 2 -> UNCOMMON;
            case 4 -> RARE;
            case 8 -> VERY_RARE;
            default -> new EnchantmentRarity(itemValue);
        };
    }

}
