package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.EnchantCostSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static io.delilaheve.util.ConfigOptions.ENCHANT_VALUES_ROOT;

/**
 * Global Config gui for enchantment cost settings.
 */
public class EnchantCostConfigGui extends AbstractEnchantConfigGui<EnchantCostSettingsGui.EnchantCostSettingFactory> {

    private static EnchantCostConfigGui INSTANCE = null;

    @Nullable
    public static EnchantCostConfigGui getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor of this Global gui for enchantment cost settings.
     */
    public EnchantCostConfigGui() {
        super(MsgUI.INSTANCE.getENCHANTMENT_LEVEL_COST_TITLE());
        if (INSTANCE == null) INSTANCE = this;

        init();
    }

    /**
     * Constructor of this Global gui for enchantment cost settings.
     */
    public EnchantCostConfigGui(Gui parent) {
        super(MsgUI.INSTANCE.getENCHANTMENT_LEVEL_COST_TITLE(), parent);
    }

    @Override
    public EnchantCostSettingsGui.EnchantCostSettingFactory createFactory(CAEnchantment enchant) {
        return createFactory(enchant, this);
    }

    public static EnchantCostSettingsGui.EnchantCostSettingFactory createFactory(CAEnchantment enchant, ValueUpdatableGui parent) {
        String key = enchant.getKey().toString().toLowerCase(Locale.ENGLISH);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key.replace(":", "_"));

        return new EnchantCostSettingsGui.EnchantCostSettingFactory(
                MsgUI.INSTANCE.getENCHANTMENT_LEVEL_COST_ELEMENT_TITLE(), parent,
                ENCHANT_VALUES_ROOT + '.' + key, ConfigHolder.DEFAULT_CONFIG,
                MsgUI.INSTANCE.getENCHANTMENT_LEVEL_COST_ELEMENT_DESCRIPTION(), prettyKey,
                enchant, 0, 255,
                1, 10, 50);
    }

    @Override
    public GuiItem itemFromFactory(CAEnchantment enchantment, EnchantCostSettingsGui.EnchantCostSettingFactory factory) {
        // Get item properties
        int itemCost = factory.getConfiguredValue();
        int bookCost = factory.getConfiguredBookValue();
        Message itemName = factory.getTitle();
        // Create item
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;

        // Prepare lore
        List<Component> lore = new ArrayList<>();
        lore.addAll(MsgUI.INSTANCE.getENCHANTMENT_LEVEL_COST_ELEMENT_ITEM_COST().formatted(itemCost));
        lore.addAll(MsgUI.INSTANCE.getENCHANTMENT_LEVEL_COST_ELEMENT_BOOK_COST().formatted(bookCost));

        List<Message> displayLore = factory.getDisplayLore();
        if (displayLore != null) {
            lore.add(Component.empty());
            lore.addAll(ComponentUtil.INSTANCE.asComponents(displayLore, factory.getParam()));
        }

        // Edit name and lore
        ComponentUtil.INSTANCE.setMessageName(itemMeta, itemName, factory.getParam());
        ComponentUtil.INSTANCE.applyLore(lore, itemMeta);

        item.setItemMeta(itemMeta);

        return GuiGlobalItems.openSettingGuiItem(item, factory);
    }

}
