package xyz.alexcrea.cuanvil.api.event.listener;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.util.AnvilUseType;
import xyz.alexcrea.cuanvil.util.AnvilXpUtil.AnvilCost;

/**
 * Called after custom anvil processed the click on the result on the anvil inventory.
 * This event should be used to modify the result of an anvil use.
 * <p>
 * You may also want to check {@link CAClickResultBypassEvent},
 * {@link CAPreAnvilBypassEvent}
 * and {@link CAEarlyPreAnvilBypassEvent} for your use case
 * <p>
 * A null result will cancel this pre anvil event
 */
@SuppressWarnings("unused")
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
    private final PrepareAnvilEvent event;

    private final AnvilUseType useType;

    @Nullable
    private ItemStack result;

    private final AnvilCost cost;

    public CATreatAnvilResultEvent(@NotNull PrepareAnvilEvent event, AnvilUseType useType, @Nullable ItemStack result, AnvilCost cost) {
        this.event = event;
        this.useType = useType;
        this.result = result;
        this.cost = cost;
    }

    /**
     * Get the bukkit inventory click event causing to this event.
     *
     * @return The click event causing to this event.
     */
    public @NotNull PrepareAnvilEvent getEvent() {
        return event;
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
     * Get the level cost displayed on the anvil.
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
     * </ul>
     *
     * @deprecated use #{@link #getCost()} instead
     * @return The current cost.
     */
    @Deprecated(forRemoval = true, since = "1.17.0")
    public int getLevelCost() {
        return cost.sum();
    }

    /**
     * Set the level cost displayed on the anvil.
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
     * </ul>
     *
     * @deprecated use #{@link #getCost()} and set value on this instead
     * @param levelCost The new cost.
     */
    @Deprecated(forRemoval = true, since = "1.17.0")
    public void setLevelCost(int levelCost) {
        cost.setGeneric(levelCost - cost.getGeneric() - cost.sum());
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
