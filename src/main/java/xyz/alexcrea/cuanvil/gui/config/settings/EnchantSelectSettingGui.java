package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.AbstractEnchantConfigGui;
import xyz.alexcrea.cuanvil.gui.config.SelectEnchantmentContainer;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class EnchantSelectSettingGui extends AbstractSettingGui{

    SelectEnchantmentContainer enchantContainer;
    int page;

    Set<Enchantment> selectedEnchant;

    public EnchantSelectSettingGui(@NotNull String title, ValueUpdatableGui parent, SelectEnchantmentContainer enchantContainer, int page) {
        super(6, title, parent);
        this.enchantContainer = enchantContainer;
        // Not used and not planned rn
        this.page = page;

        this.selectedEnchant = new HashSet<>(enchantContainer.getSelectedEnchantments());

        initGroups();
    }

    @Override
    protected Pattern getGuiPattern() {
        return new Pattern(
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                "B1111111S"
        );
    }

    protected void initGroups(){
        // Add enchantment gui item
        OutlinePane filledEnchant = new OutlinePane(0, 0, 9, 5);
        filledEnchant.setPriority(Pane.Priority.HIGH);
        filledEnchant.align(OutlinePane.Alignment.BEGIN);
        filledEnchant.setOrientation(Orientable.Orientation.HORIZONTAL);

        Set<Enchantment> illegalEnchant = this.enchantContainer.illegalEnchantments();
        for (Enchantment enchant : AbstractEnchantConfigGui.SORTED_ENCHANTMENT_LIST) {
            if(illegalEnchant.contains(enchant)) {
                return;
            }
            filledEnchant.addItem(getGuiItemFromEnchant(enchant));
        }

        addPane(filledEnchant);

    }

    private GuiItem getGuiItemFromEnchant(Enchantment enchantment){
        boolean isIn = this.selectedEnchant.contains(enchantment);

        Material usedMaterial;
        if(isIn){
            usedMaterial = Material.ENCHANTED_BOOK;
        }else{
            usedMaterial = Material.BOOK;
        }
        ItemStack item = new ItemStack(usedMaterial);

        setEnchantItemMeta(item, enchantment.getKey().getKey(), isIn);

        GuiItem guiItem = new GuiItem(item, CustomAnvil.instance);
        guiItem.setAction(getEnchantItemConsumer(enchantment, guiItem));
        return guiItem;
    }


    private static final List<String> TRUE_LORE = Collections.singletonList("\u00A77Value: \u00A7aSelected");
    private static final List<String> FALSE_LORE = Collections.singletonList("\u00A77Value: \u00A7cNot Selected");

    public void setEnchantItemMeta(ItemStack item, String name, boolean isIn){
        ItemMeta meta = item.getItemMeta();

        if(meta == null){
            CustomAnvil.instance.getLogger().warning("Could not create item for enchantment: "+name+":\n" +
                    "Item do not gave item meta: "+item+". Using placeholder instead");
            item.setType(Material.PAPER);
            meta = item.getItemMeta();
            assert meta != null;
        }

        meta.setDisplayName("\u00A7"+(isIn ? 'a' : 'c')+ CasedStringUtil.snakeToUpperSpacedCase(name));
        if(isIn){
            meta.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true);
            meta.setLore(TRUE_LORE);
        }else{
            meta.removeEnchant(Enchantment.DAMAGE_UNDEAD);
            meta.setLore(FALSE_LORE);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
    }

    private Consumer<InventoryClickEvent> getEnchantItemConsumer(Enchantment enchant, GuiItem guiItem){
        return event -> {
            event.setCancelled(true);

            ItemStack item = guiItem.getItem();

            boolean isIn = this.selectedEnchant.contains(enchant);
            if(isIn){
                this.selectedEnchant.remove(enchant);
                item.setType(Material.BOOK);
            }else{
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
        Set<Enchantment> baseGroup = this.enchantContainer.getSelectedEnchantments();
        return baseGroup.size() != this.selectedEnchant.size() ||
                !baseGroup.containsAll(this.selectedEnchant);
    }

}
