package xyz.alexcrea.cuanvil.api;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.inventory.ItemStackMock;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.data.AnvilClickTestData;
import xyz.alexcrea.cuanvil.data.TestDataContainer;
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;
import xyz.alexcrea.cuanvil.tests.ConfigResetCustomAnvilTest;
import xyz.alexcrea.cuanvil.data.AnvilFuseTestData;

import static org.junit.jupiter.api.Assertions.*;

public class CustomAnvilRecipeApiTests extends ConfigResetCustomAnvilTest {

    private AnvilInventory anvil;
    private PlayerMock player;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // Mock used player & open anvil
        player = server.addPlayer();

        Inventory anvil = server.createInventory(player, InventoryType.ANVIL);

        this.anvil = (AnvilInventory) anvil;
        player.openInventory(anvil);

        ConfigHolder.DEFAULT_CONFIG.getConfig().set("debug_log", true);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set("debug_log_verbose", true);
    }

    @Test
    public void testBasicRecipe() {
        String recipeName = "stick_recipe";
        ItemStack stick = new ItemStackMock(Material.STICK);

        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                stick, stick,
                null
        );

        AnvilFuseTestData legalResultData = new AnvilFuseTestData(
                stick, stick,
                null, stick, null,
                2,
                null, null
        );

        // Testing default conflict (no recipe exist)
        nullResultData.executeTest(anvil, player);

        // Add and test recipe
        AnvilRecipeBuilder builder = new AnvilRecipeBuilder(recipeName);
        builder.setExactCount(true).setLeftItem(stick).setResultItem(stick).setLevelCostPerCraft(2);

        assertTrue(builder.registerIfAbsent());
        legalResultData.executeTest(anvil, player);

        AnvilCustomRecipe recipe = getByName(recipeName);
        assertNotNull(recipe);

        // Remove recipe
        assertTrue(CustomAnvilRecipeApi.removeRecipe(recipe));
        assertFalse(CustomAnvilRecipeApi.removeRecipe(recipe));
        nullResultData.executeTest(anvil, player);

        recipe = getByName(recipeName);
        assertNull(recipe);

        // Try to add deleted recipe with no override (should not add)
        assertFalse(CustomAnvilRecipeApi.addRecipe(builder, false));
        nullResultData.executeTest(anvil, player);

        recipe = getByName(recipeName);
        assertNull(recipe);

        // Try to add deleted recipe with override (should add)
        assertTrue(CustomAnvilRecipeApi.addRecipe(builder, true));
        legalResultData.executeTest(anvil, player);

        recipe = getByName(recipeName);
        assertNotNull(recipe);
    }

    @Test
    public void testUnitRecipe() {
        String recipeName = "stick_recipe";
        ItemStack stick = new ItemStackMock(Material.STICK);
        ItemStack stick2 = new ItemStackMock(Material.STICK, 2);
        ItemStack stick5 = new ItemStackMock(Material.STICK, 5);
        ItemStack stick10 = new ItemStackMock(Material.STICK, 10);

        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                stick, stick,
                null
        );

        AnvilFuseTestData legalResultData1 = new AnvilFuseTestData(
                stick, stick,
                null, stick2, null,
                2,
                null, null
        );

        AnvilFuseTestData legalResultData2 = new AnvilFuseTestData(
                stick5, stick,
                null, stick10, null,
                10, // 2 * 5
                null, null
        );

        nullResultData.executeTest(anvil, player);

        AnvilRecipeBuilder builder = new AnvilRecipeBuilder(recipeName);
        builder.setExactCount(false)
                .setLeftItem(stick)
                .setResultItem(stick2)
                .setLevelCostPerCraft(2);

        assertTrue(builder.registerIfAbsent());

        // Now working test
        legalResultData1.executeTest(anvil, player);
        legalResultData2.executeTest(anvil, player);
    }

    @Test
    public void testLinearXpCost() {
        String recipeName = "stick_recipe";
        ItemStack stick = new ItemStackMock(Material.STICK);
        ItemStack stick2 = new ItemStackMock(Material.STICK, 2);
        ItemStack stick5 = new ItemStackMock(Material.STICK, 5);
        ItemStack stick10 = new ItemStackMock(Material.STICK, 10);


        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                stick, stick,
                null
        );

        TestDataContainer legalResultData1 = new TestDataContainer(new AnvilFuseTestData(
                stick, stick,
                null, stick2, null,
                1,
                null, null
        ), new AnvilClickTestData(
                null, null, null, stick2,
                1,
                Event.Result.DENY, true, Event.Result.DENY

        ));

        TestDataContainer legalResultData2 = new TestDataContainer(new AnvilFuseTestData(
                stick5, stick,
                null, stick10, null,
                4,
                null, null
        ), new AnvilClickTestData(
                null, null, null, stick10,
                4,
                Event.Result.DENY, true, Event.Result.DENY

        ));

        nullResultData.executeTest(anvil, player);

        AnvilRecipeBuilder builder = new AnvilRecipeBuilder(recipeName);
        builder.setExactCount(false)
                .setLeftItem(stick)
                .setResultItem(stick2)
                .setLevelCostPerCraft(0)
                .setLinearXpCostPerCraft(10);

        assertTrue(builder.registerIfAbsent());

        // Now working test
        legalResultData1.executeTest(anvil, player);
        legalResultData2.executeTest(anvil, player);
    }

    @Test
    public void testLinearXpCostRemoveExact() {
        String recipeName = "stick_recipe";
        ItemStack stick = new ItemStackMock(Material.STICK);
        ItemStack stick2 = new ItemStackMock(Material.STICK, 2);
        ItemStack stick5 = new ItemStackMock(Material.STICK, 5);
        ItemStack stick10 = new ItemStackMock(Material.STICK, 10);

        AnvilFuseTestData nullResultData = new AnvilFuseTestData(
                stick, stick,
                null
        );

        TestDataContainer legalResultData1 = new TestDataContainer(new AnvilFuseTestData(
                stick, stick,
                null, stick2, null,
                2,
                null, null
        ), new AnvilClickTestData(
                null, null, null, stick2,
                2,
                Event.Result.DENY, true, Event.Result.DENY

        ));

        TestDataContainer legalResultData2 = new TestDataContainer(new AnvilFuseTestData(
                stick5, stick,
                null, stick10, null,
                5,
                null, null
        ), new AnvilClickTestData(
                null, null, null, stick10,
                5,
                Event.Result.DENY, true, Event.Result.DENY
        ));

        nullResultData.executeTest(anvil, player);

        AnvilRecipeBuilder builder = new AnvilRecipeBuilder(recipeName);
        builder.setExactCount(false)
                .setLeftItem(stick)
                .setResultItem(stick2)
                .setLinearXpCostPerCraft(10)
                .setRemoveExactLinearXp(true);

        assertTrue(builder.registerIfAbsent());

        // Now working test
        legalResultData1.executeTest(anvil, player);
        //TODO check exp ?
        System.out.printf(String.valueOf(player.getExp()));
        legalResultData2.executeTest(anvil, player);
        //TODO check exp ?
    }

    @Nullable
    public static AnvilCustomRecipe getByName(String name) {
        for (AnvilCustomRecipe registeredRecipe : CustomAnvilRecipeApi.getRegisteredRecipes()) {
            if (registeredRecipe.getName().contentEquals(name)) {
                return registeredRecipe;
            }
        }

        return null;
    }

}
