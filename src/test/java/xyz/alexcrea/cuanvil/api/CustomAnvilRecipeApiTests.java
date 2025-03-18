package xyz.alexcrea.cuanvil.api;

import org.bukkit.Material;
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
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;
import xyz.alexcrea.cuanvil.tests.ConfigResetCustomAnvilTest;
import xyz.alexcrea.cuanvil.data.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestUtil;

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
        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, nullResultData);

        // Add and test recipe
        AnvilRecipeBuilder builder = new AnvilRecipeBuilder(recipeName);
        builder.setExactCount(true).setLeftItem(stick).setResultItem(stick).setXpCostPerCraft(2);

        assertTrue(builder.registerIfAbsent());
        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, legalResultData);

        AnvilCustomRecipe recipe = getByName(recipeName);
        assertNotNull(recipe);

        // Remove recipe
        assertTrue(CustomAnvilRecipeApi.removeRecipe(recipe));
        assertFalse(CustomAnvilRecipeApi.removeRecipe(recipe));
        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, nullResultData);

        recipe = getByName(recipeName);
        assertNull(recipe);

        // Try to add deleted recipe with no override (should not add)
        assertFalse(CustomAnvilRecipeApi.addRecipe(builder, false));
        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, nullResultData);

        recipe = getByName(recipeName);
        assertNull(recipe);

        // Try to add deleted recipe with override (should add)
        assertTrue(CustomAnvilRecipeApi.addRecipe(builder, true));
        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, legalResultData);

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

        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, nullResultData);

        AnvilRecipeBuilder builder = new AnvilRecipeBuilder(recipeName);
        builder.setExactCount(false)
                .setLeftItem(stick)
                .setResultItem(stick2)
                .setXpCostPerCraft(2);

        assertTrue(builder.registerIfAbsent());

        // Now working test
        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, legalResultData1);
        AnvilFuseTestUtil.executeAnvilFuseTest(anvil, player, legalResultData2);
    }

    @Nullable
    public static AnvilCustomRecipe getByName(String name){
        for (AnvilCustomRecipe registeredRecipe : CustomAnvilRecipeApi.getRegisteredRecipes()) {
            if(registeredRecipe.getName().contentEquals(name)){
                return registeredRecipe;
            }
        }

        return null;
    }

}
