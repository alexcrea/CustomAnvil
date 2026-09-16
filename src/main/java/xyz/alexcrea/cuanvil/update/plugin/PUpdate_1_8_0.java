package xyz.alexcrea.cuanvil.update.plugin;

import io.delilaheve.util.ConfigOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.anvil.AnvilUseType;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.config.WorkPenaltyType;
import xyz.alexcrea.cuanvil.gui.config.settings.WorkPenaltyTypeSettingGui;
import xyz.alexcrea.cuanvil.update.UpdateHandler;
import xyz.alexcrea.cuanvil.update.Version;

import java.util.EnumMap;

@NotNullByDefault
public class PUpdate_1_8_0 extends PluginUpdate {

    private static final String WORK_PENALTY_TYPE = "work_penalty_type";

    public PUpdate_1_8_0() {
        super(new Version(1, 8, 0));
    }

    @Override
    public void handleUpdate(UpdateHandler.UpdatedConfigList toSave) {
        FileConfiguration config = toSave.use(ConfigHolder.DEFAULT).getConfig();

        // We migrate the work penalty type if it exists
        String penaltyTypeValue = config.getString(WORK_PENALTY_TYPE);
        if(penaltyTypeValue == null) return;

        EnumMap<AnvilUseType, WorkPenaltyType.WorkPenaltyPart> partEnum;
        partEnum = new EnumMap<>(ConfigOptions.INSTANCE.getWorkPenaltyType().partMap());

        boolean keepIncrease;
        boolean keepAdditive;

        //noinspection EnhancedSwitchMigration
        switch(penaltyTypeValue.toLowerCase()) {
            case "add_only":
                keepIncrease = false;
                keepAdditive = true;
                break;
            case "increase_only":
                keepIncrease = true;
                keepAdditive = false;
                break;
            case "disabled":
                keepIncrease = false;
                keepAdditive = false;
                break;
            default:
                keepIncrease = true;
                keepAdditive = true;
        }

        for(AnvilUseType type : partEnum.keySet()) {
            WorkPenaltyType.WorkPenaltyPart part = partEnum.get(type);
            part = new WorkPenaltyType.WorkPenaltyPart(
                    keepIncrease & part.penaltyIncrease(),
                    keepAdditive & part.penaltyAdditive(),
                    part.exclusivePenaltyIncrease(),
                    part.exclusivePenaltyAdditive());
            partEnum.replace(type, part);
        }

        if(WorkPenaltyTypeSettingGui.saveWorkPenalty(partEnum)) {
            config.set(WORK_PENALTY_TYPE, null);
        }
    }

}
