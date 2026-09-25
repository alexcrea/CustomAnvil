package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.ask.SelectItemTypeGui;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.UnitRepairElementListGui;
import xyz.alexcrea.cuanvil.lang.Message;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;
import xyz.alexcrea.cuanvil.util.ComponentUtil;
import xyz.alexcrea.cuanvil.util.MaterialUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NotNullByDefault
public class UnitRepairConfigGui extends
        MappedGuiListConfigGui<NamespacedKey, MappedGuiListConfigGui.LazyElement<UnitRepairElementListGui>> {

    //TODO #130 part 3
    private static @Nullable UnitRepairConfigGui INSTANCE;

    @Nullable
    public static UnitRepairConfigGui getCurrentInstance() {
        return INSTANCE;
    }

    public static UnitRepairConfigGui getInstance() {
        if(INSTANCE == null) INSTANCE = new UnitRepairConfigGui();

        return INSTANCE;
    }

    private UnitRepairConfigGui() {
        super(MsgUI.INSTANCE.getUNIT_REPAIR_TITLE());

        init();
    }

    public UnitRepairConfigGui(Gui parent) {
        super(MsgUI.INSTANCE.getUNIT_REPAIR_TITLE(), parent);
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
    protected ItemStack createItemForGeneric(NamespacedKey material) {
        Set<String> reparable;
        try(var lock = ConfigHolder.UNIT_REPAIR.write) {
            reparable = getConfiguredValues(lock.get(), material);
        }

        var display = MaterialUtil.INSTANCE.getMatFromKey(material);

        if(display == null || display.isAir()) {
            display = Material.BARRIER;
        }

        String materialName = CasedStringUtil.snakeToUpperSpacedCase(material.getKey().toLowerCase());
        var reparableItemCount = reparable.size();

        ItemStack item = new ItemStack(display);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ComponentUtil.setMessageName(meta, MsgUI.UNIT_REPAIR_ELEMENT_NAME, materialName);
        ComponentUtil.applyLore(meta, MsgUI.UNIT_REPAIR_ELEMENT_LORE, materialName, reparableItemCount);

        item.setItemMeta(meta);

        return item;
    }

    private HashSet<String> getConfiguredValues(ConfigHolder.UnitRepairHolder holder, NamespacedKey material) {
        var result = new HashSet<String>();
        var unitConfig = holder.getConfig();
        var section = unitConfig.getConfigurationSection(material.toString().toLowerCase());
        var legacySection = unitConfig.getConfigurationSection(material.getKey().toLowerCase());

        if(section != null)
            result.addAll(section.getKeys(false));
        if(legacySection != null)
            result.addAll(legacySection.getKeys(false));

        return result;
    }

    @Override
    protected Collection<NamespacedKey> getEveryInstanceOfGeneric() {
        var materials = new HashSet<NamespacedKey>();
        try(var lock = ConfigHolder.UNIT_REPAIR.read) {
            var holder = lock.get();
            var config = holder.getConfig();

            for(String matName : config.getKeys(false)) {
                if(!config.isConfigurationSection(matName)) continue;

                NamespacedKey material = NamespacedKey.fromString(matName.toLowerCase());
                if(material != null) {
                    materials.add(material);
                }
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

        ComponentUtil.setMessageName(createMeta, MsgUI.UNIT_REPAIR_NEW_NAME);
        ComponentUtil.applyLore(createMeta, MsgUI.UNIT_REPAIR_NEW_LORE);

        createItem.setItemMeta(createMeta);

        return new GuiItem(createItem, clickEvent -> {
            clickEvent.setCancelled(true);

            new SelectItemTypeGui(
                    MsgUI.UNIT_REPAIR_NEW_TITLE, "",
                    MsgUI.UNIT_REPAIR_NEW_DESCRIPTION, "",
                    this,
                    (itemStack, player) -> {
                        NamespacedKey type = MaterialUtil.getCustomType(itemStack);
                        // Add new material
                        updateValueForGeneric(type, true);

                        // Display material edit setting
                        this.elementGuiMap.get(type).get().getMappedGui().show(player);
                    },
                    true
            ).show(clickEvent.getWhoClicked());
        }, CustomAnvil.instance);
    }

    public LazyElement<UnitRepairElementListGui> getInstanceOrCreate(NamespacedKey mat) {
        LazyElement<UnitRepairElementListGui> element = this.elementGuiMap.get(mat);
        if(element == null) {
            updateValueForGeneric(mat, false);

            element = this.elementGuiMap.get(mat);
        }

        return element;
    }

    @Override
    protected Message genericDisplayedName() {
        throw new IllegalStateException("Using a method intended to not be used");
    }

    @Override
    protected NamespacedKey createAndSaveNewEmptyGeneric(String name) {
        throw new IllegalStateException("Using a method intended to not be used");
    }

}
