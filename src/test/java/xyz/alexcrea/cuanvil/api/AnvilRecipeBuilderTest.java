package xyz.alexcrea.cuanvil.api;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.alexcrea.cuanvil.tests.SharedOnlyMockBukkit;

import static org.junit.jupiter.api.Assertions.*;

public class AnvilRecipeBuilderTest extends SharedOnlyMockBukkit {

    private AnvilRecipeBuilder builder;

    @BeforeEach
    public void setup() {
        builder = new AnvilRecipeBuilder("test");
    }

    @Test
    void createBuilder_NoLeftItem(){
        builder.setResultItem(new ItemStack(Material.STICK));

        assertNull(builder.build());
    }

    @Test
    void createBuilder_NoResultItem(){
        builder.setLeftItem(new ItemStack(Material.STICK));

        assertNull(builder.build());
    }

    @Test
    void createBuilder_minimalist(){
        builder.setLeftItem(new ItemStack(Material.STICK))
                .setResultItem(new ItemStack(Material.STICK));

        assertNotNull(builder.build());
    }

    @Test
    void setLeftItem(){
        assertNull(builder.getLeftItem());
        builder.setLeftItem(new ItemStack(Material.STICK));
        assertNotNull(builder.getLeftItem());
    }

    @Test
    void setRightItem(){
        assertNull(builder.getRightItem());
        builder.setRightItem(new ItemStack(Material.STICK));
        assertNotNull(builder.getRightItem());
    }

    @Test
    void setResultItem(){
        assertNull(builder.getResultItem());
        builder.setResultItem(new ItemStack(Material.STICK));
        assertNotNull(builder.getResultItem());
    }

    @Test
    void setXpCostPerCraft(){
        assertEquals(1, builder.getXpCostPerCraft());
        builder.setXpCostPerCraft(2);
        assertEquals(2, builder.getXpCostPerCraft());
    }

    @Test
    void setExactCount(){
        assertTrue(builder.isExactCount());
        builder.setExactCount(false);
        assertFalse(builder.isExactCount());
    }

    @Test
    void setName(){
        assertEquals("test", builder.getName());
        builder.setName("other");
        assertEquals("other", builder.getName());
    }

}
