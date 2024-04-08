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
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class MappedElementListConfigGui< T, S extends ElementMappedToListGui> extends ElementListConfigGui< T > {


    protected final HashMap<T, S> elementGuiMap;
    public MappedElementListConfigGui(@NotNull String title) {
        super(title);
        this.elementGuiMap = new HashMap<>();

    }

    @Override
    protected GuiItem prepareCreateNewItem(){
        // Create new conflict item
        ItemStack createItem = new ItemStack(Material.PAPER);
        ItemMeta createMeta = createItem.getItemMeta();

        createMeta.setDisplayName("\u00A7aCreate new "+genericDisplayedName());
        createMeta.setLore(Arrays.asList(
                "\u00A77Create a new "+genericDisplayedName()+".",
                "\u00A77You will be asked to name the "+genericDisplayedName()+" in chat.",
                "\u00A77Then, you should edit the "+genericDisplayedName()+" config as you need"
        ));

        createItem.setItemMeta(createMeta);

        return new GuiItem(createItem, (clickEvent) -> {
            clickEvent.setCancelled(true);
            HumanEntity player = clickEvent.getWhoClicked();

            // check permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.closeInventory();
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }
            player.closeInventory();

            player.sendMessage("\u00A7eWrite the "+genericDisplayedName()+" name you want to create in the chat.\n" +
                    "\u00A7eOr write \u00A7ccancel \u00A7eto go back to "+genericDisplayedName()+" config menu");

            CustomAnvil.Companion.getChatListener().setListenedCallback(player, prepareCreateItemConsumer(player));

        }, CustomAnvil.instance);
    }

    @Override
    public void reloadValues() {
        this.elementGuiMap.forEach((conflict, gui) -> gui.cleanUnused());
        this.elementGuiMap.clear();

        super.reloadValues();
    }

    @Override
    protected void updateGeneric(T generic, ItemStack usedItem) {
        S mapElement = this.elementGuiMap.get(generic);

        GuiItem guiItem;
        if (mapElement == null) {
            // Create new sub setting mapElement
            guiItem = new GuiItem(usedItem, CustomAnvil.instance);
            mapElement = newInstanceOfGui(generic, guiItem);

            guiItem.setAction(GuiGlobalActions.openGuiAction(mapElement.getMappedGui()));

            this.elementGuiMap.put(generic, mapElement);
            addToPage(guiItem);
        } else {
            // Replace item with the updated one
            guiItem = mapElement.getParentItemForThisGui();
            guiItem.setItem(usedItem);
        }
        mapElement.updateLocal();

    }

    @Override
    protected GuiItem findGuiItemForRemoval(T generic) {
        S mapElement = this.elementGuiMap.get(generic);
        if (mapElement == null) return null;

        this.elementGuiMap.remove(generic);
        return mapElement.getParentItemForThisGui();
    }

    protected Consumer<String> prepareCreateItemConsumer(HumanEntity player){
        AtomicReference<Consumer<String>> selfRef = new AtomicReference<>();
        Consumer<String> selfCallback = (message) -> {
            if (message == null) return;

            // check permission
            if (!player.hasPermission(CustomAnvil.editConfigPermission)) {
                player.sendMessage(GuiGlobalActions.NO_EDIT_PERM);
                return;
            }

            message = message.toLowerCase(Locale.ROOT);
            if ("cancel".equalsIgnoreCase(message)) {
                player.sendMessage(genericDisplayedName()+" creation cancelled...");
                show(player);
                return;
            }

            message = message.replace(' ', '_');

            // Try to find if it already exists in a for loop
            // Not the most efficient on large number of conflict, but it should not run often.
            for (T generic : getEveryDisplayableInstanceOfGeneric()) {
                if (generic.toString().equalsIgnoreCase(message)) {
                    player.sendMessage("\u00A7cPlease enter a "+genericDisplayedName()+" name that do not already exist...");
                    // wait next message.
                    CustomAnvil.Companion.getChatListener().setListenedCallback(player, selfRef.get());
                    return;
                }
            }

            T generic = createAndSaveNewEmptyGeneric(message);

            updateValueForGeneric(generic, true);

            // show the new conflict config to the player
            this.elementGuiMap.get(generic).getMappedGui().show(player);

            update();
        };

        selfRef.set(selfCallback);
        return selfCallback;
    }

    protected abstract S newInstanceOfGui(T generic, GuiItem item);

    protected abstract String genericDisplayedName();

    protected abstract T createAndSaveNewEmptyGeneric(String name);

}
