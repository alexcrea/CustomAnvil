package xyz.alexcrea.cuanvil.enchant;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.group.ConflictType;
import xyz.alexcrea.cuanvil.group.EnchantConflictGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class CAEnchantmentBase implements CAEnchantment {

    @NotNull
    private final NamespacedKey key;
    @NotNull
    private final String name;
    @NotNull
    private final EnchantmentRarity defaultRarity;
    private final int defaultMaxLevel;

    private final List<EnchantConflictGroup> conflicts;

    /**
     * Constructor of Wrapped Enchantment.
     * @param key The enchantment's key.
     * @param defaultRarity Default rarity the enchantment should be.
     * @param defaultMaxLevel Default max level the enchantment can be applied with.
     */
    protected CAEnchantmentBase(
            @NotNull NamespacedKey key,
            @Nullable EnchantmentRarity defaultRarity,
            int defaultMaxLevel){
        this.key = key;
        this.name = key.getKey();
        this.defaultMaxLevel = defaultMaxLevel;

        this.defaultRarity = Objects.requireNonNullElse(defaultRarity, EnchantmentRarity.COMMON);

        this.conflicts = new ArrayList<>();
    }

    @NotNull
    @Override
    public final EnchantmentRarity defaultRarity(){
        return defaultRarity;
    }

    @NotNull
    @Override
    public final NamespacedKey getKey(){
        return key;
    }

    @NotNull
    @Override
    public final String getName(){
        return name;
    }

    @Override
    public final int defaultMaxLevel(){
        return defaultMaxLevel;
    }

    @Override
    public boolean isOptimised(){
        return false;
    }

    @Override
    public boolean isAllowed(@NotNull HumanEntity player){
        return true;
    }

    public int getLevel(@NotNull ItemStack item){
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return 0;
        return getLevel(item, meta);
    }

    public boolean isEnchantmentPresent(@NotNull ItemStack item){
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return false;
        return isEnchantmentPresent(item, meta);
    }

    @Override
    public void addConflict(@NotNull EnchantConflictGroup conflict){
        this.conflicts.add(conflict);
    }

    @Override
    public void removeConflict(@NotNull EnchantConflictGroup conflict){
        this.conflicts.remove(conflict);
    }

    @Override
    public void clearConflict(){
        this.conflicts.clear();
    }

    @Override
    public @NotNull List<EnchantConflictGroup> getConflicts() {
        return conflicts;
    }

}
