package xyz.alexcrea.cuanvil.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum WorkPenaltyType {
    DEFAULT("default", true, true),
    INCREASE("increase_only", true, false),
    ADDITIVE("add_only", false, true),
    DISABLED("disabled", false, false),
    ;

    private final String name;
    private final boolean penaltyIncrease;
    private final boolean penaltyAdditive;

    WorkPenaltyType(String name, boolean penaltyIncrease, boolean penaltyAdditive) {
        this.name = name;
        this.penaltyIncrease = penaltyIncrease;
        this.penaltyAdditive = penaltyAdditive;
    }

    public boolean isPenaltyIncreasing() {
        return penaltyIncrease;
    }

    public boolean isPenaltyAdditive() {
        return penaltyAdditive;
    }

    private boolean doRepresentThisType(String toTest){
        return name.equalsIgnoreCase(toTest);
    }

    @NotNull
    public static WorkPenaltyType fromString(@Nullable String toTest){
        if(toTest == null) return DEFAULT;

        // Test if it matches any of values
        for (WorkPenaltyType value : values()) {
            if(value.doRepresentThisType(toTest)){
                return value;
            }
        }

        // Use default if not found
        return DEFAULT;
    }

}
