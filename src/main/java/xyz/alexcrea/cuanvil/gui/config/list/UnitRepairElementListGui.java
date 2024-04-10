package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.settings.DoubleSettingGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalItems;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class UnitRepairElementListGui extends SettingGuiListConfigGui<String, DoubleSettingGui.DoubleSettingFactory> implements ElementMappedToListGui {

    private final GuiItem parentItem;
    private final Material material;
    private final String materialName;

    public UnitRepairElementListGui(@NotNull Material material,
                                    @NotNull Gui parentGui,
                                    @NotNull GuiItem parentItem) {
        super("\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(material.name().toLowerCase()) + " \u00A7rUnit repair");
        this.parentItem = parentItem;
        this.material = material;
        this.materialName = CasedStringUtil.snakeToUpperSpacedCase(material.name().toLowerCase());

        GuiGlobalItems.addBackItem(this.backgroundPane, parentGui);
    }

    // SettingGuiListConfigGui methods
    @Override
    protected List<String> getCreateItemLore() {
        return Arrays.asList(
                "a",
                "b",
                "c"
        );
    }

    @Override
    protected Consumer<InventoryClickEvent> getCreateClickConsumer() {
        return event -> {
            event.setCancelled(true);

            event.getWhoClicked().sendMessage("todo");
        };
    }

    @Override
    protected String createItemName() {
        return "\u00A7aAdd a new item reparable by " + this.materialName;
    }

    @Override
    protected DoubleSettingGui.DoubleSettingFactory createFactory(String materialName) {
        return DoubleSettingGui.doubleFactory(
                "\u00A70%\u00A78" +CasedStringUtil.snakeToUpperSpacedCase(materialName)+" Repair",
                this,
                material.name()+"."+materialName,
                ConfigHolder.UNIT_REPAIR_HOLDER,
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
                "\u00A77%\u00A7a" + CasedStringUtil.snakeToUpperSpacedCase(materialName)+ " \u00A7erepaired by \u00A7a" + this.materialName);
    }

    @Override
    protected List<String> getEveryDisplayableInstanceOfGeneric() {
        ArrayList<String> keys = new ArrayList<>();

        ConfigurationSection materialSection = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().getConfigurationSection(material.name().toLowerCase());
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

    // ElementMappedToListGui methods

    @Override
    public GuiItem getParentItemForThisGui() {
        return this.parentItem;
    }

    @Override // Not used in this implementation
    public void updateLocal() {}

    @Override
    public void cleanAndBeUnusable() {
        // TODO
    }

    @Override
    public Gui getMappedGui() {
        return this;
    }

}
