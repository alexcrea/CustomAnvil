package xyz.alexcrea.cuanvil.gui.config;

import javafx.scene.paint.Material;
import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;

import java.util.List;
import java.util.Set;

public interface SelectMaterialContainer {

    List<Material> getSelectedMaterials();
    void setSelectedMaterials(List<Material> materials);

    Set<Material> illegalMaterials();

}
