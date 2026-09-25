package xyz.alexcrea.cuanvil.enchant.wrapped;

import io.delilaheve.CustomAnvil;
import me.sciguymjm.uberenchant.api.UberEnchantment;
import me.sciguymjm.uberenchant.api.utils.Rarity;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentBase;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

@NotNullByDefault
public class CAUberEnchantment extends CAEnchantmentBase {

    private final UberEnchantment enchant;

    public CAUberEnchantment(UberEnchantment enchant) {
        super(enchant.getKey(), rarityFromUber(enchant.getRarity()), enchant.getMaxLevel());

        this.enchant = enchant;
    }

    private static EnchantmentRarity rarityFromUber(Rarity rarity) {
        return switch(rarity) {
            case COMMON -> EnchantmentRarity.COMMON;
            case UNCOMMON -> EnchantmentRarity.UNCOMMON;
            case RARE -> EnchantmentRarity.RARE;
            case VERY_RARE -> EnchantmentRarity.VERY_RARE;
        };
    }

    @Override
    public int getLevel(ItemStack item, ItemMeta meta) {
        return enchant.getLevel(item);
    }

    @Override
    public boolean isEnchantmentPresent(ItemStack item, ItemMeta meta) {
        return enchant.containsEnchantment(item);
    }

    @Override
    public void addEnchantmentUnsafe(ItemStack item, int level) {
        var time = System.currentTimeMillis();

        if (item.getType().equals(Material.ENCHANTED_BOOK))
            UberUtils.addStoredEnchantment(enchant, item, level);
        else
            UberUtils.addEnchantment(enchant, item, level);
        var end = System.currentTimeMillis();

        CustomAnvil.log("time: " + (end - time));
        if((end - time) > 20)
            CustomAnvil.log("long time");
    }

    @Override
    public void removeFrom(ItemStack item) {
        UberUtils.removeStoredEnchantment(enchant, item);
        UberUtils.removeEnchantment(enchant, item);
    }

}
