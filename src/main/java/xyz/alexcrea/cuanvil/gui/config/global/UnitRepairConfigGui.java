package xyz.alexcrea.cuanvil.gui.config.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.gui.config.list.MappedGuiListConfigGui;
import xyz.alexcrea.cuanvil.gui.config.list.UnitRepairElementListGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UnitRepairConfigGui extends MappedGuiListConfigGui<Material, UnitRepairElementListGui> {

    public final static UnitRepairConfigGui INSTANCE = new UnitRepairConfigGui();

    static {
        INSTANCE.init();
    }

    private UnitRepairConfigGui() {
        super("Unit Repair Config");
    }

    @Override
    protected UnitRepairElementListGui newInstanceOfGui(Material material, GuiItem item) {
        return new UnitRepairElementListGui(material, item);
    }

    @Override
    protected String genericDisplayedName() {
        return "item to be repaired";
    }

    @Override
    protected Material createAndSaveNewEmptyGeneric(String name) {
        return Material.getMaterial(name.toUpperCase());
    }

    @Override
    protected ItemStack createItemForGeneric(Material material) {
        return new ItemStack(material); //TODO proper item
    }

    @Override
    protected List<Material> getEveryDisplayableInstanceOfGeneric() {
        ArrayList<Material> materials = new ArrayList<>();

        for (String matName : ConfigHolder.UNIT_REPAIR_HOLDER.getConfig().getKeys(false)) {
            Material mat = Material.getMaterial(matName.toUpperCase());
            if(mat != null){
                materials.add(mat);
            }
        }
        return materials;
    }


}
