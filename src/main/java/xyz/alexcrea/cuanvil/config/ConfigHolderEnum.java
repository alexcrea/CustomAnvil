package xyz.alexcrea.cuanvil.config;

public enum ConfigHolderEnum {

    DEFAULT(ConfigHolder.DEFAULT_CONFIG),
    ITEM_GROUP(ConfigHolder.ITEM_GROUP_HOLDER),
    CONFLICT(ConfigHolder.CONFLICT_HOLDER),
    UNIT_REPAIR(ConfigHolder.UNIT_REPAIR_HOLDER),
    CUSTOM_RECIPE(ConfigHolder.CUSTOM_RECIPE_HOLDER)
    ;

    private final ConfigHolder configHolder;

    ConfigHolderEnum(ConfigHolder configHolder) {
        this.configHolder = configHolder;
    }

    public ConfigHolder getConfigHolder() {
        return configHolder;
    }

}
