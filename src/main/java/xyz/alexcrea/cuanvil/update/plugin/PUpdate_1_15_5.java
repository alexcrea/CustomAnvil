package xyz.alexcrea.cuanvil.update.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.Version;

import static xyz.alexcrea.cuanvil.update.UpdateUtils.addAbsentToList;

@NotNullByDefault
public class PUpdate_1_15_5 extends PluginUpdate {

    public PUpdate_1_15_5() {
        super(new Version(1, 15, 5));
    }

    @Override
    public void handleUpdate(UpdateHandler.UpdatedConfigList toSave) {
        FileConfiguration config = toSave.use(ConfigHolder.CONFLICT).getConfig();

        if(config.isConfigurationSection("restriction_luck_of_the_sea")) return;

        // We fix the luck of the see enchantment
        addAbsentToList(config, "restriction_luck_of_the_sea.enchantments",
                "minecraft:luck_of_the_sea");
        addAbsentToList(config, "restriction_luck_of_the_sea.notAffectedGroups",
                "enchanted_book", "fishing_rod");
    }

}
