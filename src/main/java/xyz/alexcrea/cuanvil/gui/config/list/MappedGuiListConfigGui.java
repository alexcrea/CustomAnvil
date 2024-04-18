package xyz.alexcrea.cuanvil.gui.config.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.delilaheve.CustomAnvil;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.gui.util.GuiGlobalActions;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class MappedGuiListConfigGui< T, S extends ElementMappedToListGui> extends MappedElementListConfigGui< T, S > {

    public MappedGuiListConfigGui(@NotNull String title) {
        super(title);

    }

    @Override
    public void reloadValues() {
        this.elementGuiMap.forEach((conflict, gui) -> gui.cleanAndBeUnusable());
        this.elementGuiMap.clear();

        super.reloadValues();
    }

    @Override
    protected S newElementRequested(T generic, GuiItem newItem) {
        S element = newInstanceOfGui(generic, newItem);

        newItem.setAction(GuiGlobalActions.openGuiAction(element.getMappedGui()));
        return element;
    }

    @Override
    protected GuiItem findItemFromElement(T generic, S element) {
        return element.getParentItemForThisGui();
    }

    @Override
    protected void updateElement(T generic, S element) {
        element.updateLocal();
    }

    @Override
    protected GuiItem findGuiItemForRemoval(T generic, S element) {
        return element.getParentItemForThisGui();
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
                    player.sendMessage("\u00A7cPlease enter a "+genericDisplayedName()+" name that do not already exist...");
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
