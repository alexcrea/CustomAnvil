package xyz.alexcrea.cuanvil.mock;

import be.seeseemelk.mockbukkit.inventory.PlayerInventoryViewMock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"removal"})
public class AnvilViewMock extends PlayerInventoryViewMock implements AnvilView {

    private @NotNull AnvilInventory top;

    /**
     * Constructs a new {@link PlayerInventoryViewMock} for the provided player, with the specified top inventory.
     *
     * @param player The player to create the view for.
     * @param top    The top inventory.
     */
    public AnvilViewMock(@NotNull HumanEntity player, @NotNull AnvilInventory top) {
        super(player, top);
        this.top = top;
    }

    @Override
    public @Nullable String getRenameText() {
        return top.getRenameText();
    }

    @Override
    public int getRepairItemCountCost() {
        return top.getRepairCostAmount();
    }

    @Override
    public int getRepairCost() {
        return top.getRepairCost();
    }

    @Override
    public int getMaximumRepairCost() {
        return top.getMaximumRepairCost();
    }

    @Override
    public void setRepairItemCountCost(int amount) {
        top.setRepairCostAmount(amount);
    }

    @Override
    public void setRepairCost(int cost) {
        top.setRepairCost(cost);
    }

    @Override
    public void setMaximumRepairCost(int levels) {
        top.setMaximumRepairCost(levels);
    }

    @Override
    public boolean bypassesEnchantmentLevelRestriction() {
        throw new UnsupportedOperationException("Custom anvil was not think with this existing");
    }

    @Override
    public void bypassEnchantmentLevelRestriction(boolean bypassEnchantmentLevelRestriction) {
        throw new UnsupportedOperationException("Custom anvil was not think with this existing");
    }

    @Override
    public @NotNull AnvilInventory getTopInventory() {
        return top;
    }

}
