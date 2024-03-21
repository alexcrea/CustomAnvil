package xyz.alexcrea.cuanvil.gui.config.openable;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.EnchantmentProperties;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.gui.config.AbstractEnchantConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.EnchantCostSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Arrays;
import java.util.Locale;

/**
 * Global Config gui for enchantment cost settings.
 */
public class EnchantCostConfigGui extends AbstractEnchantConfigGui<EnchantCostSettingsGui.EnchantCostSettingFactory> {

    private final static String SECTION_NAME = "enchant_values";

    public final static EnchantCostConfigGui INSTANCE = new EnchantCostConfigGui();

    static {
        INSTANCE.init();
    }

    /**
     * Constructor of this Global gui for enchantment cost settings.
     */
    private EnchantCostConfigGui() {
        super("\u00A78Enchantment Level Limit");

    }

    @Override
    public EnchantCostSettingsGui.EnchantCostSettingFactory getFactoryFromEnchant(Enchantment enchant) {
        String key = enchant.getKey().getKey().toLowerCase(Locale.ENGLISH);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key);

        // try to find rarity. default to 0 if not found
        EnchantmentRarity rarity = EnchantmentRarity.NO_RARITY;
        try {
            rarity = EnchantmentProperties.valueOf(key.toUpperCase(Locale.ENGLISH)).getRarity();
        }catch (IllegalArgumentException ignored){}

        return EnchantCostSettingsGui.enchantCostFactory(prettyKey+" Level Cost", this,
                SECTION_NAME+'.'+key, ConfigHolder.DEFAULT_CONFIG, 0, 255,
                rarity.getItemValue(), rarity.getBookValue(),
                1, 10, 50);
    }

    @Override
    public GuiItem getItemFromFactory(EnchantCostSettingsGui.EnchantCostSettingFactory factory) {
        // Get item properties
        int itemCost = factory.getConfiguredValue();
        int bookCost = factory.getConfiguredBookValue();
        String itemName = "\u00A7a" + factory.getTitle();
        // Create item
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = item.getItemMeta();

        // Edit name and lore
        itemMeta.setDisplayName(itemName);
        itemMeta.setLore(Arrays.asList(
                "\u00A77Item  Cost: " + itemCost,
                "\u00A77Book Cost: " + bookCost));

        item.setItemMeta(itemMeta);

        return GuiGlobalItems.openSettingGuiItem(item, factory);
    }

}
