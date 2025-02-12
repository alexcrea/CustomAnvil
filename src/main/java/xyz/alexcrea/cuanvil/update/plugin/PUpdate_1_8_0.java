package xyz.alexcrea.cuanvil.update.plugin;

import io.delilaheve.util.ConfigOptions;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.alexcrea.cuanvil.config.ConfigHolder;
import xyz.alexcrea.cuanvil.config.WorkPenaltyType;
import xyz.alexcrea.cuanvil.gui.config.settings.WorkPenaltyTypeSettingGui;
import xyz.alexcrea.cuanvil.util.AnvilUseType;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Set;

public class PUpdate_1_8_0 {

    private static final String WORK_PENALTY_TYPE = "work_penalty_type";

    public static void handleUpdate(@Nonnull Set<ConfigHolder> toSave) {
        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.getConfig();

        // We migrate the work penalty type if it exists
        String penaltyTypeValue = config.getString(WORK_PENALTY_TYPE);
        if (penaltyTypeValue == null) return;

        EnumMap<AnvilUseType, WorkPenaltyType.WorkPenaltyPart> partEnum;
        partEnum = new EnumMap<>(ConfigOptions.INSTANCE.getWorkPenaltyType().getPartMap());

        boolean keepIncrease;
        boolean keepAdditive;

        switch (penaltyTypeValue.toLowerCase()) {
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

        for (AnvilUseType type : partEnum.keySet()) {
            WorkPenaltyType.WorkPenaltyPart part = partEnum.get(type);
            part = new WorkPenaltyType.WorkPenaltyPart(
                    keepIncrease & part.penaltyIncrease(),
                    keepAdditive & part.penaltyAdditive(),
                    part.exclusivePenaltyIncrease(),
                    part.exclusivePenaltyAdditive());
            partEnum.replace(type, part);
        }

        if(WorkPenaltyTypeSettingGui.saveWorkPenalty(partEnum)){
            config.set(WORK_PENALTY_TYPE, null);
        }

        toSave.add(ConfigHolder.DEFAULT_CONFIG);
    }

}
