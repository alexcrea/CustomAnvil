package xyz.alexcrea.cuanvil.api.event.listener;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.util.AnvilUseType;

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

    private int levelCost;

    public CATreatAnvilResultEvent(@NotNull PrepareAnvilEvent event, AnvilUseType useType, @Nullable ItemStack result, int levelCost) {
        this.event = event;
        this.useType = useType;
        this.result = result;
        this.levelCost = levelCost;
    }

    public @NotNull PrepareAnvilEvent getEvent() {
        return event;
    }

    public AnvilUseType getUseType() {
        return useType;
    }

    public @Nullable ItemStack getResult() {
        return result;
    }

    public void setResult(@Nullable ItemStack result) {
        this.result = result;
    }

    public int getLevelCost() {
        return levelCost;
    }

    public void setLevelCost(int levelCost) {
        this.levelCost = levelCost;
    }
}
