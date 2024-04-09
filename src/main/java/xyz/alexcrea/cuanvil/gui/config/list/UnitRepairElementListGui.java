package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.global.EnchantCostConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.DoubleSettingGui;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class UnitRepairElementListGui extends SettingGuiListConfigGui<String, DoubleSettingGui.DoubleSettingFactory> implements ElementMappedToListGui {




    private final GuiItem parentItem;
    private final Material material;
    private final String materialName;

    public UnitRepairElementListGui(@NotNull Material material, GuiItem parentItem) {
        super("\u00A7e" + CasedStringUtil.snakeToUpperSpacedCase(material.name().toLowerCase()) + " \u00A7rUnit repair config");
        this.parentItem = parentItem;
        this.material = material;
        this.materialName = CasedStringUtil.snakeToUpperSpacedCase(material.name().toLowerCase());

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
    protected String genericDisplayedName() {
        return this.materialName+"`s Unit Repair Item";
    }

    @Override
    protected DoubleSettingGui.DoubleSettingFactory createFactory(String generic) {
        return null; //TODO
    }

    @Override
    protected GuiItem itemFromFactory(DoubleSettingGui.DoubleSettingFactory factory) {
        return new GuiItem(new ItemStack(Material.STONE), CustomAnvil.instance); //TODO correct item
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

    // ElementMappedToListGui methods

    @Override
    public GuiItem getParentItemForThisGui() {
        return this.parentItem;
    }

    @Override
    public void updateLocal() {

    }

    @Override
    public void cleanAndBeUnusable() {

    }

    @Override
    public Gui getMappedGui() {
        return this;
    }
}
