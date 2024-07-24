package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.ask.SelectItemTypeGui;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.UnitRepairElementListGui;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class UnitRepairConfigGui extends MappedGuiListConfigGui<Material, UnitRepairElementListGui> {

    private static UnitRepairConfigGui INSTANCE;

    @Nullable
    public static UnitRepairConfigGui getCurrentInstance(){
        return INSTANCE;
    }

    @NotNull
    public static UnitRepairConfigGui getInstance(){
        if(INSTANCE == null) INSTANCE = new UnitRepairConfigGui();

        return INSTANCE;
    }

    private UnitRepairConfigGui() {
        super("Unit Repair Config");

        init();
    }

    @Override
    protected UnitRepairElementListGui newInstanceOfGui(Material material, GuiItem item) {
        UnitRepairElementListGui element = new UnitRepairElementListGui(material, this, item);
        element.init();
        return element;
    }

    @Override
    protected ItemStack createItemForGeneric(Material material) {
        ConfigurationSection materialSection = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().getConfigurationSection(material.name().toLowerCase());
        String materialName = CasedStringUtil.snakeToUpperSpacedCase(material.name().toLowerCase());

        if(material.isAir()){
            material = Material.BARRIER;
        }

        int reparableItemCount = materialSection == null ? 0 : materialSection.getKeys(false).size(); // Probably an expensive call but... why not

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("\u00A7eRepaired by " +materialName);
        meta.setLore(Arrays.asList(
                "\u00A77There is currently \u00A7e" +reparableItemCount+ " \u00A77reparable item with "+materialName,
                "\u00A77Click here to open the menu to edit reparable item by " + materialName
        ));

        item.setItemMeta(meta);

        return item;
    }

    @Override
    protected Collection<Material> getEveryDisplayableInstanceOfGeneric() {
        ArrayList<Material> materials = new ArrayList<>();

        for (String matName : ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().getKeys(false)) {
            Material mat = Material.getMaterial(matName.toUpperCase());
            if(mat != null){
                materials.add(mat);
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

        createMeta.setDisplayName("\u00A7aSelect a new unit material");
        createMeta.setLore(Arrays.asList(
                "\u00A77Select a new unit material to be used.",
                "\u00A77You will be asked the material to use."
        ));

        createItem.setItemMeta(createMeta);

        return new GuiItem(createItem, clickEvent -> {
            clickEvent.setCancelled(true);

            new SelectItemTypeGui(
                    "Select unit repair item.",
                    "\u00A77Click here with an item to set the item\n" +
                            "\u00A77You like to be an unit repair item",
                    this,
                    (itemStack, player) -> {
                        Material type = itemStack.getType();
                        // Add new material
                        updateValueForGeneric(type, true);

                        // Display material edit setting
                        this.elementGuiMap.get(type).getMappedGui().show(player);
                    },
                    true
            ).show(clickEvent.getWhoClicked());
        }, CustomAnvil.instance);
    }

    @NotNull
    public UnitRepairElementListGui getInstanceOrCreate(Material mat){
        UnitRepairElementListGui element = this.elementGuiMap.get(mat);
        if(element == null){
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
    protected Material createAndSaveNewEmptyGeneric(String name) {
        return null;
    }
}
