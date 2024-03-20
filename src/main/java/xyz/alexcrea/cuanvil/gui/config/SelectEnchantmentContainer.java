package xyz.alexcrea.cuanvil.gui.config;

import org.bukkit.enchantments.Enchantment;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;

import java.util.List;
import java.util.Set;

public interface SelectEnchantmentContainer {

    List<Enchantment> getSelectedEnchantments();
    void setSelectedEnchantments(List<Enchantment> enchantments);

    Set<Enchantment> illegalEnchantments();

}
