package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.ask.SelectItemTypeGui;
import xyz.alexcrea.cuanvil.gui.config.global.UnitRepairConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.elements.ElementMappedToListGui;
import xyz.alexcrea.cuanvil.gui.config.settings.DoubleSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.gui.util.GuiSharedConstant;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.MaterialUtil;

import java.util.*;
import java.util.function.Consumer;

public class UnitRepairElementListGui extends SettingGuiListConfigGui<NamespacedKey, DoubleSettingGui.DoubleSettingFactory> implements ElementMappedToListGui {

    private final NamespacedKey parentMaterial;
    private final UnitRepairConfigGui parentGui;
    private final String materialName;

    private boolean shouldWork = true;

    private static String prettifiedName(NamespacedKey parentMaterial) {
        return CasedStringUtil.snakeToUpperSpacedCase(parentMaterial.getKey().toLowerCase());
    }

    public UnitRepairElementListGui(@NotNull NamespacedKey parentMaterial,
                                    @NotNull UnitRepairConfigGui parentGui) {
        super(MsgUI.INSTANCE.getUNIT_REPAIR_ELEMENT_TITLE(), prettifiedName(parentMaterial));
        this.parentMaterial = parentMaterial;
        this.parentGui = parentGui;
        this.materialName = prettifiedName(parentMaterial);

        GuiGlobalItems.addBackItem(this.backgroundPane, parentGui);
    }

    // SettingGuiListConfigGui methods
    @Override
    protected List<String> getCreateItemLore() {
        return Arrays.asList(//TODO MESSAGE
                "§7Select a new item to be repairable.",
                "§7You will be asked the material to use."
        );
    }

    @Override
    protected Consumer<InventoryClickEvent> getCreateClickConsumer() {
        return event -> {
            event.setCancelled(true);
            if(!this.shouldWork) {
                return;
            }
            event.setCancelled(true);

            new SelectItemTypeGui(
                    MsgUI.INSTANCE.getUNIT_REPAIR_NEW_ELEMENT_TITLE(), this.materialName,
                    MsgUI.INSTANCE.getUNIT_REPAIR_NEW_ELEMENT_DESCRIPTION(), this.materialName,
                    this,
                    (itemStack, player) -> {
                        ItemMeta meta = itemStack.getItemMeta();
                        NamespacedKey type = MaterialUtil.INSTANCE.getCustomType(itemStack);

                        if(!(meta instanceof Damageable)) {
                            MsgUI.INSTANCE.getUNIT_REPAIR_NEW_ELEMENT_CANNOT_REPAIR().send(player);
                            return;
                        }
                        if(type.equals(this.parentMaterial)) {
                            MsgUI.INSTANCE.getUNIT_REPAIR_NEW_ELEMENT_SAME_TYPE().send(player);
                            return;
                        }

                        String materialName = type.toString();

                        // Add new material
                        ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().set(parentMaterial.toString().toLowerCase() + "." + materialName, 0.25);

                        if(GuiSharedConstant.TEMPORARY_DO_SAVE_TO_DISK_EVERY_CHANGE) {
                            ConfigHolder.UNIT_REPAIR_HOLDER.saveToDisk(GuiSharedConstant.TEMPORARY_DO_BACKUP_EVERY_SAVE);
                        }

                        // Update gui
                        updateValueForGeneric(type, true);
                        this.parentGui.updateValueForGeneric(this.parentMaterial, true);


                        // Display material edit setting
                        this.factoryMap.get(type).create().show(player);
                    },
                    true
            ).show(event.getWhoClicked());

        };
    }

    @Override
    protected String createItemName() {
        return "§aAdd a new item reparable by " + this.materialName; //TODO MESSAGE ?
    }

    @Override
    protected DoubleSettingGui.DoubleSettingFactory createFactory(NamespacedKey materialName) {
        String materialDisplayName = CasedStringUtil.snakeToUpperSpacedCase(materialName.getKey());

        return new DoubleSettingGui.DoubleSettingFactory(
                MsgUI.INSTANCE.getUNIT_REPAIR_ELEMENT_VALUE_TITLE(),
                this,
                ConfigHolder.UNIT_REPAIR_HOLDER,
                this.parentMaterial.toString().toLowerCase() + "." + materialName,
                MsgUI.INSTANCE.getUNIT_REPAIR_ELEMENT_VALUE_DESCRIPTION(),
                materialDisplayName, this.materialName,
                2,
                true, true,
                0,
                1,
                0.25,
                0.01, 0.05, 0.25
        );
    }

    @Override
    protected GuiItem itemFromFactory(NamespacedKey materialName, DoubleSettingGui.DoubleSettingFactory factory) {
        return factory.getItem(
                materialFromName(materialName),
                MsgUI.INSTANCE.getUNIT_REPAIR_ITEM(),
                CasedStringUtil.snakeToUpperSpacedCase(materialName.getKey()),
                this.materialName
        );
    }

    @Override
    protected Collection<NamespacedKey> getEveryInstanceOfGeneric() {
        Set<NamespacedKey> keys = new HashSet<>();
        if(!this.shouldWork) {
            return keys;
        }

        ConfigurationSection legacySection = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().getConfigurationSection(parentMaterial.getKey().toLowerCase());
        ConfigurationSection materialSection = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().getConfigurationSection(parentMaterial.toString().toLowerCase());

        addAllKeys(legacySection, keys);
        addAllKeys(materialSection, keys);

        return keys;
    }

    private void addAllKeys(@Nullable ConfigurationSection section, @NotNull Set<NamespacedKey> keys) {
        if(section == null) return;
        for(var key : section.getKeys(false)) {
            var material = NamespacedKey.fromString(key);
            if(material == null) continue;

            keys.add(material);
        }
    }

    private Material materialFromName(NamespacedKey material) {
        Material mat = MaterialUtil.INSTANCE.getMatFromKey(material);
        if(mat == null || mat.isAir()) return Material.BARRIER;
        return mat;
    }

    @Override
    public void updateGuiValues() {
        super.updateGuiValues();
        this.parentGui.updateValueForGeneric(this.parentMaterial, true);
    }

    // ElementMappedToListGui methods

    @Override // Not used in this implementation
    public void updateLocal() {
    }

    @Override
    public void cleanAndBeUnusable() {
        this.shouldWork = false;
        this.backgroundPane.bindItem('S', GuiGlobalItems.backgroundItem(Material.BLACK_STAINED_GLASS_PANE));
        this.backgroundPane.bindItem('L', GuiGlobalItems.backgroundItem(Material.BLACK_STAINED_GLASS_PANE));
        this.backgroundPane.bindItem('R', GuiGlobalItems.backgroundItem(Material.BLACK_STAINED_GLASS_PANE));

        for(HumanEntity viewer : getViewers()) {
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
        if(!this.shouldWork) {
            humanEntity.closeInventory();
            return;
        }
        super.show(humanEntity);
    }

}
