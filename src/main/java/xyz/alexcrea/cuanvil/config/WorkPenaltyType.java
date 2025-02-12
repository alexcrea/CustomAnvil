package xyz.alexcrea.cuanvil.config;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.util.AnvilUseType;

import java.util.EnumMap;

public class WorkPenaltyType {

    public record WorkPenaltyPart(
            boolean penaltyIncrease,
            boolean penaltyAdditive) {

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof WorkPenaltyPart other)) return false;

            return other.penaltyIncrease == this.penaltyIncrease && other.penaltyAdditive == this.penaltyAdditive;
        }
    }

    private final EnumMap<AnvilUseType, WorkPenaltyPart> partMap;

    public WorkPenaltyType(@Nullable EnumMap<AnvilUseType, WorkPenaltyPart> partMap) {
        this.partMap = new EnumMap<>(partMap != null ? partMap : new EnumMap<>(AnvilUseType.class));
    }

    public ImmutableMap<AnvilUseType, WorkPenaltyPart> getPartMap() {
        return ImmutableMap.copyOf(partMap);
    }

    public WorkPenaltyPart getPenaltyInfo(AnvilUseType type) {
        return partMap.getOrDefault(type, type.getDefaultPenalty());
    }

    public boolean isPenaltyIncreasing(AnvilUseType type) {
        return partMap.getOrDefault(type, type.getDefaultPenalty()).penaltyIncrease;
    }

    public boolean isPenaltyAdditive(AnvilUseType type) {
        return partMap.getOrDefault(type, type.getDefaultPenalty()).penaltyAdditive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkPenaltyType that)) return false;

        for (AnvilUseType type : AnvilUseType.getEntries()) {
            if(!getPenaltyInfo(type).equals(that.getPenaltyInfo(type))) return false;
        }
        return true;
    }

}
