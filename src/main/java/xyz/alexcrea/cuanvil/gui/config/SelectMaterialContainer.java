package xyz.alexcrea.cuanvil.gui.config;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.lang.MsgUI;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@NotNullByDefault
public interface SelectMaterialContainer {

    Set<NamespacedKey> getSelectedMaterials();

    boolean setSelectedMaterials(Set<NamespacedKey> materials);

    Set<NamespacedKey> illegalMaterials();

    static List<Component> getMaterialLore(SelectMaterialContainer container, String containerType, String action) {
        // Prepare material lore
        List<Component> groupLore = MsgUI.SELECT_MATERIAL_HEADER.formatted(containerType, action);

        Set<NamespacedKey> materialSet = container.getSelectedMaterials();
        if(materialSet.isEmpty()) {
            groupLore.addAll(MsgUI.SELECT_MATERIAL_EMPTY.formatted(containerType, action));
            return groupLore;
        }

        groupLore.addAll(MsgUI.SELECT_MATERIAL_NOT_EMPTY.formatted(containerType, action));
        Iterator<NamespacedKey> materialIterator = materialSet.iterator();

        boolean greaterThanMax = materialSet.size() > 5;
        int maxIndex = (greaterThanMax ? 4 : materialSet.size());
        for(int i = 0; i < maxIndex; i++) {
            // format string like "- Stone Sword"
            String formattedName = CasedStringUtil.snakeToUpperSpacedCase(materialIterator.next().getKey().toLowerCase());
            groupLore.addAll(MsgUI.SELECT_MATERIAL_ITEM.formatted(formattedName));

        }
        if(greaterThanMax) {
            var count = materialSet.size() - maxIndex;
            groupLore.addAll(MsgUI.SELECT_MATERIAL_AND_MORE.formatted(count));
        }
        return groupLore;
    }

}
