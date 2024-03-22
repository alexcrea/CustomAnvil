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
import xyz.alexcrea.cuanvil.gui.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;
import xyz.alexcrea.cuanvil.gui.config.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.SelectEnchantmentContainer;
import xyz.alexcrea.cuanvil.gui.config.SelectGroupContainer;
import xyz.alexcrea.cuanvil.gui.config.openable.EnchantConflictGui;
import xyz.alexcrea.cuanvil.gui.config.settings.EnchantSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.GroupSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.config.settings.IntSettingsGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;
import java.util.function.Supplier;

public class EnchantConflictSubSettingGui extends ValueUpdatableGui implements SelectEnchantmentContainer, SelectGroupContainer {

    private final EnchantConflictGui parent;
    private final EnchantConflictGroup enchantConflict;
    private final PatternPane pane;
    private boolean canOpen = true;

    public EnchantConflictSubSettingGui(
            @NotNull EnchantConflictGui parent,
            @NotNull EnchantConflictGroup enchantConflict) {
        super(3, "\u00A72Config for \u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName()), CustomAnvil.instance);
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

        GuiGlobalItems.addBackItem(pane, MainConfigGui.INSTANCE);
        GuiGlobalItems.addBackgroundItem(pane);

        // Delete item
        ItemStack deleteItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta deleteMeta = deleteItem.getItemMeta();

        deleteMeta.setDisplayName("\u00A74DELETE CONFLICT");
        deleteMeta.setLore(Collections.singletonList("\u00A7cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        pane.bindItem('D', new GuiItem(deleteItem, GuiGlobalActions.openGuiAction(createDeleteGui()), CustomAnvil.instance));

        // Displayed item will be updated later

        enchantSettingItem = new GuiItem(new ItemStack(Material.ENCHANTED_BOOK), (event)->{
            event.setCancelled(true);
            EnchantSelectSettingGui enchantGui = new EnchantSelectSettingGui(
                    "\u00A7eEnchantments for \u00A78" +CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName()),
                    this, this, 0);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        groupSettingItem = new GuiItem(new ItemStack(Material.PAPER), (event)->{
            event.setCancelled(true);
            GroupSelectSettingGui enchantGui = new GroupSelectSettingGui(
                    "\u00A7eGroups for \u00A78" +CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName()),
                    this, this, 0);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        minBeforeActiveSettingFactory = IntSettingsGui.intFactory("\u00A7eMinimum enchantment before conflict is active", this,
                enchantConflict.getName()+".maxEnchantmentBeforeConflict", ConfigHolder.CONFLICT_HOLDER,
                0, 255, 0, 1
                );

        pane.bindItem('E', enchantSettingItem);
        pane.bindItem('G', groupSettingItem);

        // Now we update the items
        updateLocal();

    }

    private ConfirmActionGui createDeleteGui() {
        Supplier<Boolean> deleteSupplier = () ->{
            EnchantConflictManager manager = ConfigHolder.CONFLICT_HOLDER.getConflictManager();

            // Remove from manager
            for (Enchantment enchantment : enchantConflict.getEnchants()) {
                manager.removeConflictFromMap(enchantment, enchantConflict);
            }
            manager.conflictList.remove(enchantConflict);

            // Update config file storage
            ConfigHolder.CONFLICT_HOLDER.getConfig().set(enchantConflict.getName(), null);

            // Save
            boolean success = true;
            if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE){
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("\u00A7cDelete \u00A7e"+CasedStringUtil.snakeToUpperSpacedCase(enchantConflict.getName())+"\u00A7c ?",
                "\u00A77Confirm that you want to delete this conflict.",
                this, this.parent, deleteSupplier
                );
    }

    @Override
    public void updateGuiValues() {
        this.parent.updateValueForConflict(this.enchantConflict);
        // Parent should call updateLocal
    }

    public void updateLocal(){
        if(!this.canOpen) return;

        // Prepare enchantment lore
        ArrayList<String> enchantLore = new ArrayList<>();
        enchantLore.add("\u00A77Allow you to select a list of \u00A75Enchantments \u00A77that this conflict should include");
        Set<Enchantment> enchants = getSelectedEnchantments();
        if(enchants.isEmpty()){
            enchantLore.add("\u00A77There is no enchantment for this conflict.");
        }else{
            enchantLore.add("\u00A77List of included enchantment for this conflict:");
            Iterator<Enchantment> enchantIterator = enchants.iterator();

            int maxindex = (enchants.size() > 5 ? 4 : enchants.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Fire Protection"
                enchantLore.add("\u00A77- \u00A75"+CasedStringUtil.snakeToUpperSpacedCase(enchantIterator.next().getKey().getKey()));
            }
        }

        // Configure enchant setting item
        ItemStack enchantItem = enchantSettingItem.getItem();
        ItemMeta enchantMeta = enchantItem.getItemMeta();

        enchantMeta.setDisplayName("\u00A7aSelect \u00A75Enchantments \u00A7aSettings");
        enchantMeta.setLore(enchantLore);

        enchantItem.setItemMeta(enchantMeta);

        enchantSettingItem.setItem(enchantItem); // Just in case


        //todo: groupSettingItem


        pane.bindItem('M', GuiGlobalItems.intSettingGuiItem(minBeforeActiveSettingFactory, Material.COMMAND_BLOCK));
        update();
    }

    public void cleanUnused(){
        for (HumanEntity viewer : getViewers()) {
            this.parent.show(viewer);
        }
        this.canOpen = false;

        // Just in case something is extremely wrong
        GuiItem background = GuiGlobalItems.backgroundItem();
        pane.bindItem('E', background);
        pane.bindItem('G', background);
        pane.bindItem('M', background);
        pane.bindItem('D', background);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if(this.canOpen){
            super.show(humanEntity);
        }else{
            this.parent.show(humanEntity);
        }
    }

    // Select enchantment container methods

    @Override
    public Set<Enchantment> getSelectedEnchantments() {
        return this.enchantConflict.getEnchants();
    }

    @Override
    public boolean setSelectedEnchantments(Set<Enchantment> enchantments) {
        return false;
    }

    @Override
    public Set<Enchantment> illegalEnchantments() {
        return new HashSet<>();
    }

    // Select group container methods

    @Override
    public Set<AbstractMaterialGroup> getSelectedGroups() {
        return this.enchantConflict.getCantConflictGroup().getGroups();
    }

    @Override
    public boolean setSelectedGroups(Set<AbstractMaterialGroup> groups) {
        // Set live configuration
        this.enchantConflict.getCantConflictGroup().setGroups(groups);

        // Save on file configuration

        // Save file configuration to disk

        return false;
    }

    @Override
    public Set<AbstractMaterialGroup> illegalGroups() {

        return new HashSet<>();
    }
}
