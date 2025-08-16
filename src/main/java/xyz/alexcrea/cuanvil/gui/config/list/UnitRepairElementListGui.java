package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemType;
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
import xyz.alexcrea.cuanvil.util.ItemTypeUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class UnitRepairElementListGui extends
        SettingGuiListConfigGui<ItemType, DoubleSettingGui.DoubleSettingFactory> implements ElementMappedToListGui {

    private final ItemType parentType;
    private final UnitRepairConfigGui parentGui;
    private final String typeName;

    private boolean shouldWork = true;

    public UnitRepairElementListGui(@NotNull ItemType parentType,
                                    @NotNull UnitRepairConfigGui parentGui) {
        super("§e" + CasedStringUtil.snakeToUpperSpacedCase(parentType.getKey().getKey()) + " §rUnit repair");
        this.parentType = parentType;
        this.parentGui = parentGui;
        this.typeName = CasedStringUtil.snakeToUpperSpacedCase(parentType.getKey().getKey());

        GuiGlobalItems.addBackItem(this.backgroundPane, parentGui);
    }

    // SettingGuiListConfigGui methods
    @Override
    protected List<String> getCreateItemLore() {
        return Arrays.asList(
                "§7Select a new item to be repairable.",
                "§7You will be asked the item to use."
        );
    }

    @Override
    protected Consumer<InventoryClickEvent> getCreateClickConsumer() {
        return event -> {
            event.setCancelled(true);
            if (!this.shouldWork) {
                return;
            }
            event.setCancelled(true);

            new SelectItemTypeGui(
                    "Select item to be repaired.",
                    "§7Click here with an item to set the item\n" +
                            "§7You like to be repaired by " + this.typeName,
                    this,
                    (itemStack, player) -> {
                        ItemMeta meta = itemStack.getItemMeta();
                        ItemType type = itemStack.getType().asItemType();

                        if (!(meta instanceof Damageable)) {
                            player.sendMessage("§cThis item can't be damaged, so it can't be repaired.");
                            return;
                        }
                        if (type == this.parentType) {
                            player.sendMessage("§cItem can't repair something of the same type.");
                            return;
                        }

                        // Add new item
                        ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().set(parentType.getKey() + "." + type.getKey(), 0.25);

                        if (GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                            ConfigHolder.UNIT_REPAIR_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
                        }

                        // Update gui
                        updateValueForGeneric(type, true);
                        this.parentGui.updateValueForGeneric(this.parentType, true);

                        // Display item edit setting
                        this.factoryMap.get(type).create().show(player);
                    },
                    true
            ).show(event.getWhoClicked());

        };
    }

    @Override
    protected String createItemName() {
        return "§aAdd a new item reparable by " + this.typeName;
    }

    @Override
    protected DoubleSettingGui.DoubleSettingFactory createFactory(ItemType type) {
        String displayName = CasedStringUtil.snakeToUpperSpacedCase(type.getKey().getKey());

        return new DoubleSettingGui.DoubleSettingFactory(
                "§0%§8" + displayName + " Repair",
                this,
                ConfigHolder.UNIT_REPAIR_HOLDER,
                this.parentType.getKey() + "." + type.getKey(),
                Arrays.asList(
                        "§7Click here to change how many §e% §7of §a" + displayName,
                        "§7Should get repaired by §e" + this.typeName
                ),
                2,
                true, true,
                0,
                1,
                0.25,
                new double[]{0.01, 0.05, 0.25},
                this.parentType.getKey().getKey() + "." + type.getKey().getKey(),
                this.parentType.getKey() + "." + type.getKey().getKey(),
                this.parentType.getKey().getKey() + "." + type.getKey()
        );
    }

    @Override
    protected GuiItem itemFromFactory(ItemType type, DoubleSettingGui.DoubleSettingFactory factory) {
        return factory.getItem(type,
                "§7%§a" + CasedStringUtil.snakeToUpperSpacedCase(type.getKey().getKey()) + " §erepaired by §a" + this.typeName);
    }

    private void fillSet(HashSet<ItemType> set, String path){
        ConfigurationSection itemSection = ConfigHolder.UNIT_REPAIR_HOLDER
                .getConfig()
                .getConfigurationSection(path);
        if (itemSection != null) {
            for (String key : itemSection.getKeys(false)) {
                ItemType type = ItemTypeUtil.INSTANCE.getItemTypeExact(key);
                if(type == null) continue; // maybe warn the user ?

                set.add(type);
            }
        }
    }

    @Override
    protected Collection<ItemType> getEveryDisplayableInstanceOfGeneric() {
        HashSet<ItemType> keys = new HashSet<>();
        if (!this.shouldWork) {
            return keys;
        }

        fillSet(keys, parentType.getKey().toString());
        fillSet(keys, parentType.getKey().getKey());

        return keys;
    }

    @Override
    public void updateGuiValues() {
        super.updateGuiValues();
        this.parentGui.updateValueForGeneric(this.parentType, true);
    }

    // ElementMappedToListGui methods

    @Override // Not used in this implementation
    public void updateLocal() {
    }

    @Override
    public void cleanAndBeUnusable() {
        this.shouldWork = false;
        this.backgroundPane.bindItem('S', GuiGlobalItems.backgroundItem(ItemType.BLACK_STAINED_GLASS_PANE));
        this.backgroundPane.bindItem('L', GuiGlobalItems.backgroundItem(ItemType.BLACK_STAINED_GLASS_PANE));
        this.backgroundPane.bindItem('R', GuiGlobalItems.backgroundItem(ItemType.BLACK_STAINED_GLASS_PANE));

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
        if (!this.shouldWork) {
            humanEntity.closeInventory();
            return;
        }

        super.show(humanEntity);
    }

}
