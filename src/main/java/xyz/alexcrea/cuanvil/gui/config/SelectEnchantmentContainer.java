package xyz.alexcrea.cuanvil.gui.config;

import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Set;

public interface SelectEnchantmentContainer {

    Set<CAEnchantment> getSelectedEnchantments();

    boolean setSelectedEnchantments(Set<CAEnchantment> enchantments);

    Set<CAEnchantment> illegalEnchantments();

}
