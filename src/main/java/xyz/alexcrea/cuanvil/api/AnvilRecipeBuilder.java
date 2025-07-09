package xyz.alexcrea.cuanvil.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.recipe.AnvilCustomRecipe;

/**
 * A Builder for custom craft using anvil.
 * <p>
 * This is part of
 * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
 */
@SuppressWarnings("unused")
public class AnvilRecipeBuilder {

    private @NotNull String name;
    private boolean exactCount;

    private int levelCostPerCraft;
    private int linearXpCostPerCraft;

    private boolean removeExactLinearXp;

    private @Nullable ItemStack leftItem;
    private @Nullable ItemStack rightItem;
    private @Nullable ItemStack resultItem;

    /**
     * Instantiates a new Anvil recipe builder.
     * exact count default to true.
     * xp level and linear cost per craft default to 0.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param name The recipe name
     */
    public AnvilRecipeBuilder(@NotNull String name) {
        this.name = name;

        this.exactCount = true;
        this.levelCostPerCraft = 0;
        this.linearXpCostPerCraft = 0;
        this.removeExactLinearXp = false;

        this.leftItem = null;
        this.rightItem = null;
        this.resultItem = null;
    }

    /**
     * Gets the recipe name.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return This recipe builder instance.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Sets the recipe name.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param name The recipe name
     * @return This recipe builder instance.
     */
    public AnvilRecipeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get if the recipe is exact count. (default 0)
     * <p>
     * Exact count mean the recipe can only be crafted 1 by 1.
     * If set to false, then it will craft as much as possible in 1 go and will keep unused material onto the anvil inventory.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return If the recipe is exact count.
     */
    public boolean isExactCount() {
        return exactCount;
    }

    /**
     * Sets if the recipe is exact count.
     * <p>
     * Exact count mean the recipe can only be crafted 1 by 1.
     * If set to false, then it will craft as much as possible in 1 go and will keep unused material onto the anvil inventory.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param exactCount If the recipe is exact count
     * @return This recipe builder instance.
     */
    public AnvilRecipeBuilder setExactCount(boolean exactCount) {
        this.exactCount = exactCount;
        return this;
    }

    /**
     * Get the xp level cost per craft. (default 0)
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return The xp level cost per craft
     * @deprecated use {@link #getLevelCostPerCraft() getLevelCostPerCraft} instead
     */
    @Deprecated(since = "1.13.0")
    public int getXpCostPerCraft() {
        return getLevelCostPerCraft();
    }

    /**
     * Sets the xp level cost per craft.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param xpCostPerCraft The xp level cost per craft
     * @return This recipe builder instance.
     * @deprecated use {@link #setLevelCostPerCraft(int) setLevelCostPerCraft} instead
     */
    @Deprecated(since = "1.13.0")
    public AnvilRecipeBuilder setXpCostPerCraft(int xpCostPerCraft) {
        return setLevelCostPerCraft(xpCostPerCraft);
    }

    /**
     * Get the xp level cost per craft. (default 0)
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return The xp level cost per craft
     */
    public int getLevelCostPerCraft() {
        return levelCostPerCraft;
    }

    /**
     * Sets the xp level cost per craft.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param levelCostPerCraft The xp level cost per craft
     * @return This recipe builder instance.
     */
    public AnvilRecipeBuilder setLevelCostPerCraft(int levelCostPerCraft) {
        this.levelCostPerCraft = levelCostPerCraft;
        return this;
    }

    /**
     * Get the linear xp cost (not xp level cost) per craft.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return The xp level cost per craft
     */
    public int getLinearXpCostPerCraft() {
        return linearXpCostPerCraft;
    }

    /**
     * Sets the linear xp cost (not xp level cost) per craft.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param linearXpCostPerCraft The linear xp cost per craft
     * @return This recipe builder instance.
     */
    public AnvilRecipeBuilder setLinearXpCostPerCraft(int linearXpCostPerCraft) {
        this.linearXpCostPerCraft = linearXpCostPerCraft;
        return this;
    }

    /**
     * Get if the linear xp should get removed by an exact amount.
     * <p>
     * If false (default) level cost will be the level that would be reached by a player with this amount of xp.
     * If true will require the level that has at least the specified level of xp then on click remove only the necessary xp
     * <p>
     * linear xp cost are applied after level cost
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return if we should remove the exact amount of linear xp
     */
    public boolean isRemoveExactLinearXp() {
        return removeExactLinearXp;
    }

    /**
     * Set if the linear xp should get removed by an exact amount.
     * <p>
     * If false (default) level cost will be the level that would be reached by a player with this amount of xp.
     * If true will require the level that has at least the specified level of xp then on click remove only the necessary xp
     * <p>
     * linear xp cost are applied after level cost
     * @param removeExactLinearXp if we should remove the exact amount of linear xp
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return This recipe builder instance.
     */
    public AnvilRecipeBuilder setRemoveExactLinearXp(boolean removeExactLinearXp) {
        this.removeExactLinearXp = removeExactLinearXp;
        return this;
    }

    /**
     * Get the left item of the recipe.
     * If null (default) then the recipe will not be able to be registered.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return The left item
     */
    @Nullable
    public ItemStack getLeftItem() {
        return leftItem;
    }

    /**
     * Set the left item.
     * If null (default) then the recipe will not be able to be registered.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param leftItem the left item
     * @return This recipe builder instance.
     */
    public AnvilRecipeBuilder setLeftItem(ItemStack leftItem) {
        this.leftItem = leftItem;
        return this;
    }

    /**
     * Get the recipe right item.
     * null on default new instance.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return The right item
     */
    @Nullable
    public ItemStack getRightItem() {
        return rightItem;
    }

    /**
     * Set the recipe right item.
     * null on default new instance.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param rightItem the right item
     * @return This recipe builder instance.
     */
    public AnvilRecipeBuilder setRightItem(ItemStack rightItem) {
        this.rightItem = rightItem;
        return this;
    }

    /**
     * Get the recipe result item.
     * If null (default) then the recipe will not be able to be registered.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return The result item
     */
    @Nullable
    public ItemStack getResultItem() {
        return resultItem;
    }

    /**
     * Set the recipe result item.
     * If null (default) then the recipe will not be able to be registered.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @param resultItem The result item
     * @return This recipe builder instance.
     */
    public AnvilRecipeBuilder setResultItem(ItemStack resultItem) {
        this.resultItem = resultItem;
        return this;
    }

    /**
     * Build the anvil custom recipe.
     * Should probably use {@link #registerIfAbsent() registerIfAbsent} or {@link ConflictAPI#addConflict(ConflictBuilder) addConflict}.
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return A new anvil custom recipe base on this builder.
     */
    @Nullable // null if missing argument
    public AnvilCustomRecipe build() {
        if (leftItem == null || resultItem == null) return null;

        return new AnvilCustomRecipe(
                this.name,
                this.exactCount,
                this.levelCostPerCraft,
                this.linearXpCostPerCraft,
                this.removeExactLinearXp,
                this.leftItem, this.rightItem, this.resultItem
        );
    }

    /**
     * Register this recipe if absent.
     * Equivalent to {@link ConflictAPI#addConflict(ConflictBuilder)}
     * <p>
     * This is part of
     * {@link xyz.alexcrea.cuanvil.api.data.CAApiFlags#CUSTOM_RECIPE_V1 CUSTOM_RECIPE_V1}
     *
     * @return True if successful.
     */
    public boolean registerIfAbsent() {
        return CustomAnvilRecipeApi.addRecipe(this);
    }

}
