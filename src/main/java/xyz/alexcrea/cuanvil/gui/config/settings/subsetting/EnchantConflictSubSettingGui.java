package xyz.alexcrea.cuanvil.gui.config.settings.subsetting;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.EnchantConflictManager;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.SelectEnchantmentContainer;
import xyz.alexcrea.cuanvil.gui.config.SelectGroupContainer;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantConflictGui;
import xyz.alexcrea.cuanvil.gui.config.settings.EnchantSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.GroupSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

public class EnchantConflictSubSettingGui extends ValueUpdatableGui implements SelectEnchantmentContainer, SelectGroupContainer {

    private final EnchantConflictGui parent;
    private final EnchantConflictGroup enchantConflict;
    private final GuiItem parentItemForThisGui;
    private final PatternPane pane;
    private boolean shouldWorld = true;

    public EnchantConflictSubSettingGui(
            @NotNull EnchantConflictGui parent,
            @NotNull EnchantConflictGroup enchantConflict,
            @NotNull GuiItem parentItemForThisGui) {
        super(3,
                "\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName()) + " \u00A78Config",
                CustomAnvil.instance);
        this.parent = parent;
        this.enchantConflict = enchantConflict;
        this.parentItemForThisGui = parentItemForThisGui;

        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "00EGM000D",
                "B00000000"
        );
        this.pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(this.pane);

        prepareStaticValues();
    }

    private GuiItem enchantSettingItem;
    private GuiItem groupSettingItem;
    private IntSettingsGui.IntSettingFactory minBeforeActiveSettingFactory;

    private void prepareStaticValues() {

        GuiGlobalItems.addBackItem(this.pane, this.parent);
        GuiGlobalItems.addBackgroundItem(this.pane);

        // Delete item
        ItemStack deleteItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta deleteMeta = deleteItem.getItemMeta();

        deleteMeta.setDisplayName("\u00A74DELETE CONFLICT");
        deleteMeta.setLore(Collections.singletonList("\u00A7cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        this.pane.bindItem('D', new GuiItem(deleteItem, GuiGlobalActions.openGuiAction(createDeleteGui()), CustomAnvil.instance));

        // Displayed item will be updated later

        this.enchantSettingItem = new GuiItem(new ItemStack(Material.ENCHANTED_BOOK), (event) -> {
            event.setCancelled(true);
            EnchantSelectSettingGui enchantGui = new EnchantSelectSettingGui(
                    "\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName()) + " \u00A75Enchantments",
                    this, this, 0);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        this.groupSettingItem = new GuiItem(new ItemStack(Material.PAPER), (event) -> {
            event.setCancelled(true);
            GroupSelectSettingGui enchantGui = new GroupSelectSettingGui(
                    "\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(this.enchantConflict.getName()) + " \u00A73Groups",
                    this, this, 0);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        this.minBeforeActiveSettingFactory = IntSettingsGui.intFactory(
                "\u00A78Minimum enchantment count",
                this, this.enchantConflict.getName() + ".maxEnchantmentBeforeConflict", ConfigHolder.CONFLICT_HOLDER,
                0, 255, 0, 1
        );

        this.pane.bindItem('E', this.enchantSettingItem);
        this.pane.bindItem('G', this.groupSettingItem);

        // Now we update the items
        updateLocal();

    }

    private ConfirmActionGui createDeleteGui() {
        Supplier<Boolean> deleteSupplier = () -> {
            EnchantConflictManager manager = ConfigHolder.CONFLICT_HOLDER.getConflictManager();

            // Remove from manager
            for (Enchantment enchantment : this.enchantConflict.getEnchants()) {
                manager.removeConflictFromMap(enchantment, this.enchantConflict);
            }
            manager.conflictList.remove(this.enchantConflict);

            // Remove from parent
            this.parent.removeConflict(this.enchantConflict);

            // Remove self
            cleanUnused();

            // Update config file storage
            ConfigHolder.CONFLICT_HOLDER.getConfig().set(this.enchantConflict.getName(), null);

            // Save
            boolean success = true;
            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("\u00A7cDelete \u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName()) + "\u00A7c?",
                "\u00A77Confirm that you want to delete this conflict.",
                this, this.parent, deleteSupplier
        );
    }

    @Override
    public void updateGuiValues() {
        this.parent.updateValueForGeneric(this.enchantConflict, true);
        // Parent should call updateLocal
    }

    public void updateLocal() {
        if (!this.shouldWorld) return;

        // Prepare enchantment lore
        ArrayList<String> enchantLore = new ArrayList<>();
        enchantLore.add("\u00A77Allow you to select a list of \u00A75Enchantments \u00A77that this conflict should include");
        Set<Enchantment> enchants = getSelectedEnchantments();
        if (enchants.isEmpty()) {
            enchantLore.add("\u00A77There is no included enchantment for this conflict.");
        } else {
            enchantLore.add("\u00A77List of included enchantment for this conflict:");
            Iterator<Enchantment> enchantIterator = enchants.iterator();

            boolean greaterThanMax = enchants.size() > 5;
            int maxindex = (greaterThanMax ? 4 : enchants.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Fire Protection"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(enchantIterator.next().getKey().getKey());
                enchantLore.add("\u00A77- \u00A75" + formattedName);
            }
            if (greaterThanMax) {
                enchantLore.add("\u00A77And " + (enchants.size() - 4) + " more...");
            }

        }

        // Prepare group lore
        ArrayList<String> groupLore = new ArrayList<>();
        groupLore.add("\u00A77Allow you to select a list of \u00A73Groups \u00A77that this conflict should include");
        Set<AbstractMaterialGroup> grouos = getSelectedGroups();
        if (grouos.isEmpty()) {
            groupLore.add("\u00A77There is no excluded groups for this conflict.");
        } else {
            groupLore.add("\u00A77List of excluded groups for this conflict:");
            Iterator<AbstractMaterialGroup> groupIterator = grouos.iterator();

            boolean greaterThanMax = grouos.size() > 5;
            int maxindex = (greaterThanMax ? 4 : grouos.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Melee Weapons"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(groupIterator.next().getName());
                groupLore.add("\u00A77- \u00A73" + formattedName);

            }
            if (greaterThanMax) {
                groupLore.add("\u00A77And " + (grouos.size() - 4) + " more...");
            }
        }

        // Configure enchant setting item
        ItemStack enchantItem = this.enchantSettingItem.getItem();
        ItemMeta enchantMeta = enchantItem.getItemMeta();

        enchantMeta.setDisplayName("\u00A7aSelect included \u00A75Enchantments \u00A7aSettings");
        enchantMeta.setLore(enchantLore);

        enchantItem.setItemMeta(enchantMeta);

        this.enchantSettingItem.setItem(enchantItem); // Just in case

        // Configure group setting item
        ItemStack groupItem = this.groupSettingItem.getItem();
        ItemMeta groupMeta = groupItem.getItemMeta();

        groupMeta.setDisplayName("\u00A7aSelect excluded \u00A73Groups \u00A7aSettings");
        groupMeta.setLore(groupLore);

        groupItem.setItemMeta(groupMeta);

        this.groupSettingItem.setItem(groupItem); // Just in case


        this.pane.bindItem('M', GuiGlobalItems.intSettingGuiItem(this.minBeforeActiveSettingFactory, Material.COMMAND_BLOCK));
        update();
    }

    public void cleanUnused() {
        for (HumanEntity viewer : getViewers()) {
            this.parent.show(viewer);
        }
        this.shouldWorld = false;

        // Just in case something is extremely wrong
        GuiItem background = GuiGlobalItems.backgroundItem();
        this.pane.bindItem('E', background);
        this.pane.bindItem('G', background);
        this.pane.bindItem('M', background);
        this.pane.bindItem('D', background);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (this.shouldWorld) {
            super.show(humanEntity);
        } else {
            this.parent.show(humanEntity);
        }
    }

    public GuiItem getParentItemForThisGui() {
        return parentItemForThisGui;
    }

    // Select enchantment container methods

    @Override
    public Set<Enchantment> getSelectedEnchantments() {
        return this.enchantConflict.getEnchants();
    }

    @Override
    public boolean setSelectedEnchantments(Set<Enchantment> enchantments) {
        if (!this.shouldWorld) {
            CustomAnvil.instance.getLogger().info("Trying to save " + enchantConflict.getName() + " enchants but sub config is destroyed");
            return false;
        }

        // Set live configuration
        this.enchantConflict.setEnchants(enchantments);

        // Save on file configuration
        String[] enchantKeys = new String[enchantments.size()];
        int index = 0;
        for (Enchantment enchantment : enchantments) {
            enchantKeys[index++] = enchantment.getKey().getKey();
        }
        ConfigHolder.CONFLICT_HOLDER.getConfig().set(enchantConflict.getName() + ".enchantments", enchantKeys);

        try {
            updateGuiValues();
        } catch (Exception e) {
            CustomAnvil.instance.getLogger().log(Level.WARNING, "An error occurred while updating enchants for " + this.enchantConflict.getName(), e);
        }


        // Save file configuration to disk
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }

        return true;
    }

    @Override
    public Set<Enchantment> illegalEnchantments() {
        return Collections.emptySet();
    }

    // Select group container methods

    @Override
    public Set<AbstractMaterialGroup> getSelectedGroups() {
        return this.enchantConflict.getCantConflictGroup().getGroups();
    }

    @Override
    public boolean setSelectedGroups(Set<AbstractMaterialGroup> groups) {
        if (!this.shouldWorld) {
            CustomAnvil.instance.getLogger().info("Trying to save " + enchantConflict.getName() + " groups but sub config is destroyed");
            return false;
        }

        // Set live configuration
        this.enchantConflict.getCantConflictGroup().setGroups(groups);

        // Save on file configuration
        String[] groupsNames = new String[groups.size()];
        int index = 0;
        for (AbstractMaterialGroup group : groups) {
            groupsNames[index++] = group.getName();
        }
        ConfigHolder.CONFLICT_HOLDER.getConfig().set(this.enchantConflict.getName() + ".notAffectedGroups", groupsNames);

        try {
            updateGuiValues();
        } catch (Exception e) {
            CustomAnvil.instance.getLogger().log(Level.WARNING, "An error occurred while updating group for " + this.enchantConflict.getName(), e);
        }

        // Save file configuration to disk
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }

        return true;
    }

    @Override
    public Set<AbstractMaterialGroup> illegalGroups() {

        return Collections.emptySet();
    }
}
