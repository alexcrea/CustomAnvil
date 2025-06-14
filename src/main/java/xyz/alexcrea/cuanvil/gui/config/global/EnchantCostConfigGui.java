package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.gui.config.settings.EnchantCostSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Global Config gui for enchantment cost settings.
 */
public class EnchantCostConfigGui extends AbstractEnchantConfigGui<EnchantCostSettingsGui.EnchantCostSettingFactory> {

    private static final String SECTION_NAME = "enchant_values";

    private static EnchantCostConfigGui INSTANCE = null;

    @Nullable
    public static EnchantCostConfigGui getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor of this Global gui for enchantment cost settings.
     */
    public EnchantCostConfigGui() {
        super("§8Enchantment Level Cost");
        if (INSTANCE == null) INSTANCE = this;

        init();
    }

    @Override
    public EnchantCostSettingsGui.EnchantCostSettingFactory createFactory(CAEnchantment enchant) {
        String key = enchant.getKey().toString().toLowerCase(Locale.ENGLISH);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key.replace(":", "_"));

        return new EnchantCostSettingsGui.EnchantCostSettingFactory(prettyKey + " Cost", this,
                SECTION_NAME + '.' + key, ConfigHolder.DEFAULT_CONFIG,
                Arrays.asList(
                        "§7How many level should " + prettyKey,
                        "§7cost when applied by book or by another item."
                ),
                enchant, 0, 255,
                1, 10, 50);
    }

    @Override
    public GuiItem itemFromFactory(CAEnchantment enchantment, EnchantCostSettingsGui.EnchantCostSettingFactory factory) {
        // Get item properties
        int itemCost = factory.getConfiguredValue();
        int bookCost = factory.getConfiguredBookValue();
        String itemName = "§a" + factory.getTitle();
        // Create item
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;

        // Prepare lore
        List<String> lore = new ArrayList<>();
        lore.add("§7Item  Cost: §e" + itemCost);
        lore.add("§7Book Cost: §e" + bookCost);

        List<String> displayLore = factory.getDisplayLore();
        if (!displayLore.isEmpty()) {
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
