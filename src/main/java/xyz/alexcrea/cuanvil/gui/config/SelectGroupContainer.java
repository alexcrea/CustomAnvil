package xyz.alexcrea.cuanvil.gui.config;

import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@NotNullByDefault
public interface SelectGroupContainer {

    Set<AbstractMaterialGroup> getSelectedGroups();

    boolean setSelectedGroups(Set<AbstractMaterialGroup> groups);

    Set<AbstractMaterialGroup> illegalGroups();

    static List<String> getGroupLore(SelectGroupContainer container, String containerType, String groupAction){
        // Prepare group lore
        ArrayList<String> groupLore = new ArrayList<>();
        groupLore.add("§7Allow you to select a list of §3Groups §7that this " + containerType + " should " + groupAction);
        Set<AbstractMaterialGroup> groups = container.getSelectedGroups();
        if (groups.isEmpty()) {
            groupLore.add("§7There is no "+groupAction+"d group for this "+containerType+".");
        } else {
            groupLore.add("§7List of "+groupAction+"d groups for this "+containerType+":");
            Iterator<AbstractMaterialGroup> groupIterator = groups.iterator();

            boolean greaterThanMax = groups.size() > 5;
            int maxIndex = (greaterThanMax ? 4 : groups.size());
            for (int i = 0; i < maxIndex; i++) {
                // format string like "- Melee Weapons"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(groupIterator.next().getName());
                groupLore.add("§7- §3" + formattedName);

            }
            if (greaterThanMax) {
                groupLore.add("§7And " + (groups.size() - 4) + " more...");
            }
        }
        return groupLore;
    }

}
