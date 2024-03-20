package xyz.alexcrea.cuanvil.gui.config;

import org.bukkit.Material;

import java.util.EnumSet;

public interface SelectMaterialContainer {

    EnumSet<Material> getSelectedMaterials();

    boolean setSelectedMaterials(EnumSet<Material> materials);

    EnumSet<Material> illegalMaterials();

}
