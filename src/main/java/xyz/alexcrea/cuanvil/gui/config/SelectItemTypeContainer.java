package xyz.alexcrea.cuanvil.gui.config;

import org.bukkit.inventory.ItemType;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public interface SelectItemTypeContainer {

    Set<ItemType> getSelectedItems();

    boolean setSelectedItems(Set<ItemType> types);

    Set<ItemType> illegalItems();

    static List<String> getItemLore(SelectItemTypeContainer container, String containerType, String action) {
        // Prepare item lore
        ArrayList<String> groupLore = new ArrayList<>();
        groupLore.add("§7Allow you to select a list of §eitems §7that this " + containerType + " should " + action);
        Set<ItemType> typeSet = container.getSelectedItems();
        if (typeSet.isEmpty()) {
            groupLore.add("§7There is no " + action + "d item for this " + containerType + ".");
        } else {
            groupLore.add("§7List of " + action + "d items for this " + containerType + ":");
            Iterator<ItemType> typeIterator = typeSet.iterator();

            boolean greaterThanMax = typeSet.size() > 5;
            int maxindex = (greaterThanMax ? 4 : typeSet.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Stone Sword"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(typeIterator.next().key().value().toLowerCase());
                groupLore.add("§7- §e" + formattedName);

            }
            if (greaterThanMax) {
                groupLore.add("§7And " + (typeSet.size() - 4) + " more...");
            }
        }
        return groupLore;
    }

}
