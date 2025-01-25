package xyz.alexcrea.cuanvil.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AnvilFuseTestData(
        @Nullable ItemStack leftItem,
        @Nullable ItemStack rightItem,
        @Nullable ItemStack expectedResult,

        @Nullable ItemStack expectedAfterLeftPlaced,
        @Nullable ItemStack expectedAfterRightPlaced,

        @Nullable Integer expectedPriceAfterLeftPlaced,
        @Nullable Integer expectedPriceAfterRightPlaced,
        @Nullable Integer expectedPriceAfterBothPlaced
        ){

    public AnvilFuseTestData(
            @Nullable ItemStack leftItem,
            @Nullable ItemStack rightItem,
            @Nullable ItemStack expectedResult,
            @Nullable Integer expectedPriceAfterBothPlaced
    ){
        this(leftItem, rightItem, expectedResult,
                null, null, null, null,
                expectedPriceAfterBothPlaced
        );
    }

    public AnvilFuseTestData(
            @Nullable ItemStack leftItem,
            @Nullable ItemStack rightItem,
            @Nullable ItemStack expectedResult
    ){
        this(leftItem, rightItem, expectedResult, null
        );
    }

    public AnvilFuseTestData(
            @Nullable ItemStack leftItem,
            @Nullable ItemStack rightItem,
            @Nullable ItemStack expectedResult,

            @Nullable ItemStack expectedAfterLeftPlaced,
            @Nullable ItemStack expectedAfterRightPlaced
    ){
        this(leftItem, rightItem,
                expectedResult, expectedAfterLeftPlaced, expectedAfterRightPlaced,
                null, null, null
        );
    }

}
