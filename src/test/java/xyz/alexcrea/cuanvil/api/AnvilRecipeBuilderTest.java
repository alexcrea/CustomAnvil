package xyz.alexcrea.cuanvil.api;

import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.alexcrea.cuanvil.tests.SharedOnlyMockBukkit;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("UnstableApiUsage")
public class AnvilRecipeBuilderTest extends SharedOnlyMockBukkit {

    private AnvilRecipeBuilder builder;
    private AnvilRecipeBuilder builder2;

    @BeforeEach
    public void setup() {
        builder = new AnvilRecipeBuilder("test");
        builder2 = new AnvilRecipeBuilder("test");

        builder2.setLeftItem(ItemType.STICK.createItemStack());
        builder2.setResultItem(ItemType.STICK.createItemStack());
    }

    @Test
    void createBuilder_NoLeftItem() {
        builder.setResultItem(ItemType.STICK.createItemStack());

        assertNull(builder.build());
    }

    @Test
    void createBuilder_NoResultItem() {
        builder.setLeftItem(ItemType.STICK.createItemStack());

        assertNull(builder.build());
    }

    @Test
    void createBuilder_minimalist() {
        builder.setLeftItem(ItemType.STICK.createItemStack())
                .setResultItem(ItemType.STICK.createItemStack());

        assertNotNull(builder.build());
        assertNotNull(builder2.build());
    }

    @Test
    void setLeftItem() {
        assertNull(builder.getLeftItem());
        builder.setLeftItem(ItemType.STICK.createItemStack());
        assertNotNull(builder.getLeftItem());
    }

    @Test
    void setRightItem() {
        assertNull(builder.getRightItem());
        builder.setRightItem(ItemType.STICK.createItemStack());
        assertNotNull(builder.getRightItem());
    }

    @Test
    void setResultItem() {
        assertNull(builder.getResultItem());
        builder.setResultItem(ItemType.STICK.createItemStack());
        assertNotNull(builder.getResultItem());
    }

    @Test
    void setXpCostPerCraft() {
        assertEquals(0, builder2.getLevelCostPerCraft());
        assertEquals(0, builder2.build().getLevelCostPerCraft());
        builder2.setLevelCostPerCraft(2);
        assertEquals(2, builder2.getLevelCostPerCraft());
        assertEquals(2, builder2.build().getLevelCostPerCraft());
    }

    @Test
    void setLinearXpCostPerCraft() {
        assertEquals(0, builder2.getLinearXpCostPerCraft());
        assertEquals(0, builder2.build().getXpCostPerCraft());
        builder2.setLinearXpCostPerCraft(2);
        assertEquals(2, builder2.getLinearXpCostPerCraft());
        assertEquals(2, builder2.build().getXpCostPerCraft());
    }


    @Test
    void setExactCount() {
        assertTrue(builder2.isExactCount());
        assertTrue(builder2.build().getExactCount());
        builder2.setExactCount(false);
        assertFalse(builder2.isExactCount());
        assertFalse(builder2.build().getExactCount());
    }

    @Test
    void setName() {
        assertEquals("test", builder2.getName());
        assertEquals("test", builder2.build().getName());
        builder2.setName("other");
        assertEquals("other", builder2.getName());
        assertEquals("other", builder2.build().getName());
    }

}
