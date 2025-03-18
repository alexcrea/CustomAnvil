package xyz.alexcrea.cuanvil.anvil;

import io.delilaheve.util.ConfigOptions;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.data.AnvilClickTestData;
import xyz.alexcrea.cuanvil.data.AnvilFuseTestData;
import xyz.alexcrea.cuanvil.data.TestDataContainer;
import xyz.alexcrea.cuanvil.tests.SharedCustomAnvilTest;
import xyz.alexcrea.cuanvil.util.config.LoreEditConfigUtil;
import xyz.alexcrea.cuanvil.util.config.LoreEditType;

import java.util.ArrayList;
import java.util.Map;

public class LoreEditTests extends SharedCustomAnvilTest {

    private static AnvilInventory anvil;
    private static PlayerMock player;

    private static final String COLORED_LORE_LINE = "§x§1§2§3§4§5§6TEST §atest";
    private static final String UNCOLORED_LORE_LINE = "#123456TEST &atest";

    private static final int COLOR_USE_COST = 1;
    private static final int COLOR_REMOVE_COST = 2;
    private static final int NON_ONE_TEST_FIXED_COST = 10;
    private static final int NON_ZERO_TEST_LINE_COST = 2;

    private static ItemStack emptyItem;
    private static ItemStack oneColoredLoreItem;
    private static ItemStack twoColoredLoreItem;
    private static ItemStack oneUncoloredLoreItem;
    private static ItemStack twoUncoloredLoreItem;

    private static ItemStack emptyPaperStack;
    private static ItemStack emptyPaper63Item;
    private static ItemStack emptyPaperOne;
    private static ItemStack coloredPaperStack;
    private static ItemStack coloredPaper63Item;
    private static ItemStack coloredPaperOne;
    private static ItemStack uncoloredPaperStack;
    private static ItemStack uncoloredPaper63Item;
    private static ItemStack uncoloredPaperOne;

    private static ItemStack emptyBook;
    private static ItemStack coloredBook1Line;
    private static ItemStack coloredBook2Line;
    private static ItemStack uncoloredBook1Line;
    private static ItemStack uncoloredBook2Line;

    private static TestDataContainer defBookAppend;
    private static TestDataContainer defBookRemove;

    private static TestDataContainer defPaperAppend;
    private static TestDataContainer defPaperRemove;

    private static TestDataContainer defMultilineBookAppend;
    private static TestDataContainer defMultilineBookRemove;

    private static TestDataContainer defMultilinePaperAppend;
    private static TestDataContainer defMultilinePaperRemove;

    private static Map<LoreEditType, TestDataContainer> singleLineTypeToTest;
    private static Map<LoreEditType, TestDataContainer> multiLineTypeToTest;

    @BeforeAll
    public static void setUp() {
        // Mock used player & open anvil
        player = server.addPlayer();

        Inventory anvil = server.createInventory(player, InventoryType.ANVIL);

        LoreEditTests.anvil = (AnvilInventory) anvil;
        player.openInventory(anvil);

        ConfigHolder.DEFAULT_CONFIG.getConfig().set(ConfigOptions.DEBUG_LOGGING, true);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(ConfigOptions.VERBOSE_DEBUG_LOGGING, true);

        // Applied item
        ItemStack item = new ItemStack(Material.STICK, 33);
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();

        emptyItem = item.clone();

        lore.add(COLORED_LORE_LINE);
        meta.setLore(lore);
        item.setItemMeta(meta);
        oneColoredLoreItem = item.clone();

        lore.add(COLORED_LORE_LINE);
        meta.setLore(lore);
        item.setItemMeta(meta);
        twoColoredLoreItem = item.clone();

        lore.clear();
        lore.add(UNCOLORED_LORE_LINE);
        meta.setLore(lore);
        item.setItemMeta(meta);
        oneUncoloredLoreItem = item.clone();

        lore.add(UNCOLORED_LORE_LINE);
        meta.setLore(lore);
        item.setItemMeta(meta);
        twoUncoloredLoreItem = item.clone();
        lore.clear();

        // Paper items
        item = new ItemStack(Material.PAPER, 64);
        meta = item.getItemMeta();
        emptyPaperStack = item.clone();
        item.setAmount(63);
        emptyPaper63Item = item.clone();
        item.setAmount(1);
        emptyPaperOne = item.clone();

        item.setAmount(64);
        meta.setDisplayName(COLORED_LORE_LINE);
        item.setItemMeta(meta);
        coloredPaperStack = item.clone();
        item.setAmount(63);
        coloredPaper63Item = item.clone();
        item.setAmount(1);
        coloredPaperOne = item.clone();

        item.setAmount(64);
        meta.setDisplayName(UNCOLORED_LORE_LINE);
        item.setItemMeta(meta);
        uncoloredPaperStack = item.clone();
        item.setAmount(63);
        uncoloredPaper63Item = item.clone();
        item.setAmount(1);
        uncoloredPaperOne = item.clone();

        // Book items
        item = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta bookmeta = (BookMeta) item.getItemMeta();
        emptyBook = item.clone();

        bookmeta.setPages(COLORED_LORE_LINE);
        item.setItemMeta(bookmeta);
        coloredBook1Line = item.clone();

        bookmeta.setPages(COLORED_LORE_LINE + "\n" + COLORED_LORE_LINE);
        item.setItemMeta(bookmeta);
        coloredBook2Line = item.clone();

        bookmeta.setPages(UNCOLORED_LORE_LINE);
        item.setItemMeta(bookmeta);
        uncoloredBook1Line = item.clone();

        bookmeta.setPages(UNCOLORED_LORE_LINE + "\n" + UNCOLORED_LORE_LINE);
        item.setItemMeta(bookmeta);
        uncoloredBook2Line = item.clone();

        // Default working test data
        defBookAppend = new TestDataContainer(
                new AnvilFuseTestData(
                        emptyItem, uncoloredBook1Line,
                        oneColoredLoreItem,
                        1
                ), new AnvilClickTestData(
                null, emptyBook, null, oneColoredLoreItem,
                1, Event.Result.DENY,
                true, Event.Result.DENY
        ));

        defBookRemove = new TestDataContainer(
                new AnvilFuseTestData(
                        oneColoredLoreItem, emptyBook,
                        emptyItem,
                        1
                ), new AnvilClickTestData(
                null, coloredBook1Line, null, emptyItem,
                1, Event.Result.DENY,
                true, Event.Result.DENY
        ));

        defPaperAppend = new TestDataContainer(
                new AnvilFuseTestData(
                        emptyItem, uncoloredPaperStack,
                        oneColoredLoreItem,
                        1
                ), new AnvilClickTestData(
                emptyPaperOne, uncoloredPaper63Item, null, oneColoredLoreItem,
                1, Event.Result.DENY,
                true, Event.Result.DENY
        ));

        defPaperRemove = new TestDataContainer(
                new AnvilFuseTestData(
                        oneColoredLoreItem, emptyPaperStack,
                        emptyItem,
                        1
                ), new AnvilClickTestData(
                coloredPaperOne, emptyPaper63Item, null, emptyItem,
                1, Event.Result.DENY,
                true, Event.Result.DENY
        ));
        defMultilineBookAppend = new TestDataContainer(
                new AnvilFuseTestData(
                        emptyItem, uncoloredBook2Line,
                        twoColoredLoreItem,
                        1
                ), new AnvilClickTestData(
                null, emptyBook, null, twoColoredLoreItem,
                1, Event.Result.DENY,
                true, Event.Result.DENY
        ));

        defMultilineBookRemove = new TestDataContainer(
                new AnvilFuseTestData(
                        twoColoredLoreItem, emptyBook,
                        emptyItem,
                        1
                ), new AnvilClickTestData(
                null, coloredBook2Line, null, emptyItem,
                1, Event.Result.DENY,
                true, Event.Result.DENY
        ));

        defMultilinePaperAppend = new TestDataContainer(
                new AnvilFuseTestData(
                        oneColoredLoreItem, uncoloredPaperStack,
                        twoColoredLoreItem,
                        1
                ), new AnvilClickTestData(
                emptyPaperOne, uncoloredPaper63Item, null, twoColoredLoreItem,
                1, Event.Result.DENY,
                true, Event.Result.DENY
        ));

        defMultilinePaperRemove = new TestDataContainer(
                new AnvilFuseTestData(
                        twoColoredLoreItem, emptyPaperStack,
                        oneColoredLoreItem,
                        1
                ), new AnvilClickTestData(
                coloredPaperOne, emptyPaper63Item, null, oneColoredLoreItem,
                1, Event.Result.DENY,
                true, Event.Result.DENY
        ));

        singleLineTypeToTest = Map.of(
                LoreEditType.APPEND_BOOK, defBookAppend,
                LoreEditType.REMOVE_BOOK, defBookRemove,
                LoreEditType.APPEND_PAPER, defPaperAppend,
                LoreEditType.REMOVE_PAPER, defPaperRemove
        );

        multiLineTypeToTest = Map.of(
                LoreEditType.APPEND_BOOK, defMultilineBookAppend,
                LoreEditType.REMOVE_BOOK, defMultilineBookRemove,
                LoreEditType.APPEND_PAPER, defMultilinePaperAppend,
                LoreEditType.REMOVE_PAPER, defMultilinePaperRemove
        );
    }

    public @Nullable ItemStack uncoloredEquivalent(@Nullable ItemStack colored){
        // null check
        if(null == colored) return null;

        if(oneColoredLoreItem == colored) return oneUncoloredLoreItem;
        if(twoColoredLoreItem == colored) return twoUncoloredLoreItem;

        if(coloredPaperStack == colored) return uncoloredPaperStack;
        if(coloredPaper63Item == colored) return uncoloredPaper63Item;
        if(coloredPaperOne == colored) return uncoloredPaperOne;

        if(coloredBook1Line == colored) return uncoloredBook1Line;
        if(coloredBook2Line == colored) return uncoloredBook2Line;

        // They already are uncolored
        if(oneUncoloredLoreItem == colored) return oneUncoloredLoreItem;
        if(twoUncoloredLoreItem == colored) return twoUncoloredLoreItem;

        if(uncoloredPaperStack == colored) return uncoloredPaperStack;
        if(uncoloredPaper63Item == colored) return uncoloredPaper63Item;
        if(uncoloredPaperOne == colored) return uncoloredPaperOne;

        if(uncoloredBook1Line == colored) return uncoloredBook1Line;
        if(uncoloredBook2Line == colored) return uncoloredBook2Line;

        // No lore items return themself
        if(emptyItem == colored) return emptyItem;
        if(emptyBook == colored) return emptyBook;
        if(emptyPaperStack == colored) return emptyPaperStack;
        if(emptyPaper63Item == colored) return emptyPaper63Item;
        if(emptyPaperOne == colored) return emptyPaperOne;

        Assertions.fail("Could not find uncolored version of " + colored);
        return null;
    }

    @BeforeEach
    public void prepareAnvil() {
        anvil.clear();

        // Make sure we reset value in case it got modified
        for (@NotNull LoreEditType type : LoreEditType.values()) {
            // Make sure it is enabled for the tests (unless its is enabled test)
            ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.IS_ENABLED, true);

            ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.DO_CONSUME, false);
            ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.FIXED_COST, 1);
            ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.PER_LINE_COST, 0);


            // Make sur color is enabled by default
            if (type.isAppend()) {
                ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.ALLOW_HEX_COLOR, true);
                ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.ALLOW_COLOR_CODE, true);
                ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.USE_COLOR_COST, 0);
            } else {
                ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.REMOVE_COLOR_ON_LORE_REMOVE, false);
                ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.REMOVE_COLOR_COST, 0);
            }

        }

        // Disable them by default and test them on specific tests
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(LoreEditConfigUtil.BOOK_PERMISSION_NEEDED, false);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(LoreEditConfigUtil.PAPER_PERMISSION_NEEDED, false);
    }

    @AfterAll
    public static void tearDown() {
        player = null;
        anvil = null;
    }

    @Test
    public void simpleTest() {
        // Test all defaults to make sure they works
        for (LoreEditType type : LoreEditType.values()) {
            singleLineTypeToTest.get(type).executeTest(anvil, player);
            multiLineTypeToTest.get(type).executeTest(anvil, player);
        }
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testPermissionNeeded_DEOP(LoreEditType type) {
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(LoreEditConfigUtil.BOOK_PERMISSION_NEEDED, true);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(LoreEditConfigUtil.PAPER_PERMISSION_NEEDED, true);
        player.setOp(false);

        singleLineTypeToTest.get(type).nullifyResult().executeTest(anvil, player);
        multiLineTypeToTest.get(type).nullifyResult().executeTest(anvil, player);
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testPermissionNeeded_OP(LoreEditType type) {
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(LoreEditConfigUtil.BOOK_PERMISSION_NEEDED, true);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(LoreEditConfigUtil.PAPER_PERMISSION_NEEDED, true);
        player.setOp(true);

        singleLineTypeToTest.get(type).executeTest(anvil, player);
        multiLineTypeToTest.get(type).executeTest(anvil, player);
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testLoreTypeDisabled(LoreEditType type) {
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.IS_ENABLED, false);

        singleLineTypeToTest.get(type).nullifyResult().executeTest(anvil, player);
        multiLineTypeToTest.get(type).nullifyResult().executeTest(anvil, player);
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testFixedCost_Default(LoreEditType type) {
        singleLineTypeToTest.get(type).setCost(1).executeTest(anvil, player);
        multiLineTypeToTest.get(type).setCost(1).executeTest(anvil, player);
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testFixedCost_Modified(LoreEditType type) {
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.FIXED_COST, NON_ONE_TEST_FIXED_COST);

        singleLineTypeToTest.get(type).setCost(NON_ONE_TEST_FIXED_COST).executeTest(anvil, player);
        multiLineTypeToTest.get(type).setCost(NON_ONE_TEST_FIXED_COST).executeTest(anvil, player);
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testLineCost_Modified(LoreEditType type) {
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.PER_LINE_COST, NON_ZERO_TEST_LINE_COST);

        if (type.isMultiLine()) {
            singleLineTypeToTest.get(type).setCost(NON_ZERO_TEST_LINE_COST + LoreEditConfigUtil.DEFAULT_FIXED_COST).executeTest(anvil, player);
            multiLineTypeToTest.get(type).setCost(2 * NON_ZERO_TEST_LINE_COST + LoreEditConfigUtil.DEFAULT_FIXED_COST).executeTest(anvil, player);
        } else {
            singleLineTypeToTest.get(type).executeTest(anvil, player);
            multiLineTypeToTest.get(type).executeTest(anvil, player);
        }
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testColorCost(LoreEditType type) {
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.USE_COLOR_COST, COLOR_USE_COST);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.REMOVE_COLOR_COST, COLOR_REMOVE_COST);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.REMOVE_COLOR_ON_LORE_REMOVE, true);

        TestDataContainer singleLData = singleLineTypeToTest.get(type);
        TestDataContainer multiLData = multiLineTypeToTest.get(type);

        if (type.isAppend()) {
            singleLData.setCost(COLOR_USE_COST + LoreEditConfigUtil.DEFAULT_FIXED_COST).executeTest(anvil, player);
            multiLData.setCost(COLOR_USE_COST + LoreEditConfigUtil.DEFAULT_FIXED_COST).executeTest(anvil, player);
        } else {
            singleLData
                    .setCost(COLOR_REMOVE_COST + LoreEditConfigUtil.DEFAULT_FIXED_COST)
                    .setClickRight(uncoloredEquivalent(singleLData.getRightClick()))
                    .setClickLeft(uncoloredEquivalent(singleLData.getLeftClick()))
                    .executeTest(anvil, player);

            multiLData.setCost(COLOR_REMOVE_COST + LoreEditConfigUtil.DEFAULT_FIXED_COST)
                    .setClickRight(uncoloredEquivalent(multiLData.getRightClick()))
                    .setClickLeft(uncoloredEquivalent(multiLData.getLeftClick()))
                    .executeTest(anvil, player);
        }
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testColorDisabled(LoreEditType type) {
        if(!type.isAppend()) return;
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.USE_COLOR_COST, COLOR_USE_COST);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.ALLOW_HEX_COLOR, false);
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.ALLOW_COLOR_CODE, false);

        TestDataContainer singleLData = singleLineTypeToTest.get(type);
        TestDataContainer multiLData = multiLineTypeToTest.get(type);

        singleLData
                .setExpectedResult(uncoloredEquivalent(singleLData.getExpectedFuse()))
                .executeTest(anvil, player);
        multiLData
                .setFuseLeft(uncoloredEquivalent(multiLData.getLeftFuse()))
                .setExpectedResult(uncoloredEquivalent(multiLData.getExpectedFuse()))
                .executeTest(anvil, player);
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testColorRemoveEnabled(LoreEditType type) {
        if(type.isAppend()) return;
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.REMOVE_COLOR_ON_LORE_REMOVE, true);

        TestDataContainer singleLData = singleLineTypeToTest.get(type);
        TestDataContainer multiLData = multiLineTypeToTest.get(type);

        singleLData
                .setClickRight(uncoloredEquivalent(singleLData.getRightClick()))
                .setClickLeft(uncoloredEquivalent(singleLData.getLeftClick()))
                .executeTest(anvil, player);
        multiLData
                .setClickRight(uncoloredEquivalent(multiLData.getRightClick()))
                .setClickLeft(uncoloredEquivalent(multiLData.getLeftClick()))
                .executeTest(anvil, player);
    }

    @ParameterizedTest
    @EnumSource(LoreEditType.class)
    public void testDoConsume(LoreEditType type) {
        if(type.isAppend()) return;
        ConfigHolder.DEFAULT_CONFIG.getConfig().set(type.getRootPath() + "." + LoreEditConfigUtil.DO_CONSUME, true);

        TestDataContainer singleLData = singleLineTypeToTest.get(type);
        TestDataContainer multiLData = multiLineTypeToTest.get(type);

        // NOTE: we set to null right item only on multi line bc the default data has more than one paper and would go to the left instead
        singleLData = singleLData
                .setClickRight(type.isMultiLine() ? null : singleLData.getRightClick())
                .setClickLeft(null);
        singleLData.executeTest(anvil, player);
        multiLData = multiLData
                .setClickRight(type.isMultiLine() ? null : singleLData.getRightClick())
                .setClickLeft(null);
        multiLData.executeTest(anvil, player);

        if(!type.isMultiLine()){
            singleLData.setFuseRight(emptyPaperOne).setClickRight(null).executeTest(anvil, player);
            multiLData.setFuseRight(emptyPaperOne).setClickRight(null).executeTest(anvil, player);
        }
    }

    //TODO single paper test

    //TODO remove order test
    //TODO work penalty test

}
