package xyz.alexcrea.cuanvil.dependency.plugins;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;

import java.util.Set;

public class ExcellentEnchant5_3Registry {

    public static @NotNull Set<CustomEnchantment> getRegistered(){
        return EnchantRegistry.getRegistered();
    }


}
