package xyz.alexcrea.cuanvil.gui.config;

import xyz.alexcrea.cuanvil.group.AbstractItemTypeGroup;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface SelectGroupContainer {

    Set<AbstractItemTypeGroup> getSelectedGroups();

    boolean setSelectedGroups(Set<AbstractItemTypeGroup> groups);

    Set<AbstractItemTypeGroup> illegalGroups();

    static List<String> getGroupLore(SelectGroupContainer container, String containerType, String groupAction){
        // Prepare group lore
        ArrayList<String> groupLore = new ArrayList<>();
        groupLore.add("§7Allow you to select a list of §3Groups §7that this " + containerType + " should " + groupAction);
        Set<AbstractItemTypeGroup> grouos = container.getSelectedGroups();
        if (grouos.isEmpty()) {
            groupLore.add("§7There is no "+groupAction+"d group for this "+containerType+".");
        } else {
            groupLore.add("§7List of "+groupAction+"d groups for this "+containerType+":");
            Iterator<AbstractItemTypeGroup> groupIterator = grouos.iterator();

            boolean greaterThanMax = grouos.size() > 5;
            int maxindex = (greaterThanMax ? 4 : grouos.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Melee Weapons"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(groupIterator.next().getName());
                groupLore.add("§7- §3" + formattedName);

            }
            if (greaterThanMax) {
                groupLore.add("§7And " + (grouos.size() - 4) + " more...");
            }
        }
        return groupLore;
    }

}
