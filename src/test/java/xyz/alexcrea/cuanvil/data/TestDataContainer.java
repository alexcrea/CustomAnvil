package xyz.alexcrea.cuanvil.data;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestUtil;

@SuppressWarnings("unused")
public record TestDataContainer(
        @NotNull AnvilFuseTestData fuseData,
        @Nullable AnvilClickTestData clickData
) {

    public void executeTest(AnvilInventory anvil, Player player) {
        executeFuseTest(anvil, player);
        if (clickData != null) executeClickTest(anvil, player);
    }

    public void executeFuseTest(AnvilInventory anvil, HumanEntity player) {
        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, fuseData);
    }

    public void executeClickTest(AnvilInventory anvil, Player player) {
        Assertions.assertNotNull(clickData);
        AnvilFuseTestUtil.executeAnvilClickTest(anvil, player, clickData);
    }

    public @NotNull TestDataContainer nullifyResult() {
        return new TestDataContainer(
                new AnvilFuseTestData(
                        fuseData.leftItem(), fuseData.rightItem(),
                        null
                ), null);
    }

    public @NotNull TestDataContainer setCost(
            @Nullable Integer priceAfterLeft,
            @Nullable Integer priceAfterRight,
            int priceAfterBoth
    ) {
        AnvilFuseTestData data = new AnvilFuseTestData(
                fuseData.leftItem(), fuseData.rightItem(), fuseData.expectedResult(),
                fuseData.expectedAfterLeftPlaced(),
                fuseData.expectedAfterRightPlaced(),
                priceAfterLeft,
                priceAfterRight,
                priceAfterBoth
        );

        AnvilClickTestData CData;
        if (clickData == null) {
            CData = null;
        } else {
            CData = new AnvilClickTestData(
                    clickData.leftItem(), clickData.rightItem(), clickData.resultSlotItem(),
                    clickData.expectedCursor(), priceAfterBoth,
                    clickData.expectedResult(),
                    clickData.testNoLevelNoChange(), clickData.npChangeResult()
            );
        }
        return new TestDataContainer(data, CData);
    }

    public @NotNull TestDataContainer setCost(
            int priceAfterBoth
    ) {
        return setCost(null, null, priceAfterBoth);
    }

    // Set fuse items
    public @NotNull TestDataContainer setFuseItems(@Nullable ItemStack left, @Nullable ItemStack right, @Nullable ItemStack expected) {
        AnvilFuseTestData data = new AnvilFuseTestData(
                left, right, expected,
                fuseData.expectedAfterLeftPlaced(),
                fuseData.expectedAfterRightPlaced(),
                fuseData.expectedPriceAfterLeftPlaced(),
                fuseData.expectedPriceAfterRightPlaced(),
                fuseData.expectedPriceAfterBothPlaced()
        );
        return new TestDataContainer(data, clickData);
    }

    public @NotNull TestDataContainer setFuseItems(
            @Nullable ItemStack left, @Nullable ItemStack right, @Nullable ItemStack expected,
            @Nullable ItemStack leftExpected, @Nullable ItemStack rightExpected) {
        AnvilFuseTestData data = new AnvilFuseTestData(
                left, right, expected,
                leftExpected,
                rightExpected,
                fuseData.expectedPriceAfterLeftPlaced(),
                fuseData.expectedPriceAfterRightPlaced(),
                fuseData.expectedPriceAfterBothPlaced()
        );
        return new TestDataContainer(data, clickData);
    }

    public @NotNull TestDataContainer setFuseLeft(@Nullable ItemStack left) {
        AnvilFuseTestData data = new AnvilFuseTestData(
                left, fuseData.rightItem(), fuseData.expectedResult(),
                fuseData.expectedAfterLeftPlaced(),
                fuseData.expectedAfterRightPlaced(),
                fuseData.expectedPriceAfterLeftPlaced(),
                fuseData.expectedPriceAfterRightPlaced(),
                fuseData.expectedPriceAfterBothPlaced()
        );
        return new TestDataContainer(data, clickData);
    }

    public @NotNull TestDataContainer setFuseRight(@Nullable ItemStack right) {
        AnvilFuseTestData data = new AnvilFuseTestData(
                fuseData.leftItem(), right, fuseData.expectedResult(),
                fuseData.expectedAfterLeftPlaced(),
                fuseData.expectedAfterRightPlaced(),
                fuseData.expectedPriceAfterLeftPlaced(),
                fuseData.expectedPriceAfterRightPlaced(),
                fuseData.expectedPriceAfterBothPlaced()
        );
        return new TestDataContainer(data, clickData);
    }

    public @NotNull TestDataContainer setFuseExpected(@Nullable ItemStack expected) {
        AnvilFuseTestData data = new AnvilFuseTestData(
                fuseData.leftItem(), fuseData.rightItem(), expected,
                fuseData.expectedAfterLeftPlaced(),
                fuseData.expectedAfterRightPlaced(),
                fuseData.expectedPriceAfterLeftPlaced(),
                fuseData.expectedPriceAfterRightPlaced(),
                fuseData.expectedPriceAfterBothPlaced()
        );
        return new TestDataContainer(data, clickData);
    }

    public @NotNull TestDataContainer setFuseExpectedLeft(@Nullable ItemStack expected) {
        AnvilFuseTestData data = new AnvilFuseTestData(
                fuseData.leftItem(), fuseData.rightItem(), fuseData.expectedResult(),
                expected,
                fuseData.expectedAfterRightPlaced(),
                fuseData.expectedPriceAfterLeftPlaced(),
                fuseData.expectedPriceAfterRightPlaced(),
                fuseData.expectedPriceAfterBothPlaced()
        );
        return new TestDataContainer(data, clickData);
    }

    public @NotNull TestDataContainer setFuseExpectedRight(@Nullable ItemStack expected) {
        AnvilFuseTestData data = new AnvilFuseTestData(
                fuseData.leftItem(), fuseData.rightItem(), fuseData.expectedResult(),
                fuseData.expectedAfterLeftPlaced(),
                expected,
                fuseData.expectedPriceAfterLeftPlaced(),
                fuseData.expectedPriceAfterRightPlaced(),
                fuseData.expectedPriceAfterBothPlaced()
        );
        return new TestDataContainer(data, clickData);
    }

    // Set click items
    public @NotNull TestDataContainer setClickLeft(@Nullable ItemStack left) {
        if (clickData == null) return this;
        AnvilClickTestData data = new AnvilClickTestData(
                left, clickData.rightItem(), clickData.resultSlotItem(), clickData.expectedCursor(),
                clickData.levelCost(), clickData.expectedResult(),
                clickData.testNoLevelNoChange(), clickData.npChangeResult()
        );

        return new TestDataContainer(fuseData, data);
    }

    public @NotNull TestDataContainer setClickRight(@Nullable ItemStack right) {
        if (clickData == null) return this;
        AnvilClickTestData data = new AnvilClickTestData(
                clickData.leftItem(), right, clickData.resultSlotItem(), clickData.expectedCursor(),
                clickData.levelCost(), clickData.expectedResult(),
                clickData.testNoLevelNoChange(), clickData.npChangeResult()
        );

        return new TestDataContainer(fuseData, data);
    }

    public @NotNull TestDataContainer setClickOutput(@Nullable ItemStack output) {
        if (clickData == null) return this;
        AnvilClickTestData data = new AnvilClickTestData(
                clickData.leftItem(), clickData.rightItem(), output, clickData.expectedCursor(),
                clickData.levelCost(), clickData.expectedResult(),
                clickData.testNoLevelNoChange(), clickData.npChangeResult()
        );

        return new TestDataContainer(fuseData, data);
    }

    public @NotNull TestDataContainer setClickCursor(@Nullable ItemStack cursor) {
        if (clickData == null) return this;
        AnvilClickTestData data = new AnvilClickTestData(
                clickData.leftItem(), clickData.rightItem(), clickData.resultSlotItem(), cursor,
                clickData.levelCost(), clickData.expectedResult(),
                clickData.testNoLevelNoChange(), clickData.npChangeResult()
        );

        return new TestDataContainer(fuseData, data);
    }

    // Both item
    public @NotNull TestDataContainer setExpectedResult(@Nullable ItemStack result) {
        return setFuseExpected(result).setClickCursor(result);
    }

    // Get fuse item
    public @Nullable ItemStack getLeftFuse() {
        return fuseData.leftItem();
    }

    public @Nullable ItemStack getRightFuse() {
        return fuseData.rightItem();
    }

    public @Nullable ItemStack getExpectedFuse() {
        return fuseData.expectedResult();
    }

    public @Nullable ItemStack getLeftExpectedFuse() {
        return fuseData.expectedAfterLeftPlaced();
    }

    public @Nullable ItemStack getRightExpectedFuse() {
        return fuseData.expectedAfterRightPlaced();
    }

    // Get click item
    public @Nullable ItemStack getLeftClick() {
        return clickData == null ? null : clickData.leftItem();
    }

    public @Nullable ItemStack getRightClick() {
        return clickData == null ? null : clickData.rightItem();
    }

    public @Nullable ItemStack getOutputClick() {
        return clickData == null ? null : clickData.resultSlotItem();
    }

    public @Nullable ItemStack getCursorClick() {
        return clickData == null ? null : clickData.expectedCursor();
    }


}
