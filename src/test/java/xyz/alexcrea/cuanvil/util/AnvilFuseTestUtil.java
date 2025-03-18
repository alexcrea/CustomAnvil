package xyz.alexcrea.cuanvil.util;

import io.delilaheve.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.listener.AnvilResultListener;
import xyz.alexcrea.cuanvil.listener.PrepareAnvilListener;
import xyz.alexcrea.cuanvil.mock.AnvilViewMock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnvilFuseTestUtil {

    private static PrepareAnvilListener PREPARE_LISTENER = new PrepareAnvilListener();
    private static AnvilResultListener RESULT_LISTENER = new AnvilResultListener();

    public static ItemStack prepareItem(@NotNull Material material,
                                        @NotNull List<CAEnchantment> enchantments,
                                        @NotNull List<Integer> level){
        return prepareItem(material, 0, enchantments, level);
    }

    public static ItemStack prepareItem(@NotNull Material material,
                                        int repairCost,
                                        @NotNull List<CAEnchantment> enchantments,
                                        @NotNull List<Integer> level){
        Assertions.assertEquals(enchantments.size(), level.size());

        HashMap<CAEnchantment, Integer> enchantmentMap = new HashMap<>();
        for (int i = 0; i < enchantments.size(); i++) {
            enchantmentMap.put(enchantments.get(i), level.get(i));
        }

        ItemStack item = new ItemStack(material);
        ItemUtil.INSTANCE.setEnchantmentsUnsafe(item, enchantmentMap);

        ItemMeta meta = item.getItemMeta();
        ((Repairable) meta).setRepairCost(repairCost);
        item.setItemMeta(meta);

        return item;
    }


    public static ItemStack prepareItem(@NotNull Material material,
                                        @NotNull List<String> enchantmentNames,
                                        Integer... levels){
        return prepareItem(material, 0, enchantmentNames, levels);
    }
    public static ItemStack prepareItem(@NotNull Material material,
                                        int repairCost,
                                        @NotNull List<String> enchantmentNames,
                                        Integer... levels){
        List<CAEnchantment> enchantments = new ArrayList<>();

        for (String enchantmentName : enchantmentNames) {
            List<CAEnchantment> enchantmentList = CAEnchantment.getListByName(enchantmentName);
            Assertions.assertNotEquals(0, enchantmentList.size(),
                    "Could not find enchantment \"" + enchantmentName + "\"");

            enchantments.addAll(enchantmentList);
        }

        return prepareItem(material, repairCost, enchantments, List.of(levels));
    }


    /*
     * Need to use that as it seems setting item in the inventory will not trigger the anvil click even
     *
     * Not the best for non-custom anvil plugins but work in the context of CA
     */
    public static void imitateAnvilUpdate(
            @NotNull HumanEntity player,
            @NotNull AnvilInventory anvil) {

        AnvilViewMock view = new AnvilViewMock(player, anvil);
        try {
            PrepareAnvilEvent event = new PrepareAnvilEvent(view, anvil.getItem(2));

            // Not ideal but possible and the easiest so why not
            PREPARE_LISTENER.anvilCombineCheck(event);
            anvil.setResult(event.getResult());
        } catch (Exception e){
            Assertions.fail(e);
        }
    }

    public static void executeAnvilTest(
            @NotNull AnvilInventory anvil,
            @NotNull HumanEntity player,
            @NotNull AnvilFuseTestData data
            ){
        Assertions.assertEquals(player.getOpenInventory().getTopInventory(), anvil,
                "Openned inventory is not anvil");

        // Test with only the left item
        anvil.setItem(1, null); // We clear the right slot in case something was there
        testPlacingItem(anvil, player,
                0, data.expectedPriceAfterLeftPlaced(),
                data.leftItem(), data.expectedAfterLeftPlaced());

        // Test with only the right item
        anvil.setItem(0, null); // We only want the right item. so we remove the left one
        testPlacingItem(anvil, player,
                1, data.expectedPriceAfterRightPlaced(),
                data.rightItem(), data.expectedAfterRightPlaced());

        // Test with both placed
        testPlacingItem(anvil, player,
                0, data.expectedPriceAfterBothPlaced(),
                data.leftItem(), data.expectedResult());
    }

    public static void executeAnvilClick(
            @NotNull AnvilInventory anvil,
            @NotNull Player player,
            @NotNull AnvilClickTestData data
    ) {
        if(data.testNoLevelNoChange()){
            ItemStack left = anvil.getFirstItem();
            ItemStack right = anvil.getSecondItem();
            ItemStack result = anvil.getResult();

            player.setLevel(0);
            player.setItemOnCursor(null);

            // Do a test with not enough level
            simulateClick(anvil, player, data.npChangeResult());

            // Nothing should have changed
            assertEqual(left, anvil.getFirstItem());
            assertEqual(right, anvil.getSecondItem());
            assertEqual(result, anvil.getResult());
            assertEqual(null, player.getItemOnCursor());
        }
        player.setLevel(data.levelCost());
        player.setItemOnCursor(null);

        simulateClick(anvil, player, data.expectedResult());

        // Nothing should have changed
        assertEqual(data.leftItem(), anvil.getFirstItem());
        assertEqual(data.rightItem(), anvil.getSecondItem());
        assertEqual(data.resultSlotItem(), anvil.getResult());
        assertEqual(data.expectedCursor(), data.expectedCursor());
    }

    private static void simulateClick(
            @NotNull AnvilInventory anvil,
            @NotNull Player player,
            @Nullable Event.Result expectedResult
    ){
        AnvilViewMock view = new AnvilViewMock(player, anvil);
        try {
            InventoryClickEvent event = new InventoryClickEvent(view,
                    InventoryType.SlotType.RESULT,
                    PrepareAnvilListener.ANVIL_OUTPUT_SLOT,
                    ClickType.LEFT,
                    InventoryAction.PICKUP_ALL);

            RESULT_LISTENER.anvilExtractionCheck(event);
            if(expectedResult != null){
                Assertions.assertEquals(expectedResult, event.getResult());
            }
        } catch (Exception e){
            Assertions.fail(e);
        }
    }

    @SuppressWarnings({"removal"})
    private static void testPlacingItem(
            @NotNull AnvilInventory anvil,
            @NotNull HumanEntity player,
            int slot,
            Integer expectedPrice,
            @Nullable ItemStack toPlace,
            @Nullable ItemStack expectedResult){
        anvil.setItem(slot, toPlace);
        anvil.setItem(2, null);
        AnvilFuseTestUtil.imitateAnvilUpdate(player, anvil);

        ItemStack result = anvil.getItem(2);
        assertEqual(expectedResult, result);

        assertPriceEqual(expectedPrice, anvil.getRepairCost());
    }

    public static void assertEqual(@Nullable ItemStack expected, @Nullable ItemStack other) {
        boolean secondIsAir = isAir(other);
        if(isAir(expected)) Assertions.assertTrue(secondIsAir,"Item "+other+" was not AIR but was expected to be air");
        else {
            Assertions.assertFalse(secondIsAir,"Item "+other+" was expected not to be air");

            expected.setDurability(expected.getDurability());
            other.setDurability(other.getDurability());
            Assertions.assertEquals(expected, other);
        }

    }

    public static boolean isAir(@Nullable ItemStack item){
        return item == null || item.isEmpty();
    }

    public static void assertPriceEqual(Integer expectedPrice, int price){
        if(expectedPrice == null) return;
        Assertions.assertEquals(expectedPrice, price);
    }

}
