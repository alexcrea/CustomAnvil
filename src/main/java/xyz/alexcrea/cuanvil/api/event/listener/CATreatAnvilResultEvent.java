package xyz.alexcrea.cuanvil.api.event.listener;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.anvil.AnvilCost;
import xyz.alexcrea.cuanvil.anvil.AnvilUseType;

/**
 * Called after custom anvil processed the click on the result on the anvil inventory.
 * This event should be used to modify the result of an anvil use.
 * <p>
 * You may also want to check {@link CAClickResultBypassEvent},
 * {@link CAPreAnvilBypassEvent}
 * and {@link CAEarlyPreAnvilBypassEvent} for your use case
 * <p>
 * A null result will cancel this event
 */
@SuppressWarnings({"unused", "UnstableApiUsage"})
public class CATreatAnvilResultEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    private final AnvilView view;

    private final AnvilUseType useType;

    @Nullable
    private final ItemStack left;
    @Nullable
    private final ItemStack right;

    @Nullable
    private ItemStack result;

    private final AnvilCost cost;

    public CATreatAnvilResultEvent(
            @NotNull AnvilView view,
            AnvilUseType useType,
            @Nullable ItemStack result,
            AnvilCost cost) {
        this.view = view;
        this.useType = useType;

        this.left = view.getItem(0);
        this.right = view.getItem(1);
        this.result = result;
        this.cost = cost;
    }

    /**
     * Get the bukkit inventory view.
     *
     * @return The inventory view of this event.
     */
    public @NotNull AnvilView getView() {
        return view;
    }


    /**
     * Get the type of use source of the result.
     *
     * @return The craft use type.
     */
    public AnvilUseType getUseType() {
        return useType;
    }

    /**
     * Get the left item of the anvil use
     *
     * @return the left item
     */
    public @Nullable ItemStack getLeftItem() {
        return left;
    }

    /**
     * Get the right item of the anvil use
     *
     * @return the right item
     */
    public @Nullable ItemStack getRightItem() {
        return right;
    }

    /**
     * Get the current result
     * <p>
     * note that it will not be null unless another listener previously set it to null.
     *
     * @return The current result.
     */
    public @Nullable ItemStack getResult() {
        return result;
    }

    /**
     * Set the current result
     * <p>
     * note that a null result will cancel this anvil use.
     *
     * @param result The new result
     */
    public void setResult(@Nullable ItemStack result) {
        this.result = result;
    }

    /**
     * Allow access to the current cost of the event
     * Note that modifying this object will change the event resulting cost
     *
     * <h3>Important note:</h3>
     * the final price are re calculated on click for the following use case:
     * <ul>
     * <li>Custom craft</li>
     * <li>Unit repair</li>
     * <li>Lore edit</li>
     * </ul>
     * This value will be used as final price for:
     * <li>Item merge</li>
     * <li>Item rename</li>
     *
     * @return the current anvil cost
     */
    public AnvilCost getCost() {
        return cost;
    }
}
