package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.delilaheve.CustomAnvil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.ask.SelectItemTypeGui;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.UnitRepairElementListGui;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ItemTypeUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@SuppressWarnings("UnstableApiUsage")
public class UnitRepairConfigGui extends
        MappedGuiListConfigGui<ItemType, MappedGuiListConfigGui.LazyElement<UnitRepairElementListGui>> {

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
    protected LazyElement<UnitRepairElementListGui> newInstanceOfGui(ItemType type, GuiItem item) {
        return new LazyElement<>(item, () -> {
            UnitRepairElementListGui element = new UnitRepairElementListGui(type, this);
            element.init();
            return element;
        });
    }

    private void aggregateFromSection(HashSet<ItemType> set, String sectionName){
        ConfigurationSection section = ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().getConfigurationSection(sectionName);
        if(section == null) return;

        for (String key : section.getKeys(false)) {
            ItemType type = ItemTypeUtil.INSTANCE.getItemTypeExact(key);
            if(type != null) set.add(type);
        }
    }

    private int numberOfChildren(ItemType type){
        HashSet<ItemType> set = new HashSet<>();

        aggregateFromSection(set, type.getKey().toString());
        aggregateFromSection(set, type.getKey().getKey());

        return set.size();
    }

    @Override
    protected ItemStack createItemForGeneric(ItemType type) {
        String typeName = CasedStringUtil.snakeToUpperSpacedCase(ItemTypeUtil.INSTANCE.name(type));

        if(type == ItemType.AIR){
            type = ItemType.BARRIER;
        }

        int reparableItemCount = numberOfChildren(type);

        ItemStack item = type.createItemStack();
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§eRepaired by " +typeName);
        meta.setLore(Arrays.asList(
                "§7There is currently §e" +reparableItemCount+ " §7reparable item with "+typeName,
                "§7Click here to open the menu to edit reparable item by " + typeName
        ));

        item.setItemMeta(meta);

        return item;
    }

    @Override
    protected Collection<ItemType> getEveryDisplayableInstanceOfGeneric() {
        HashSet<ItemType> types = new HashSet<>(); // we need set to avoid duplicate

        for (String typeName : ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().getKeys(false)) {
            ItemType type = ItemTypeUtil.INSTANCE.getItemTypeExact(typeName);
            if(type != null){
                types.add(type);
            }
        }
        return types;
    }

    @Override
    protected GuiItem prepareCreateNewItem() {
        // Create new conflict item
        ItemStack createItem = ItemType.PAPER.createItemStack();
        ItemMeta createMeta = createItem.getItemMeta();
        assert createMeta != null;

        createMeta.setDisplayName("§aSelect a new unit type");
        createMeta.setLore(Arrays.asList(
                "§7Select a new unit to be used.",
                "§7You will be asked the item/item type to use."
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
                        ItemType type = itemStack.getType().asItemType();
                        // Add new item type
                        updateValueForGeneric(type, true);

                        // Display item type edit setting
                        this.elementGuiMap.get(type).get().getMappedGui().show(player);
                    },
                    true
            ).show(clickEvent.getWhoClicked());
        }, CustomAnvil.instance);
    }

    @NotNull
    public LazyElement<UnitRepairElementListGui> getInstanceOrCreate(ItemType mat){
        LazyElement<UnitRepairElementListGui> element = this.elementGuiMap.get(mat);
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
    protected ItemType createAndSaveNewEmptyGeneric(String name) {
        return null;
    }
}
