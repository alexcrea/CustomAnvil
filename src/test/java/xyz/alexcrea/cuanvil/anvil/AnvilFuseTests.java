package xyz.alexcrea.cuanvil.anvil;

import io.delilaheve.util.ConfigOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.tests.SharedCustomAnvilTest;
import xyz.alexcrea.cuanvil.data.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestUtil;
import xyz.alexcrea.cuanvil.util.CommonItemUtil;

public class AnvilFuseTests extends SharedCustomAnvilTest {

    private static AnvilInventory anvil;
    private static PlayerMock player;

    @BeforeAll
    public static void setUp() {
        // Mock used player & open anvil
        player = server.addPlayer();

        Inventory anvil = server.createInventory(player, InventoryType.ANVIL);

        AnvilFuseTests.anvil = (AnvilInventory) anvil;
        player.openInventory(anvil);

        ConfigHolder.DEFAULT_CONFIG.getConfig().set(ConfigOptions.DEBUG_LOGGING, true);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(ConfigOptions.VERBOSE_DEBUG_LOGGING, true);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(ConfigOptions.ALLOW_COLOR_CODE, true); // For rename test
    }

    @BeforeEach
    public void prepareAnvil(){
        anvil.clear();
    }

    @AfterAll
    public static void tearDown() {
        player = null;
        anvil = null;
    }

    public static AnvilFuseTestData mergeFuseData(Integer levelLeft, Integer levelRight, Integer levelResult) {
        ItemStack result = CommonItemUtil.sharpness(levelResult);
        if(result != null){
            Repairable meta = (Repairable) result.getItemMeta();
            meta.setRepairCost(1);
            result.setItemMeta(meta);
        }

        return new AnvilFuseTestData(
                CommonItemUtil.sharpness(levelLeft),
                CommonItemUtil.sharpness(levelRight),
                result,
                levelResult
        );
    }

    @Test
    public void mergeFuseTest(){
        mergeFuseData(4, 4, 5)
                .executeTest(anvil, player);
    }

    @Test
    public void overFuseTest(){
        mergeFuseData(4, 5, 5)
                .executeTest(anvil, player);
    }

    @Test
    public void underFuseTest(){
        mergeFuseData(5, 4, null)
                .executeTest(anvil, player);
    }

    // Note: currently anvil can only have null name. maybe handle differently later
    @Test
    public void nullNameResetTest(){
        ItemStack base = new ItemStack(Material.NETHERITE_SWORD);
        ItemStack expected = base.clone();

        ItemMeta meta = expected.getItemMeta();
        meta.displayName(Component.text("test"));
        base.setItemMeta(meta);

        AnvilFuseTestData data = new AnvilFuseTestData(
                base, null,
                expected, expected, null,
                1, null, 1
        );

        data.executeTest(anvil, player);
    }

}
