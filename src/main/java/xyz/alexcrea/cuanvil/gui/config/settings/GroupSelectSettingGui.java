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
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.SelectGroupContainer;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GroupSelectSettingGui extends AbstractSettingGui {

    SelectGroupContainer groupContainer;
    int page;

    Set<AbstractMaterialGroup> selectedGroups;

    public GroupSelectSettingGui(@NotNull String title, ValueUpdatableGui parent, SelectGroupContainer groupContainer, int page) {
        super(6, title, parent);
        this.groupContainer = groupContainer;
        //Not used but planned
        this.page = page;

        this.selectedGroups = new HashSet<>(groupContainer.getSelectedGroups());

        // Add secondary background item
        this.getPane().bindItem('1', GuiSharedConstant.SECONDARY_BACKGROUND_ITEM);

        initGroups();
    }

    @Override
    protected Pattern getGuiPattern() {
        return new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "B1111111S"
        );
    }

    protected void initGroups() {
        // Add enchantment gui item
        OutlinePane filledEnchant = new OutlinePane(0, 0, 9, 5);
        filledEnchant.setPriority(Pane.Priority.HIGH);
        filledEnchant.align(OutlinePane.Alignment.BEGIN);
        filledEnchant.setOrientation(Orientable.Orientation.HORIZONTAL);

        Set<AbstractMaterialGroup> illegalGroup = this.groupContainer.illegalGroups();
        for (AbstractMaterialGroup group : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if (illegalGroup.contains(group)) {
                return;
            }
            filledEnchant.addItem(getGuiItemFromGroup(group));
        }

        addPane(filledEnchant);

    }

    private GuiItem getGuiItemFromGroup(AbstractMaterialGroup group) {
        boolean isIn = this.selectedGroups.contains(group);

        Material usedMaterial = group.getRepresentativeMaterial();
        ItemStack item = new ItemStack(usedMaterial);

        setGroupItemMeta(item, group.getName(), isIn);

        GuiItem guiItem = new GuiItem(item, CustomAnvil.instance);
        guiItem.setAction(getGroupItemConsumer(group, guiItem));
        return guiItem;
    }

    private static final List<String> TRUE_LORE = Collections.singletonList("\u00A77Value: \u00A7aSelected");
    private static final List<String> FALSE_LORE = Collections.singletonList("\u00A77Value: \u00A7cNot Selected");

    public void setGroupItemMeta(ItemStack item, String name, boolean isIn) {
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            CustomAnvil.instance.getLogger().warning("Could not create item for group: " + name + ":\n" +
                    "Item do not gave item meta: " + item + ". Using placeholder instead");
            item.setType(Material.PAPER);
            meta = item.getItemMeta();
            assert meta != null;
        }

        meta.setDisplayName("\u00A7" + (isIn ? 'a' : 'c') + CasedStringUtil.snakeToUpperSpacedCase(name));
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

    private Consumer<InventoryClickEvent> getGroupItemConsumer(AbstractMaterialGroup group, GuiItem guiItem) {
        return event -> {
            event.setCancelled(true);

            boolean isIn = this.selectedGroups.contains(group);
            if (isIn) {
                this.selectedGroups.remove(group);
            } else {
                this.selectedGroups.add(group);
            }

            ItemStack item = guiItem.getItem();
            setGroupItemMeta(item, group.getName(), !isIn);
            guiItem.setItem(item);// Just in case

            update();
        };
    }

    @Override
    public boolean onSave() {
        return this.groupContainer.setSelectedGroups(this.selectedGroups);
    }

    @Override
    public boolean hadChange() {
        Set<AbstractMaterialGroup> baseGroup = this.groupContainer.getSelectedGroups();
        return baseGroup.size() != this.selectedGroups.size() ||
                !baseGroup.containsAll(this.selectedGroups);
    }

}
