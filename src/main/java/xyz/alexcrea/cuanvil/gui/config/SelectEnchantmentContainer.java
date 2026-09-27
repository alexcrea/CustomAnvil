package xyz.alexcrea.cuanvil.gui.config;

import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;

import java.util.Set;

@NotNullByDefault
public interface SelectEnchantmentContainer {

    Set<CAEnchantment> getSelectedEnchantments();

    boolean setSelectedEnchantments(Set<CAEnchantment> enchantments);

    Set<CAEnchantment> illegalEnchantments();

}
