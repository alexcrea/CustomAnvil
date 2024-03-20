package xyz.alexcrea.cuanvil.gui.config;

import xyz.alexcrea.cuanvil.group.AbstractMaterialGroup;

import java.util.List;
import java.util.Set;

public interface SelectGroupContainer {

    Set<AbstractMaterialGroup> getSelectedGroups();

    boolean setSelectedGroups(Set<AbstractMaterialGroup> groups);

    Set<AbstractMaterialGroup> illegalGroups();

}
