package xyz.alexcrea.cuanvil.gui.config.list.elements;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.group.*;
import xyz.alexcrea.cuanvil.gui.config.SelectGroupContainer;
import xyz.alexcrea.cuanvil.gui.config.ask.ConfirmActionGui;
import xyz.alexcrea.cuanvil.gui.config.global.GroupConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.GroupSelectSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GroupConfigSubSettingGui extends MappedToListSubSettingGui implements SelectGroupContainer {

    private final GroupConfigGui parent;
    private final IncludeGroup group;
    private final PatternPane pane;

    public GroupConfigSubSettingGui(
            @NotNull GroupConfigGui parent,
            @NotNull IncludeGroup group,
            @NotNull GuiItem item) {
        super(item, 3, group.getName());
        this.parent = parent;
        this.group = group;

        Pattern pattern = new Pattern(
                GuiSharedConstant.EMPTY_GUI_FULL_LINE,
                "00102000D",
                "B00000000"
        );
        this.pane = new PatternPane(0, 0, 9, 3, pattern);
        addPane(this.pane);

        prepareStaticValues();
    }

    private GuiItem materialSelection;
    private GuiItem groupSelection;
    private void prepareStaticValues() {
        GuiGlobalItems.addBackItem(this.pane, this.parent);
        GuiGlobalItems.addBackgroundItem(this.pane);

        // Delete item
        ItemStack deleteItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta deleteMeta = deleteItem.getItemMeta();

        deleteMeta.setDisplayName("\u00A74DELETE GROUP");
        deleteMeta.setLore(Collections.singletonList("\u00A7cCaution with this button !"));

        deleteItem.setItemMeta(deleteMeta);
        this.pane.bindItem('D', new GuiItem(deleteItem, openGuiAndCheckAction(), CustomAnvil.instance));

        // Displayed item will be updated later
        this.materialSelection = new GuiItem(new ItemStack(Material.DIAMOND_SWORD), (event) -> {
            event.setCancelled(true);

        }, CustomAnvil.instance);

        this.groupSelection = new GuiItem(new ItemStack(Material.CHEST), (event) -> {
            event.setCancelled(true);
            GroupSelectSettingGui enchantGui = new GroupSelectSettingGui(
                    "\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(this.group.toString()) + " \u00A7rGroups",
                    this, this, 0);
            enchantGui.show(event.getWhoClicked());
        }, CustomAnvil.instance);

        this.pane.bindItem('1', this.materialSelection);
        this.pane.bindItem('2', this.groupSelection);
    }

    private @NotNull Consumer<InventoryClickEvent> openGuiAndCheckAction() {
        ConfirmActionGui deleteGui = createDeleteGui();
        return event -> {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();
            // Do not allow to open inventory if player do not have edit configuration permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }
            // test if group is used & cancel & warn user if so
            if(testAndWarnIfUsed(player)) return;

            deleteGui.show(player);
        };
    }

    private @NotNull ConfirmActionGui createDeleteGui() {
        Supplier<Boolean> deleteSupplier = () -> {
            // test if group is used & cancel if so
            if(!getUsedLocations(this.group).isEmpty()) return false;

            ItemGroupManager manager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();

            // Remove from manager
            manager.getGroupMap().remove(this.group.getName());

            // Remove from parent
            this.parent.removeGeneric(this.group);

            // Remove self
            cleanAndBeUnusable();

            // Update config file storage
            ConfigHolder.CUSTOM_RECIPE_HOLDER.getConfig().set(this.group.getName(), null);

            // Save
            boolean success = true;
            if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                success = ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
            }

            return success;
        };

        return new ConfirmActionGui("\u00A7cDelete \u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(this.group.toString()) + "\u00A7c?",
                "\u00A77Confirm that you want to delete this group.",
                this, this.parent, deleteSupplier
        );
    }

    public boolean testAndWarnIfUsed(HumanEntity player){
        List<String> usedLoc = getUsedLocations(this.group);
        if(usedLoc.isEmpty()){
            return false;
        }
        StringBuilder stb = new StringBuilder("\u00A7cCan't delete group " +this.group.getName()+
                "\n\u00A7eUsed by:");
        int maxIndex = usedLoc.size();
        int nbMore = 0;
        if(maxIndex > 10){
            nbMore = maxIndex - 9;
            maxIndex = 9;
        }
        for (int i = 0; i < maxIndex; i++) {
            stb.append("\n\u00A7r-\u00A7e ").append(usedLoc.get(i));
        }
        if(nbMore > 0){
            stb.append("\u00A7cAnd ").append(nbMore).append(" More...");
        }

        player.sendMessage(stb.toString());
        return true;
    }

    // return a string containing every instance of where this group is used
    public static List<String> getUsedLocations(AbstractMaterialGroup group){
        ArrayList<String> usageList = new ArrayList<>();

        // Test used by another group
        ItemGroupManager groupManager = ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager();
        for (AbstractMaterialGroup otherGroup : groupManager.getGroupMap().values()) {
            if(otherGroup.getGroups().contains(group)) {
                usageList.add("group " + otherGroup.getName());
            }
        }

        // Test if used for conflict
        EnchantConflictManager conflictManager = ConfigHolder.CONFLICT_HOLDER.getConflictManager();
        for (EnchantConflictGroup conflict : conflictManager.getConflictList()) {
            if(conflict.getCantConflictGroup().getGroups().contains(group)) {
                usageList.add("conflict " + conflict);
            }
        }

        return usageList;
    }

    @Override
    public void updateGuiValues() {
        // Parent should call updateLocal with this call
        this.parent.updateValueForGeneric(this.group, true);

    }

    @Override
    public void updateLocal() {
        // Prepare group lore
        List<String> groupLore = SelectGroupContainer.getGroupLore(this, "group", "include");

        // Configure enchant setting item
        ItemStack matSelectItem = this.materialSelection.getItem();
        ItemMeta matSelectMeta = matSelectItem.getItemMeta();

        matSelectMeta.setDisplayName("\u00A7aSelect included \u00A7eMaterials \u00A7aSettings");
        matSelectMeta.setLore(Collections.emptyList()); // temporary
        matSelectMeta.addItemFlags(ItemFlag.values());

        matSelectItem.setItemMeta(matSelectMeta);

        this.materialSelection.setItem(matSelectItem); // Just in case

        // Configure enchant setting item
        ItemStack groupSelectItem = this.groupSelection.getItem();
        ItemMeta groupSelectMeta = groupSelectItem.getItemMeta();

        groupSelectMeta.setDisplayName("\u00A7aSelect included \u00A73Groups \u00A7aSettings");
        groupSelectMeta.setLore(groupLore);

        groupSelectItem.setItemMeta(groupSelectMeta);

        this.groupSelection.setItem(groupSelectItem); // Just in case
    }

    @Override
    public void cleanAndBeUnusable() {

    }

    // ----------------------------
    // SelectGroupContainer methods
    // ----------------------------

    @Override
    public Set<AbstractMaterialGroup> getSelectedGroups() {
        return this.group.getGroups();
    }

    @Override
    public boolean setSelectedGroups(Set<AbstractMaterialGroup> groups) {
        // update group and referencing groups
        updateGroup(this.group, groups);

        // Save file configuration to disk
        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
            return ConfigHolder.CONFLICT_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
        }

        return true;
    }

    private void updateGroup(@NotNull AbstractMaterialGroup group, Set<AbstractMaterialGroup> groups){
        // Set live configuration
        group.setGroups(groups);

        // Write to file configuration
        groups = group.getGroups(); // Maybe some group may have been rejected
        String[] groupNames = new String[groups.size()];
        int index = 0;
        for (AbstractMaterialGroup otherGroup : groups) {
            groupNames[index++] = otherGroup.getName();
        }

        ConfigHolder.ITEM_GROUP_HOLDER.getConfig().set(this.group.getName()+"."+ItemGroupManager.GROUP_LIST_PATH, groupNames);

        // Try to update referencing group. kind of expensive operation in some case.
        for (AbstractMaterialGroup otherGroup : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if(otherGroup.getGroups().contains(group)){
                Set<AbstractMaterialGroup> groupClone = new HashSet<>(otherGroup.getGroups());
                updateGroup(otherGroup, groupClone);
            }
        }

        // Update parent & local by extension
        if(group instanceof IncludeGroup){
            this.parent.updateValueForGeneric((IncludeGroup) group, true);
        }

        // We assume a backup & save call we be done soon after
    }

    @Override
    public Set<AbstractMaterialGroup> illegalGroups() {
        Set<AbstractMaterialGroup> illegal = new HashSet<>();

        for (AbstractMaterialGroup otherGroup : ConfigHolder.ITEM_GROUP_HOLDER.getItemGroupsManager().getGroupMap().values()) {
            if(otherGroup.isReferencing(this.group)){
                illegal.add(otherGroup);
            }
        }
        illegal.add(this.group);

        return illegal;
    }
}
