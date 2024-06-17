package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.EnchantmentProperties;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;
import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment;
import xyz.alexcrea.cuanvil.gui.config.settings.EnchantCostSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;

/**
 * Global Config gui for enchantment cost settings.
 */
public class EnchantCostConfigGui extends AbstractEnchantConfigGui<EnchantCostSettingsGui.EnchantCostSettingFactory> {

    private static final String SECTION_NAME = "enchant_values";

    public static final EnchantCostConfigGui INSTANCE = new EnchantCostConfigGui();

    static {
        INSTANCE.init();
    }

    /**
     * Constructor of this Global gui for enchantment cost settings.
     */
    private EnchantCostConfigGui() {
        super("\u00A78Enchantment Level Cost");

    }

    @Override
    public EnchantCostSettingsGui.EnchantCostSettingFactory createFactory(WrappedEnchantment enchant) {
        String key = enchant.getKey().getKey().toLowerCase(Locale.ENGLISH);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key);

        // try to find rarity. default to 0 if not found
        EnchantmentRarity rarity = enchant.defaultRarity();
        try {
            rarity = EnchantmentProperties.valueOf(key.toUpperCase(Locale.ENGLISH)).getRarity();
        } catch (IllegalArgumentException ignored) {
        }

        return EnchantCostSettingsGui.enchantCostFactory(prettyKey + " Level Cost", this,
                ConfigHolder.DEFAULT_CONFIG, SECTION_NAME + '.' + key,
                Arrays.asList(
                        "\u00A77How many level should " + prettyKey,
                        "\u00A77cost when applied by book or by another item."
                ),
                0, 255,
                rarity.getItemValue(), rarity.getBookValue(),
                1, 10, 50);
    }

    @Override
    public GuiItem itemFromFactory(WrappedEnchantment enchantment, EnchantCostSettingsGui.EnchantCostSettingFactory factory) {
        // Get item properties
        int itemCost = factory.getConfiguredValue();
        int bookCost = factory.getConfiguredBookValue();
        String itemName = "\u00A7a" + factory.getTitle();
        // Create item
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;

        // Prepare lore
        List<String> lore = new ArrayList<>();
        lore.add("\u00A77Item  Cost: \u00A7e" + itemCost);
        lore.add("\u00A77Book Cost: \u00A7e" + bookCost);

        List<String> displayLore = factory.getDisplayLore();
        if(!displayLore.isEmpty()){
            lore.add("");
            lore.addAll(displayLore);
        }

        // Edit name and lore
        itemMeta.setDisplayName(itemName);
        itemMeta.setLore(lore);

        item.setItemMeta(itemMeta);

        return GuiGlobalItems.openSettingGuiItem(item, factory);
    }

}
