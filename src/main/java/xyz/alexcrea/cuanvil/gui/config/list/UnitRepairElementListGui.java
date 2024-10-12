package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.ask.SelectItemTypeGui;
import xyz.alexcrea.cuanvil.gui.config.global.UnitRepairConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.ElementMappedToListGui;
import xyz.alexcrea.cuanvil.gui.config.settings.DoubleSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class UnitRepairElementListGui extends SettingGuiListConfigGui<String, DoubleSettingGui.DoubleSettingFactory> implements ElementMappedToListGui {

    private final GuiItem parentItem;
    private final Material parentMaterial;
    private final UnitRepairConfigGui parentGui;
    private final String materialName;

    private boolean shouldWork = true;
    public UnitRepairElementListGui(@NotNull Material parentMaterial,
                                    @NotNull UnitRepairConfigGui parentGui,
                                    @NotNull GuiItem parentItem) {
        super("§e" + CasedStringUtil.snakeToUpperSpacedCase(parentMaterial.name().toLowerCase()) + " §rUnit repair");
        this.parentItem = parentItem;
        this.parentMaterial = parentMaterial;
        this.parentGui = parentGui;
        this.materialName = CasedStringUtil.snakeToUpperSpacedCase(parentMaterial.name().toLowerCase());

        GuiGlobalItems.addBackItem(this.backgroundPane, parentGui);
    }

    // SettingGuiListConfigGui methods
    @Override
    protected List<String> getCreateItemLore() {
        return Arrays.asList(
                "§7Select a new item to be repairable.",
                "§7You will be asked the material to use."
        );
    }

    @Override
    protected Consumer<InventoryClickEvent> getCreateClickConsumer() {
        return event -> {
            event.setCancelled(true);
            if(!this.shouldWork){
                return;
            }
            event.setCancelled(true);

            new SelectItemTypeGui(
                    "Select item to be repaired.",
                    "§7Click here with an item to set the item\n" +
                            "§7You like to be repaired by " + this.materialName,
                    this,
                    (itemStack, player) -> {
                        ItemMeta meta = itemStack.getItemMeta();
                        Material type = itemStack.getType();

                        if(!(meta instanceof Damageable) || (type.getMaxDurability() <= 0)) {
                            player.sendMessage("§cThis item can't be damaged, so it can't be repaired.");
                            return;
                        }
                        if(type == this.parentMaterial){
                            player.sendMessage("§cItem can't repair something of the same type.");
                            return;
                        }

                        String materialName = type.name().toLowerCase();

                        // Add new material
                        ConfigHolder.UNIT_REPAIR_HOLDER.acquiredWrite()
                                .set(parentMaterial.name().toLowerCase() + "." + materialName,0.25);
                        ConfigHolder.UNIT_REPAIR_HOLDER.releaseWrite();

                        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                            ConfigHolder.UNIT_REPAIR_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
                        }

                        // Update gui
                        updateValueForGeneric(materialName, true);
                        this.parentGui.updateValueForGeneric(this.parentMaterial, true);


                        // Display material edit setting
                        this.factoryMap.get(materialName).create().show(player);
                    },
                    true
            ).show(event.getWhoClicked());

        };
    }

    @Override
    protected String createItemName() {
        return "§aAdd a new item reparable by " + this.materialName;
    }

    @Override
    protected DoubleSettingGui.DoubleSettingFactory createFactory(String materialName) {
        String materialDisplayName = CasedStringUtil.snakeToUpperSpacedCase(materialName);

        return new DoubleSettingGui.DoubleSettingFactory(
                "§0%§8" + materialDisplayName +" Repair",
                this,
                ConfigHolder.UNIT_REPAIR_HOLDER,
                this.parentMaterial.name().toLowerCase()+"."+materialName,
                Arrays.asList(
                        "§7Click here to change how many §e% §7of §a" + materialDisplayName,
                        "§7Should get repaired by §e"+this.materialName
                ),
                2,
                true, true,
                0,
                1,
                0.25,
                0.01, 0.05, 0.25
        );
    }

    @Override
    protected GuiItem itemFromFactory(String materialName, DoubleSettingGui.DoubleSettingFactory factory) {
        return factory.getItem(materialFromName(materialName),
                "§7%§a" + CasedStringUtil.snakeToUpperSpacedCase(materialName)+ " §erepaired by §a" + this.materialName);
    }

    @Override
    protected Collection<String> getEveryDisplayableInstanceOfGeneric() {
        ArrayList<String> keys = new ArrayList<>();
        if(!this.shouldWork){
            return keys;
        }

        ConfigurationSection materialSection = ConfigHolder.UNIT_REPAIR_HOLDER.get()
                .getConfigurationSection(parentMaterial.name().toLowerCase());
        if(materialSection == null){
            return keys;
        }
        keys.addAll(materialSection.getKeys(false));
        return keys;
    }

    private Material materialFromName(String materialName){
        Material mat = Material.getMaterial(materialName.toUpperCase());
        if(mat == null || mat.isAir()) return Material.BARRIER;
        return mat;
    }

    @Override
    public void updateGuiValues() {
        super.updateGuiValues();
        this.parentGui.updateValueForGeneric(this.parentMaterial, true);
    }

    // ElementMappedToListGui methods

    @Override
    public GuiItem getParentItemForThisGui() {
        return this.parentItem;
    }

    @Override // Not used in this implementation
    public void updateLocal() {}

    @Override
    public void cleanAndBeUnusable() {
        this.shouldWork = false;
        this.backgroundPane.bindItem('S', GuiGlobalItems.backgroundItem(Material.BLACK_STAINED_GLASS_PANE));
        this.backgroundPane.bindItem('L', GuiGlobalItems.backgroundItem(Material.BLACK_STAINED_GLASS_PANE));
        this.backgroundPane.bindItem('R', GuiGlobalItems.backgroundItem(Material.BLACK_STAINED_GLASS_PANE));

        for (HumanEntity viewer : getViewers()) {
            viewer.sendMessage("This config do not exist anymore");
            this.parentGui.show(viewer);
        }
    }

    @Override
    public Gui getMappedGui() {
        return this;
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if(!this.shouldWork){
            humanEntity.closeInventory();
            return;
        }
        super.show(humanEntity);
    }

}
