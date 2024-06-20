package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;
import xyz.alexcrea.cuanvil.group.EnchantConflictManager;
import xyz.alexcrea.cuanvil.gui.config.SelectEnchantmentContainer;
import xyz.alexcrea.cuanvil.gui.config.SelectGroupContainer;
import xyz.alexcrea.cuanvil.gui.config.ask.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantConflictGui;
import xyz.alexcrea.cuanvil.gui.config.settings.EnchantSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.GroupSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;

public class EnchantConflictSubSettingGui extends MappedToListSubSettingGui implements SelectEnchantmentContainer, SelectGroupContainer {

    private final EnchantConflictGui parent;
    private final EnchantConflictGroup enchantConflict;
    private final PatternPane pane;
    private boolean shouldWork = true;

    public EnchantConflictSubSettingGui(
            @NotNull EnchantConflictGui parent,
            @NotNull EnchantConflictGroup enchantConflict,
            @NotNull GuiItem parentItemForThisGui) {
        super(parentItemForThisGui,
                3,
                "\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.toString()) + " \u00A78Config");
        this.parent = parent;
        this.enchantConflict = enchantConflict;

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
        assert deleteMeta != null;

        deleteMeta.setDisplayName("\u00A74DELETE CONFLICT");
        deleteMeta.setLore(Collections.singletonList("\u00A7cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        this.pane.bindItem('D', new GuiItem(deleteItem, GuiGlobalActions.openGuiAction(createDeleteGui()), CustomAnvil.instance));

        // Displayed item will be updated later
        this.enchantSettingItem = new GuiItem(new ItemStack(Material.ENCHANTED_BOOK), event -> {
            event.setCancelled(true);
            EnchantSelectSettingGui enchantGui = new EnchantSelectSettingGui(
                    "\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.toString()) + "\u00A75",
                    this, this);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        this.groupSettingItem = new GuiItem(new ItemStack(Material.PAPER), event -> {
            event.setCancelled(true);
            GroupSelectSettingGui enchantGui = new GroupSelectSettingGui(
                    "\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(this.enchantConflict.toString()) + " \u00A73Groups",
                    this, this, 0);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        this.minBeforeActiveSettingFactory = IntSettingsGui.intFactory(
                "\u00A78Minimum enchantment count",
                this, this.enchantConflict + ".maxEnchantmentBeforeConflict", ConfigHolder.CONFLICT_HOLDER,
                Arrays.asList(
                        "\u00A77Minimum enchantment count set to X mean only X enchantment can be put",
                        "\u00A77on an item before the conflict is active."
                ),
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

            // Remove from enchantment
            for (CAEnchantment enchantment : this.enchantConflict.getEnchants()) {
                enchantment.removeConflict(this.enchantConflict);
            }
            manager.conflictList.remove(this.enchantConflict);

            // Remove from parent
            this.parent.removeGeneric(this.enchantConflict);

            // Remove self
            cleanAndBeUnusable();

            // Update config file storage
            ConfigHolder.CONFLICT_HOLDER.getConfig().set(this.enchantConflict.toString(), null);

            // Save
            boolean success = true;
            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("\u00A7cDelete \u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(this.enchantConflict.toString()) + "\u00A7c?",
                "\u00A77Confirm that you want to delete this conflict.",
                this, this.parent, deleteSupplier
        );
    }

    @Override
    public void updateGuiValues() {
        // update value from config to conflict
        int minBeforeBlock = ConfigHolder.CONFLICT_HOLDER.getConfig().getInt(this.enchantConflict.toString()+'.'+EnchantConflictManager.ENCH_MAX_PATH, 0);
        this.enchantConflict.setMinBeforeBlock(minBeforeBlock);

        // Parent should call updateLocal with this call
        this.parent.updateValueForGeneric(this.enchantConflict, true);
    }

    @Override
    public void updateLocal() {
        if (!this.shouldWork) return;

        // Prepare enchantment lore
        ArrayList<String> enchantLore = new ArrayList<>();
        enchantLore.add("\u00A77Allow you to select a list of \u00A75Enchantments \u00A77that this conflict should include");
        Set<CAEnchantment> enchants = getSelectedEnchantments();
        if (enchants.isEmpty()) {
            enchantLore.add("\u00A77There is no included enchantment for this conflict.");
        } else {
            enchantLore.add("\u00A77List of included enchantment for this conflict:");
            Iterator<CAEnchantment> enchantIterator = enchants.iterator();

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
        List<String> groupLore = SelectGroupContainer.getGroupLore(this, "conflict", "exclude");

        // Configure enchant setting item
        ItemStack enchantItem = this.enchantSettingItem.getItem();
        ItemMeta enchantMeta = enchantItem.getItemMeta();
        assert enchantMeta != null;

        enchantMeta.setDisplayName("\u00A7aSelect included \u00A75Enchantments \u00A7aSettings");
        enchantMeta.setLore(enchantLore);

        enchantItem.setItemMeta(enchantMeta);

        this.enchantSettingItem.setItem(enchantItem); // Just in case

        // Configure group setting item
        ItemStack groupItem = this.groupSettingItem.getItem();
        ItemMeta groupMeta = groupItem.getItemMeta();
        assert groupMeta != null;

        groupMeta.setDisplayName("\u00A7aSelect Excluded \u00A73Groups \u00A7aSettings");
        groupMeta.setLore(groupLore);

        groupItem.setItemMeta(groupMeta);

        this.groupSettingItem.setItem(groupItem); // Just in case

        this.pane.bindItem('M', this.minBeforeActiveSettingFactory.getItem(Material.COMMAND_BLOCK,
                "Minimum Enchantment Count"));
        update();
    }

    @Override
    public void cleanAndBeUnusable() {
        for (HumanEntity viewer : getViewers()) {
            this.parent.show(viewer);
        }
        this.shouldWork = false;

        // Just in case something is extremely wrong
        GuiItem background = GuiGlobalItems.backgroundItem();
        this.pane.bindItem('E', background);
        this.pane.bindItem('G', background);
        this.pane.bindItem('M', background);
        this.pane.bindItem('D', background);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (this.shouldWork) {
            super.show(humanEntity);
        } else {
            this.parent.show(humanEntity);
        }
    }

    // Select enchantment container methods

    @Override
    public Set<CAEnchantment> getSelectedEnchantments() {
        return this.enchantConflict.getEnchants();
    }

    @Override
    public boolean setSelectedEnchantments(Set<CAEnchantment> enchantments) {
        if (!this.shouldWork) {
            CustomAnvil.instance.getLogger().info("Trying to save " + enchantConflict + " enchants but sub config is destroyed");
            return false;
        }

        // Set live configuration
        this.enchantConflict.setEnchants(enchantments);

        // Save on file configuration
        String[] enchantKeys = new String[enchantments.size()];
        int index = 0;
        for (CAEnchantment enchantment : enchantments) {
            enchantKeys[index++] = enchantment.getKey().getKey();
        }
        ConfigHolder.CONFLICT_HOLDER.getConfig().set(enchantConflict + ".enchantments", enchantKeys);

        try {
            updateGuiValues();
        } catch (Exception e) {
            CustomAnvil.instance.getLogger().log(Level.WARNING, "An error occurred while updating enchants for " + this.enchantConflict, e);
        }

        // Save file configuration to disk
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }

        return true;
    }

    @Override
    public Set<CAEnchantment> illegalEnchantments() {
        return Collections.emptySet();
    }

    // Select group container methods

    @Override
    public Set<AbstractMaterialGroup> getSelectedGroups() {
        return this.enchantConflict.getCantConflictGroup().getGroups();
    }

    @Override
    public boolean setSelectedGroups(Set<AbstractMaterialGroup> groups) {
        if (!this.shouldWork) {
            CustomAnvil.instance.getLogger().info("Trying to save " + enchantConflict.toString() + " groups but sub config is destroyed");
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
        ConfigHolder.CONFLICT_HOLDER.getConfig().set(this.enchantConflict + ".notAffectedGroups", groupsNames);

        try {
            updateGuiValues();
        } catch (Exception e) {
            CustomAnvil.instance.getLogger().log(Level.WARNING, "An error occurred while updating group for " + this.enchantConflict, e);
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
