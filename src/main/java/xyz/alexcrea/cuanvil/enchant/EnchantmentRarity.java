package xyz.alexcrea.cuanvil.enchant;

// because spigot (1.18) do not support enchantment rarity, I need to do it myself...
public enum EnchantmentRarity {

    NO_RARITY(0, 0),
    COMMON(1),
    UNCOMMON(2),
    RARE(4),
    VERY_RARE(8)

    ;

    private final int itemValue;
    private final int bookValue;

    EnchantmentRarity(int itemValue, int bookValue){
        this.itemValue = itemValue;
        this.bookValue = bookValue;
    }
    EnchantmentRarity(int itemValue){
        this(itemValue, Math.max(1,itemValue/2));
    }

    public int getBookValue() {
        return bookValue;
    }

    public int getItemValue() {
        return itemValue;
    }

}
