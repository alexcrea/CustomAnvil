package xyz.alexcrea.cuanvil.util;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AnvilClickTestData(
        @Nullable ItemStack leftItem,
        @Nullable ItemStack rightItem,
        @Nullable ItemStack resultSlotItem,
        @Nullable ItemStack expectedCursor,
        int levelCost,
        @Nullable Event.Result expectedResult,
        boolean testNoLevelNoChange,
        @Nullable Event.Result npChangeResult
) {

    public AnvilClickTestData(@Nullable ItemStack leftItem,
                              @Nullable ItemStack rightItem,
                              @Nullable ItemStack resultSlotItem,
                              @Nullable ItemStack expectedCursor,
                              int levelCost,
                              @Nullable Event.Result expectedResult) {
        this(leftItem, rightItem, resultSlotItem,
                expectedCursor, levelCost, expectedResult,
                false, null);
    }

    public AnvilClickTestData(@Nullable ItemStack leftItem,
                              @Nullable ItemStack rightItem,
                              @Nullable ItemStack resultSlotItem,
                              @Nullable ItemStack expectedCursor,
                              int levelCost) {
        this(leftItem, rightItem, resultSlotItem,
                expectedCursor, levelCost, null);
    }

    public AnvilClickTestData(@Nullable ItemStack expectedCursor,
                              int levelCost,
                              @Nullable Event.Result expectedResult) {
        this(null, null, null,
                expectedCursor, levelCost, expectedResult,
                false, null);
    }

    public AnvilClickTestData(@Nullable ItemStack expectedCursor,
                              int levelCost) {
        this(expectedCursor, levelCost, null);
    }
}
