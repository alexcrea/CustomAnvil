package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.settings.EnchantCostSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.UpdateGuiStorage;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ComponentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static io.delilaheve.util.ConfigOptions.ENCHANT_VALUES_ROOT;

/**
 * Global Config gui for enchantment cost settings.
 */
@NotNullByDefault
public class EnchantCostConfigGui extends AbstractEnchantConfigGui<EnchantCostSettingsGui.EnchantCostSettingFactory> {

    public static final UpdateGuiStorage<EnchantCostConfigGui> INSTANCES = new UpdateGuiStorage<>();

    /**
     * Constructor of this Global gui for enchantment cost settings.
     */
    public EnchantCostConfigGui(Supplier<? extends Gui> parent) {
        super(MsgUI.ENCHANTMENT_LEVEL_COST_TITLE, parent);
        INSTANCES.add(this);
    }

    @Override
    public EnchantCostSettingsGui.EnchantCostSettingFactory createFactory(CAEnchantment enchant) {
        return createFactory(enchant, this);
    }

    public static EnchantCostSettingsGui.EnchantCostSettingFactory createFactory(CAEnchantment enchant, ValueUpdatableGui parent) {
        String key = enchant.getKey().toString().toLowerCase(Locale.ENGLISH);
        String prettyKey = CasedStringUtil.snakeToUpperSpacedCase(key.replace(":", "_"));

        return new EnchantCostSettingsGui.EnchantCostSettingFactory(
                MsgUI.ENCHANTMENT_LEVEL_COST_ELEMENT_TITLE, parent,
                ENCHANT_VALUES_ROOT + '.' + key, ConfigHolder.DEFAULT,
                MsgUI.ENCHANTMENT_LEVEL_COST_ELEMENT_DESCRIPTION, prettyKey,
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
        lore.addAll(MsgUI.ENCHANTMENT_LEVEL_COST_ELEMENT_ITEM_COST.formatted(itemCost));
        lore.addAll(MsgUI.ENCHANTMENT_LEVEL_COST_ELEMENT_BOOK_COST.formatted(bookCost));

        List<Message> displayLore = factory.getDisplayLore();
        if (displayLore != null) {
            lore.add(Component.empty());
            lore.addAll(ComponentUtil.asComponents(displayLore, factory.getParam()));
        }

        // Edit name and lore
        ComponentUtil.setMessageName(itemMeta, itemName, factory.getParam());
        ComponentUtil.applyLore(lore, itemMeta);

        item.setItemMeta(itemMeta);

        return GuiGlobalItems.openSettingGuiItem(item, factory);
    }

}
