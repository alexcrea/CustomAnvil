package xyz.alexcrea.cuanvil.gui.config;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@NotNullByDefault
public interface SelectGroupContainer {

    Set<AbstractMaterialGroup> getSelectedGroups();

    boolean setSelectedGroups(Set<AbstractMaterialGroup> groups);

    Set<AbstractMaterialGroup> illegalGroups();

    static List<Component> getGroupLore(SelectGroupContainer container, String containerType, String action){
        // Prepare group lore
        List<Component> groupLore = MsgUI.SELECT_GROUP_HEADER.formatted(containerType, action);

        Set<AbstractMaterialGroup> groups = container.getSelectedGroups();
        if (groups.isEmpty()) {
            groupLore.addAll(MsgUI.SELECT_GROUP_EMPTY.formatted(containerType, action));
            return groupLore;
        }

        groupLore.addAll(MsgUI.SELECT_GROUP_NOT_EMPTY.formatted(containerType, action));
        Iterator<AbstractMaterialGroup> groupIterator = groups.iterator();

        boolean greaterThanMax = groups.size() > 5;
        int maxIndex = (greaterThanMax ? 4 : groups.size());
        for (int i = 0; i < maxIndex; i++) {
            // format string like "- Melee Weapons"
            String formattedName = CasedStringUtil.snakeToUpperSpacedCase(groupIterator.next().getName());
            groupLore.addAll(MsgUI.SELECT_GROUP_ITEM.formatted(formattedName));

        }
        if (greaterThanMax) {
            var count = groups.size() - 4;
            groupLore.addAll(MsgUI.SELECT_GROUP_AND_MORE.formatted(count));
        }
        return groupLore;
    }

}
