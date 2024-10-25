package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.delilaheve.CustomAnvil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.config.list.elements.ElementMappedToListGui;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;
import xyz.alexcrea.cuanvil.util.LazyValue;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class MappedGuiListConfigGui< T, S extends MappedGuiListConfigGui.LazyElement<?>>
        extends MappedElementListConfigGui< T, S > {

    protected MappedGuiListConfigGui(@NotNull String title) {
        super(title);

    }

    @Override
    public void reloadValues() {
        this.elementGuiMap.forEach((conflict, element) -> {
            ElementMappedToListGui gui = element.getStored();
            if(gui != null) gui.cleanAndBeUnusable();
        });
        this.elementGuiMap.clear();

        super.reloadValues();
    }

    @Override
    protected S newElementRequested(T generic, GuiItem newItem) {
        S element = newInstanceOfGui(generic, newItem);

        newItem.setAction(element.openAction());
        return element;
    }

    @Override
    protected GuiItem findItemFromElement(T generic, S element) {
        return element.getParentItem();
    }

    @Override
    protected void updateElement(T generic, S element) {
        ElementMappedToListGui gui = element.getStored();
        if(gui != null) gui.updateLocal();
    }

    @Override
    protected GuiItem findGuiItemForRemoval(T generic, S element) {
        return element.getParentItem();
    }

    @Override
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
                    player.sendMessage("§cPlease enter a "+genericDisplayedName()+" name that do not already exist...");
                    // wait next message.
                    CustomAnvil.Companion.getChatListener().setListenedCallback(player, selfRef.get());
                    return;
                }
            }

            T generic = createAndSaveNewEmptyGeneric(message);
            if(generic == null) {// we don't know what to do. so we back up by opening this gui.
                this.show(player);
                return;
            }

            updateValueForGeneric(generic, true);

            // show the new conflict config to the player
            this.elementGuiMap.get(generic).get().getMappedGui().show(player);

            update();
        };

        selfRef.set(selfCallback);
        return selfCallback;
    }

    protected abstract S newInstanceOfGui(T generic, GuiItem item);

    protected abstract String genericDisplayedName();

    protected abstract T createAndSaveNewEmptyGeneric(String name);

    public static class LazyElement<T extends ElementMappedToListGui> extends LazyValue<T> {

        private final GuiItem parentItem;
        private final LazyValue<Consumer<InventoryClickEvent>> lazyOpenConsumer;
        public LazyElement(GuiItem parentItem, Supplier<T> valueSupplier) {
            super(valueSupplier);
            this.parentItem = parentItem;

            this.lazyOpenConsumer = new LazyValue<>(() ->
                    GuiGlobalActions.openGuiAction(this.get().getMappedGui()))
            ;
        }

        public GuiItem getParentItem() {
            return parentItem;
        }

        @NotNull
        public Consumer<InventoryClickEvent> openAction(){
            return event -> lazyOpenConsumer.get().accept(event);
        }

    }

}
