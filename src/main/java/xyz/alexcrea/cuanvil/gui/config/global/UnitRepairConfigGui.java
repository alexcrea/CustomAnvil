package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.ask.SelectItemTypeGui;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.UnitRepairElementListGui;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.MaterialUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class UnitRepairConfigGui extends
        MappedGuiListConfigGui<NamespacedKey, MappedGuiListConfigGui.LazyElement<UnitRepairElementListGui>> {

    private static UnitRepairConfigGui INSTANCE;

    @Nullable
    public static UnitRepairConfigGui getCurrentInstance() {
        return INSTANCE;
    }

    @NotNull
    public static UnitRepairConfigGui getInstance() {
        if (INSTANCE == null) INSTANCE = new UnitRepairConfigGui();

        return INSTANCE;
    }

    private UnitRepairConfigGui() {
        super("Unit Repair Config");

        init();
    }

    public UnitRepairConfigGui(Gui parent) {
        super("Unit Repair Config", parent);
    }

    @Override
    protected LazyElement<UnitRepairElementListGui> newInstanceOfGui(NamespacedKey material, GuiItem item) {
        return new LazyElement<>(item, () -> {
            UnitRepairElementListGui element = new UnitRepairElementListGui(material, this);
            element.init();
            return element;
        });
    }

    @Override
    protected ItemStack createItemForGeneric(@NotNull NamespacedKey material) {
        var unitConfig = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();
        var section = unitConfig.getConfigurationSection(material.toString().toLowerCase());
        var legacySection = unitConfig.getConfigurationSection(material.toString().toLowerCase());

        String materialName = CasedStringUtil.snakeToUpperSpacedCase(material.getKey().toLowerCase());

        var display = MaterialUtil.INSTANCE.getMatFromKey(material);

        if (display == null || display.isAir()) {
            display = Material.BARRIER;
        }

        var reparable = new HashSet<String>();
        if (section != null)
            reparable.addAll(section.getKeys(false));
        if (legacySection != null)
            reparable.addAll(legacySection.getKeys(false));

        var reparableItemCount = reparable.size();

        ItemStack item = new ItemStack(display);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§eRepaired by " + materialName);
        meta.setLore(Arrays.asList(
                "§7There is currently §e" + reparableItemCount + " §7reparable item with " + materialName,
                "§7Click here to open the menu to edit reparable item by " + materialName
        ));

        item.setItemMeta(meta);

        return item;
    }

    @Override
    protected Collection<NamespacedKey> getEveryInstanceOfGeneric() {
        var materials = new HashSet<NamespacedKey>();
        var config = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig();

        for (String matName : config.getKeys(false)) {
            if(!config.isConfigurationSection(matName)) continue;

            NamespacedKey material = NamespacedKey.fromString(matName.toLowerCase());
            if (material != null) {
                materials.add(material);
            }
        }
        return materials;
    }

    @Override
    protected GuiItem prepareCreateNewItem() {
        // Create new conflict item
        ItemStack createItem = new ItemStack(Material.PAPER);
        ItemMeta createMeta = createItem.getItemMeta();
        assert createMeta != null;

        createMeta.setDisplayName("§aSelect a new unit material");
        createMeta.setLore(Arrays.asList(
                "§7Select a new unit material to be used.",
                "§7You will be asked the material to use."
        ));

        createItem.setItemMeta(createMeta);

        return new GuiItem(createItem, clickEvent -> {
            clickEvent.setCancelled(true);

            new SelectItemTypeGui(
                    "Select unit repair item.",
                    "§7Click here with an item to set the item\n" +
                            "§7You like to be an unit repair item",
                    this,
                    (itemStack, player) -> {
                        NamespacedKey type = MaterialUtil.INSTANCE.getCustomType(itemStack);
                        // Add new material
                        updateValueForGeneric(type, true);

                        // Display material edit setting
                        this.elementGuiMap.get(type).get().getMappedGui().show(player);
                    },
                    true
            ).show(clickEvent.getWhoClicked());
        }, CustomAnvil.instance);
    }

    @NotNull
    public LazyElement<UnitRepairElementListGui> getInstanceOrCreate(NamespacedKey mat) {
        LazyElement<UnitRepairElementListGui> element = this.elementGuiMap.get(mat);
        if (element == null) {
            updateValueForGeneric(mat, false);

            element = this.elementGuiMap.get(mat);
        }

        return element;
    }

    @Override // Not used in this implementation.
    protected String genericDisplayedName() {
        return "this function Should not be used.";
    }

    @Override // Not used in this implementation.
    protected NamespacedKey createAndSaveNewEmptyGeneric(String name) {
        return null;
    }

}
