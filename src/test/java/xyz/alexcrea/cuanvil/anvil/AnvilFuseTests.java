package xyz.alexcrea.cuanvil.anvil;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.alexcrea.cuanvil.DefaultCustomAnvilTest;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.util.AnvilFuseTestUtil;
import xyz.alexcrea.cuanvil.util.CommonItemUtil;

public class AnvilFuseTests extends DefaultCustomAnvilTest {

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
    public void mergeFuseTest(){
        // Literally just test a sharpness 4 + sharpness 4
        ItemStack sharpness4 = CommonItemUtil.sharpness(4);
        ItemStack sharpness5 = CommonItemUtil.sharpness(5);

        AnvilFuseTestData data = new AnvilFuseTestData(
                sharpness4, sharpness4,
                sharpness5
                // TODO add expected price
        );

        AnvilFuseTestUtil.executeAnvilTest(anvil, player, data);
    }

    @Test
    public void overFuseTest(){
        // Test sharpness 4 + sharpness 5
        ItemStack sharpness4 = CommonItemUtil.sharpness(4);
        ItemStack sharpness5 = CommonItemUtil.sharpness(5);

        AnvilFuseTestData data = new AnvilFuseTestData(
                sharpness4, sharpness5,
                sharpness5
                // TODO add expected price
        );

        AnvilFuseTestUtil.executeAnvilTest(anvil, player, data);
    }

    @Test
    public void underFuseTest(){
        // test sharpness 5 + 4. Custom Anvil should not allow it to be as it result as the same item as left item
        ItemStack sharpness4 = CommonItemUtil.sharpness(4);
        ItemStack sharpness5 = CommonItemUtil.sharpness(5);

        AnvilFuseTestData data = new AnvilFuseTestData(
                sharpness5, sharpness4,
                null
        );

        AnvilFuseTestUtil.executeAnvilTest(anvil, player, data);
    }

}
