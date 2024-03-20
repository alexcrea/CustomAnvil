package xyz.alexcrea.cuanvil.gui.config;

import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;

import java.util.List;
import java.util.Set;

public interface SelectGroupContainer {

    List<AbstractMaterialGroup> getSelectedGroups();
    void setSelectedGroups(List<AbstractMaterialGroup> groups);

    Set<AbstractMaterialGroup> illegalGroups();

}
