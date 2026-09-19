package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.Version;

@NotNullByDefault
public class PUpdate_1_6_7 extends PluginUpdate {

    public PUpdate_1_6_7() {
        super(new Version(1, 6, 7));
    }

    @Override
    public void handleUpdate(UpdateHandler.UpdatedConfigList toSave) {
        FileConfiguration config = toSave.use(ConfigHolder.DEFAULT).getConfig();

        // We fix the density enchantment
        String value = config.getString("enchant_values.minecraft:density.item");
        if(value == null) value = config.getString("enchant_values.density.item");

        if(value == null || "1".equalsIgnoreCase(value)){
            config.set("enchant_values.minecraft:density.item", 2);
        }
    }

}
