package xyz.alexcrea.cuanvil.gui.config;

import org.bukkit.Material;
import xyz.alexcrea.cuanvil.util.CasedStringUtil;

import java.util.*;

public interface SelectMaterialContainer {

    EnumSet<Material> getSelectedMaterials();

    boolean setSelectedMaterials(EnumSet<Material> materials);

    EnumSet<Material> illegalMaterials();

    static List<String> getMaterialLore(SelectMaterialContainer container, String containerType, String action){
        // Prepare material lore
        ArrayList<String> groupLore = new ArrayList<>();
        groupLore.add("§7Allow you to select a list of §ematerials §7that this " + containerType + " should " + action);
        Set<Material> materialSet = container.getSelectedMaterials();
        if (materialSet.isEmpty()) {
            groupLore.add("§7There is no "+action+"d material for this "+containerType+".");
        } else {
            groupLore.add("§7List of "+action+"d materials for this "+containerType+":");
            Iterator<Material> materialIterator = materialSet.iterator();

            boolean greaterThanMax = materialSet.size() > 5;
            int maxindex = (greaterThanMax ? 4 : materialSet.size());
            for (int i = 0; i < maxindex; i++) {
                // format string like "- Stone Sword"
                String formattedName = CasedStringUtil.snakeToUpperSpacedCase(materialIterator.next().name().toLowerCase());
                groupLore.add("§7- §e" + formattedName);

            }
            if (greaterThanMax) {
                groupLore.add("§7And " + (materialSet.size() - 4) + " more...");
            }
        }
        return groupLore;
    }

}
