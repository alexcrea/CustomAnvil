package xyz.alexcrea.cuanvil.config;

import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.util.AnvilUseType;

import java.util.EnumMap;

public class WorkPenaltyType {

    public record WorkPenaltyPart(
            boolean penaltyIncrease,
            boolean penaltyAdditive) {

        public static WorkPenaltyPart ONLY_TRUE_PART = new WorkPenaltyPart(true, true);
    }

    private final EnumMap<AnvilUseType, WorkPenaltyPart> partMap;

    public WorkPenaltyType(@Nullable EnumMap<AnvilUseType, WorkPenaltyPart> partMap) {
        this.partMap = new EnumMap<>(partMap != null ? partMap : new EnumMap<>(AnvilUseType.class));
    }

    public WorkPenaltyPart getPenaltyInfo(AnvilUseType type) {
        return partMap.getOrDefault(type, WorkPenaltyPart.ONLY_TRUE_PART);
    }

    public boolean isPenaltyIncreasing(AnvilUseType type) {
        return partMap.getOrDefault(type, WorkPenaltyPart.ONLY_TRUE_PART).penaltyIncrease;
    }

    public boolean isPenaltyAdditive(AnvilUseType type) {
        return partMap.getOrDefault(type, WorkPenaltyPart.ONLY_TRUE_PART).penaltyAdditive;
    }

}
