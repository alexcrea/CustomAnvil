package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import xyz.alexcrea.cuanvil.dependency.plugins.ExcellentEnchant5_4EnchantSettings;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Map;

public class CAEEV5_4Enchantment extends CAEEV5Enchantment {

    public CAEEV5_4Enchantment(@NotNull CustomEnchantment enchantment) {
        super(enchantment);
    }

    @Override
    public boolean isEnchantConflict(@NotNull Map<CAEnchantment, Integer> enchantments, @NotNull Material itemMat) {
        if(super.isEnchantConflict(enchantments, itemMat)) return true;

        var limit = ExcellentEnchant5_4EnchantSettings.anvilLimit();
        var count = enchantments.keySet().stream()
                .filter(key -> key instanceof CAEEV5_4Enchantment)
                .count();

        return count > limit;
    }

}
