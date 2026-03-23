package xyz.alexcrea.cuanvil.dependency.plugins;

import su.nightexpress.excellentenchants.EnchantsAPI;

public class ExcellentEnchant5_4EnchantSettings {


    public static int anvilLimit() {
        return EnchantsAPI.getEnchantManager().getSettings().getAnvilEnchantsLimit();
    }

}
