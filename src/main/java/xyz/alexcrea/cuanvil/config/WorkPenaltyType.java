package xyz.alexcrea.cuanvil.config;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.anvil.AnvilUseType;

import java.util.Map;

@SuppressWarnings("unused")
@NotNullByDefault
public record WorkPenaltyType(ImmutableMap<AnvilUseType, WorkPenaltyPart> partMap) {

    public record WorkPenaltyPart(
            boolean penaltyIncrease,
            boolean penaltyAdditive,
            boolean exclusivePenaltyIncrease,
            boolean exclusivePenaltyAdditive
    ) {

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof WorkPenaltyPart(
                    boolean increase, boolean additive,
                    boolean exclusiveIncrease, boolean exclusiveAdditive
            ))) return false;

            return increase == this.penaltyIncrease &&
                    additive == this.penaltyAdditive &&
                    exclusiveIncrease == this.exclusivePenaltyIncrease &&
                    exclusiveAdditive == this.exclusivePenaltyAdditive;
        }

        public WorkPenaltyPart(boolean penaltyIncrease, boolean penaltyAdditive) {
            this(penaltyIncrease, penaltyAdditive, false, false);
        }
    }

    private static ImmutableMap<AnvilUseType, WorkPenaltyPart> asImmutable(
            @Nullable Map<AnvilUseType, WorkPenaltyPart> partMap
    ) {
        if(partMap == null)
            return ImmutableMap.of();
        else
            return ImmutableMap.copyOf(partMap);
    }

    public WorkPenaltyType(@Nullable Map<AnvilUseType, WorkPenaltyPart> partMap) {
        this(asImmutable(partMap));
    }

    public WorkPenaltyPart getPenaltyInfo(AnvilUseType type) {
        var result = partMap.getOrDefault(type, type.getDefaultPenalty());
        assert result != null;

        return result;
    }

    public boolean isPenaltyIncreasing(AnvilUseType type) {
        return getPenaltyInfo(type).penaltyIncrease;
    }

    public boolean isPenaltyAdditive(AnvilUseType type) {
        return getPenaltyInfo(type).penaltyAdditive;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof WorkPenaltyType that)) return false;

        for(AnvilUseType type : AnvilUseType.getEntries()) {
            if(!getPenaltyInfo(type).equals(that.getPenaltyInfo(type))) return false;
        }
        return true;
    }

}
