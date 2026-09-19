package xyz.alexcrea.cuanvil.enchant.wrapped;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNullByDefault;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import xyz.alexcrea.cuanvil.dependency.plugins.ExcellentEnchant5_4EnchantSettings;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Map;

@NotNullByDefault
public class CAEEV5_4Enchantment extends CAEEV5Enchantment {

    public CAEEV5_4Enchantment(CustomEnchantment enchantment) {
        super(enchantment);
    }

    @Override
    public boolean isEnchantConflict(Map<CAEnchantment, Integer> enchantments, NamespacedKey itemMat) {
        if(super.isEnchantConflict(enchantments, itemMat)) return true;

        var limit = ExcellentEnchant5_4EnchantSettings.anvilLimit();
        var count = enchantments.keySet().stream()
                .filter(key -> key instanceof CAEEV5_4Enchantment)
                .count();

        return count > limit;
    }

}
