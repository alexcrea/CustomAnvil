package xyz.alexcrea.cuanvil.gui.config;

import xyz.alexcrea.cuanvil.enchant.WrappedEnchantment;

import java.util.Set;

public interface SelectEnchantmentContainer {

    Set<WrappedEnchantment> getSelectedEnchantments();

    boolean setSelectedEnchantments(Set<WrappedEnchantment> enchantments);

    Set<WrappedEnchantment> illegalEnchantments();

}
