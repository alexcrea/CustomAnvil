package xyz.alexcrea.cuanvil.gui.config;

import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface SelectGroupContainer {

    Set<AbstractMaterialGroup> getSelectedGroups();

    boolean setSelectedGroups(Set<AbstractMaterialGroup> groups);

    Set<AbstractMaterialGroup> illegalGroups();

    static List<String> getGroupLore(SelectGroupContainer container, String containerType, String groupAction){
        // Prepare group lore
        ArrayList<String> groupLore = new ArrayList<>();
        groupLore.add("\u00A77Allow you to select a list of \u00A73Groups \u00A77that this " + containerType + " should " + groupAction);
        Set<AbstractMaterialGroup> grouos = container.getSelectedGroups();
        if (grouos.isEmpty()) {
            groupLore.add("\u00A77There is no "+groupAction+"d group for this "+containerType+".");
        } else {
            groupLore.add("\u00A77List of "+groupAction+"d groups for this "+containerType+":");
            Iterator<AbstractMaterialGroup> groupIterator = grouos.iterator();

            boolean greaterThanMax = grouos.size() > 5;
            int maxindex = (greaterThanMax ? 4 : grouos.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Melee Weapons"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(groupIterator.next().getName());
                groupLore.add("\u00A77- \u00A73" + formattedName);

            }
            if (greaterThanMax) {
                groupLore.add("\u00A77And " + (grouos.size() - 4) + " more...");
            }
        }
        return groupLore;
    }

}
