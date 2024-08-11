package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.SelectEnchantmentContainer;
import xyz.alexcrea.cuanvil.gui.config.list.SettingGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EnchantSelectSettingGui extends SettingGuiListConfigGui<CAEnchantment, EnchantSelectSettingGui.DummyFactory> implements SettingGui {

    private final SelectEnchantmentContainer enchantContainer;

    private final Set<CAEnchantment> selectedEnchant;
    private final GuiItem saveItem;

    private boolean displayUnselected;

    public EnchantSelectSettingGui(@NotNull String title, ValueUpdatableGui parent, SelectEnchantmentContainer enchantContainer) {
        super(title);
        this.enchantContainer = enchantContainer;

        this.selectedEnchant = new HashSet<>(enchantContainer.getSelectedEnchantments());

        this.saveItem = GuiGlobalItems.saveItem(this, parent);
        this.backgroundPane.bindItem('S',  GuiGlobalItems.noChangeItem());

        this.displayUnselected = true;
        this.backgroundPane.bindItem('b',  createDisplayUnusedItem());

        init();
    }

    @Override
    protected Pattern getBackgroundPattern() {
        return new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "B11LbR11S"
        );
    }

    @Override
    protected Collection<CAEnchantment> getEveryDisplayableInstanceOfGeneric() {
        Stream<CAEnchantment> toDisplayStream;
        if(this.displayUnselected){
            toDisplayStream = CAEnchantmentRegistry.getInstance().getNameSortedEnchantments().stream();
        }else{
            toDisplayStream = this.selectedEnchant.stream().sorted(Comparator.comparing(CAEnchantment::getName));
        }
        Set<CAEnchantment> illegalEnchantments = this.enchantContainer.illegalEnchantments();


        return toDisplayStream
                .filter(enchantment -> !illegalEnchantments.contains(enchantment))
                .toList();
    }

    @Override
    public void update() {
        this.backgroundPane.bindItem('S', hadChange() ? saveItem : GuiGlobalItems.noChangeItem());
        super.update();
    }

    @Override
    protected GuiItem itemFromFactory(CAEnchantment enchantment, DummyFactory factory) {
        boolean isIn = this.selectedEnchant.contains(enchantment);

        Material usedMaterial;
        if (isIn) {
            usedMaterial = Material.ENCHANTED_BOOK;
        } else {
            usedMaterial = Material.BOOK;
        }
        ItemStack item = new ItemStack(usedMaterial);

        setEnchantItemMeta(item, enchantment.getKey().getKey(), isIn);

        GuiItem guiItem = new GuiItem(item, CustomAnvil.instance);
        guiItem.setAction(getEnchantItemConsumer(enchantment, guiItem));
        return guiItem;
    }

    private GuiItem createDisplayUnusedItem() {
        ItemStack item = new ItemStack(this.displayUnselected ? Material.BOOK : Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName((this.displayUnselected ? "§aEverything displayed" : "§eOnly selected displayed"));
        meta.setLore(Collections.singletonList(
                        "§7Click here to see " +
                        (this.displayUnselected ? "only selected" : "every") +
                                " enchantments"));

        item.setItemMeta(meta);

        return new GuiItem(item, clickEvent -> {
            clickEvent.setCancelled(true);
            this.displayUnselected = !this.displayUnselected;

            this.backgroundPane.bindItem('b',  createDisplayUnusedItem());
            reloadValues();
        }, CustomAnvil.instance);
    }

    private static final List<String> TRUE_LORE = Collections.singletonList("§7Value: §aSelected");
    private static final List<String> FALSE_LORE = Collections.singletonList("§7Value: §cNot Selected");

    public void setEnchantItemMeta(ItemStack item, String name, boolean isIn) {
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            CustomAnvil.instance.getLogger().warning("Could not create item for enchantment: " + name + ":\n" +
                    "Item do not gave item meta: " + item + ". Using a placeholder item instead");
            item.setType(Material.PAPER);
            meta = item.getItemMeta();
            assert meta != null;
        }

        meta.setDisplayName("§" + (isIn ? 'a' : 'c') + CasedStringUtil.snakeToUpperSpacedCase(name));
        if (isIn) {
            meta.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true);
            meta.setLore(TRUE_LORE);
        } else {
            meta.removeEnchant(Enchantment.DAMAGE_UNDEAD);
            meta.setLore(FALSE_LORE);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
    }

    private Consumer<InventoryClickEvent> getEnchantItemConsumer(CAEnchantment enchant, GuiItem guiItem) {
        return event -> {
            event.setCancelled(true);

            ItemStack item = guiItem.getItem();

            boolean isIn = this.selectedEnchant.contains(enchant);
            if (isIn) {
                this.selectedEnchant.remove(enchant);
                item.setType(Material.BOOK);
            } else {
                this.selectedEnchant.add(enchant);
                item.setType(Material.ENCHANTED_BOOK);
            }

            setEnchantItemMeta(item, enchant.getKey().getKey(), !isIn);
            guiItem.setItem(item);// Just in case

            update();
        };
    }

    @Override
    public boolean onSave() {
        return this.enchantContainer.setSelectedEnchantments(this.selectedEnchant);
    }

    @Override
    public boolean hadChange() {
        Set<CAEnchantment> baseGroup = this.enchantContainer.getSelectedEnchantments();
        return baseGroup.size() != this.selectedEnchant.size() ||
                !baseGroup.containsAll(this.selectedEnchant);
    }


    // Unused methods and class
    public static class DummyFactory extends AbstractSettingGui.SettingGuiFactory{
        protected DummyFactory(@NotNull String configPath, @NotNull ConfigHolder config) {
            super(configPath, config);
        }
        @Override
        public Gui create() {
            return null;
        }
    }
    @Override
    protected List<String> getCreateItemLore() {
        return Collections.emptyList();
    }
    @Override
    protected Consumer<InventoryClickEvent> getCreateClickConsumer() {
        return null;
    }
    @Override
    protected String createItemName() {
        return null;
    }
    @Override
    protected DummyFactory createFactory(CAEnchantment generic) {
        return null;
    }

}
