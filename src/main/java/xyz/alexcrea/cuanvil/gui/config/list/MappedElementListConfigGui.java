package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.delilaheve.CustomAnvil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

public abstract class MappedElementListConfigGui< T, S > extends ElementListConfigGui< T > {

    protected final HashMap<T, S> elementGuiMap;
    protected MappedElementListConfigGui(@NotNull String title) {
        super(title);
        this.elementGuiMap = new HashMap<>();

    }

    @Override
    protected GuiItem prepareCreateNewItem(){
        // Create new conflict item
        ItemStack createItem = new ItemStack(Material.PAPER);
        ItemMeta createMeta = createItem.getItemMeta();
        assert createMeta != null;

        createMeta.setDisplayName("§aCreate new "+genericDisplayedName());
        createMeta.setLore(Arrays.asList(
                "§7Create a new "+genericDisplayedName()+".",
                "§7You will be asked to name the "+genericDisplayedName()+" in chat.",
                "§7Then, you should edit the "+genericDisplayedName()+" config as you need"
        ));

        createItem.setItemMeta(createMeta);

        return new GuiItem(createItem, clickEvent -> {
            clickEvent.setCancelled(true);
            HumanEntity player = clickEvent.getWhoClicked();

            // check permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }
            player.closeInventory();

            player.sendMessage("§eWrite the "+genericDisplayedName()+" name you want to create in the chat.\n" +
                    "§eOr write §ccancel §eto go back to "+genericDisplayedName()+" config menu");

            CustomAnvil.Companion.getChatListener().setListenedCallback(player, prepareCreateItemConsumer(player));

        }, CustomAnvil.instance);
    }

    @Override
    protected void updateGeneric(T generic, ItemStack usedItem) {
        S element = this.elementGuiMap.get(generic);

        GuiItem guiItem;
        if (element == null) {
            // Create new sub setting element
            guiItem = new GuiItem(usedItem, CustomAnvil.instance);

            element = newElementRequested(generic, guiItem);

            this.elementGuiMap.put(generic, element);

            addToPage(guiItem);
        } else {
            // Replace item with the updated one
            guiItem = findItemFromElement(generic, element);
            guiItem.setItem(usedItem);
        }
        updateElement(generic, element);

    }

    protected abstract void updateElement(T generic, S element);

    protected abstract S newElementRequested(T generic, GuiItem newItem);

    protected abstract GuiItem findItemFromElement(T generic, S element);

    @Override
    protected GuiItem findGuiItemForRemoval(T generic) {
        S element = this.elementGuiMap.get(generic);
        if (element == null) return null;

        this.elementGuiMap.remove(generic);
        return findGuiItemForRemoval(generic, element);
    }

    protected abstract GuiItem findGuiItemForRemoval(T generic, S element);

    protected abstract Consumer<String> prepareCreateItemConsumer(HumanEntity player);

    protected abstract String genericDisplayedName();

}
