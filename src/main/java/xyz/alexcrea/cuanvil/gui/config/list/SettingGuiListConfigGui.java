package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.config.MainConfigGui;
import xyz.alexcrea.cuanvil.gui.config.settings.SettingGui;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public abstract class SettingGuiListConfigGui< T, S extends SettingGui.SettingGuiFactory> extends ElementListConfigGui< T >{

    protected HashMap<T, GuiItem> guiItemMap;
    protected HashMap<T, S> factoryMap;
    protected SettingGuiListConfigGui(@NotNull String title, Gui parent) {
        super(title, parent);
        this.guiItemMap = new HashMap<>();
        this.factoryMap = new HashMap<>();
    }

    protected SettingGuiListConfigGui(@NotNull String title) {
        this(title, MainConfigGui.getInstance());
    }

    @Override
    protected GuiItem prepareCreateNewItem() {
        ItemStack createItem = new ItemStack(Material.PAPER);
        ItemMeta createMeta = createItem.getItemMeta();
        assert createMeta != null;

        createMeta.setDisplayName(createItemName());
        createMeta.setLore(getCreateItemLore());

        createItem.setItemMeta(createMeta);
        return new GuiItem(createItem, getCreateClickConsumer(), CustomAnvil.instance);
    }

    @Override
    public void updateValueForGeneric(T generic, boolean shouldUpdate) {
        if(!this.factoryMap.containsKey(generic)){
            // Create new item & factory
            S factory = createFactory(generic);
            GuiItem newItem = itemFromFactory(generic, factory);

            addToPage(newItem);
            this.guiItemMap.put(generic, newItem);
            this.factoryMap.put(generic, factory);
        }else{
            S factory = this.factoryMap.get(generic);
            // Update old item
            GuiItem oldItem = this.guiItemMap.get(generic);

            GuiItem newItem = itemFromFactory(generic, factory);
            updateGuiItem(oldItem, newItem);
        }

        if(shouldUpdate){
            update();
        }
    }

    @Override
    protected void reloadValues() {
        this.guiItemMap.clear();
        this.factoryMap.clear();

        super.reloadValues();
    }

    private void updateGuiItem(GuiItem oldITem, GuiItem newItem){
        oldITem.setItem(newItem.getItem());
        oldITem.setProperties(newItem.getProperties());
        oldITem.setVisible(newItem.isVisible());
    }

    @Override
    protected GuiItem findGuiItemForRemoval(T generic) {
        return this.guiItemMap.get(generic);
    }

    @Override // Not used
    protected void updateGeneric(T generic, ItemStack usedItem) {}
    @Override // Not used
    protected ItemStack createItemForGeneric(T generic) {
        return null;
    }

    protected abstract List<String> getCreateItemLore();
    protected abstract Consumer<InventoryClickEvent> getCreateClickConsumer();
    protected abstract String createItemName();

    protected abstract S createFactory(T generic);
    protected abstract GuiItem itemFromFactory(T generic, S factory);


}
