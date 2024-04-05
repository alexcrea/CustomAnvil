package xyz.alexcrea.cuanvil.config;

import com.google.common.io.Files;
import io.delilaheve.CustomAnvil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.alexcrea.cuanvil.group.EnchantConflictManager;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.recipe.CustomAnvilRecipeManager;
import xyz.alexcrea.cuanvil.util.MetricsUtil;

import java.io.File;
import java.io.IOException;

public abstract class ConfigHolder {

    // Available configuration:
    public static DefaultConfigHolder DEFAULT_CONFIG;
    public static ItemGroupConfigHolder ITEM_GROUP_HOLDER;
    public static ConflictConfigHolder CONFLICT_HOLDER;
    public static UnitRepairHolder UNIT_REPAIR_HOLDER;
    public static CustomAnvilCraftHolder CUSTOM_RECIPE_HOLDER;

    public static boolean loadConfig() {
        DEFAULT_CONFIG = new DefaultConfigHolder();
        ITEM_GROUP_HOLDER = new ItemGroupConfigHolder();
        CONFLICT_HOLDER = new ConflictConfigHolder();
        UNIT_REPAIR_HOLDER = new UnitRepairHolder();
        CUSTOM_RECIPE_HOLDER = new CustomAnvilCraftHolder();

        boolean result = reloadAllFromDisk(true);
        if (result) {
            MetricsUtil.INSTANCE.testIfConfigIsDefault();
        }
        return result;
    }

    public static boolean reloadAllFromDisk(boolean hardfail) {

        boolean sucess = DEFAULT_CONFIG.reloadFromDisk(hardfail);
        if (!sucess) return false;
        sucess = ITEM_GROUP_HOLDER.reloadFromDisk(hardfail);
        if (!sucess) return false;
        sucess = CONFLICT_HOLDER.reloadFromDisk(hardfail);
        if (!sucess) return false;
        sucess = UNIT_REPAIR_HOLDER.reloadFromDisk(hardfail);
        if (!sucess) return false;
        sucess = CUSTOM_RECIPE_HOLDER.reloadFromDisk(hardfail);

        return sucess;
    }


    // usefull part of the file
    private static final File BACKUP_FOLDER = new File(CustomAnvil.instance.getDataFolder(), "backup");

    protected FileConfiguration configuration;

    protected ConfigHolder() {

    }

    public abstract boolean reloadFromDisk(boolean hardFail);

    public abstract void reload();

    public FileConfiguration getConfig() {
        return configuration;
    }

    // Config name and files
    protected abstract String getConfigFileName();

    protected String getConfigFileExtension() {
        return ".yml";
    }

    protected File getConfigFile() {
        return new File(CustomAnvil.instance.getDataFolder(), getConfigFileName() + getConfigFileExtension());
    }

    protected File getFirstBackup() {
        return new File(BACKUP_FOLDER, getConfigFileName() + "-first" + getConfigFileExtension());
    }

    protected File getLastBackup() {
        return new File(BACKUP_FOLDER, getConfigFileName() + "-latest" + getConfigFileExtension());
    }

    // Save logic
    public boolean saveToDisk(boolean doBackup) {
        if (doBackup) {
            if (!saveBackup()) {
                CustomAnvil.instance.getLogger().severe("Could not save backup. see above.");
                return false;
            }
        }
        File base = getConfigFile();
        // if file exist and can't be deleted the file, then we gave up.
        if (base.exists() && !base.delete()) {
            CustomAnvil.instance.getLogger().severe("Could not save config: can't delete existing file.");
            return false;
        }
        FileConfiguration config = getConfig();
        try {
            config.save(base);
        } catch (IOException e) {
            e.printStackTrace();
            CustomAnvil.instance.getLogger().severe("Could not save config...");
            return false;
        }

        return true;
    }

    protected boolean saveBackup() {
        File base = getConfigFile();
        if (!base.exists()) return true; // We did back up everything we had to (nothing in this case)
        boolean sufficientSuccess = false;

        BACKUP_FOLDER.mkdirs();
        // save first backup if do not exist
        File firstBackup = getFirstBackup();
        if (!firstBackup.exists()) {
            try {
                Files.copy(base, firstBackup);
                sufficientSuccess = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // save last backup
        File lastBackup = getLastBackup();
        // if file exist and can't be deleted the file, then we gave up.
        if (lastBackup.exists() && !lastBackup.delete()) {
            return sufficientSuccess;
        }

        try {
            Files.move(base, lastBackup);
            sufficientSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sufficientSuccess;
    }

    public static class DefaultConfigHolder extends ConfigHolder {

        @Override
        protected String getConfigFileName() {
            return "config";
        }

        @Override
        public boolean reloadFromDisk(boolean hardFail) {
            CustomAnvil.instance.saveDefaultConfig();
            CustomAnvil.instance.reloadConfig();
            this.configuration = CustomAnvil.instance.getConfig();
            return true;
        }

        @Override
        public void reload() {
        }// Nothing to do

    }

    // Abstract class for non default config
    public abstract static class ResourceConfigHolder extends ConfigHolder {

        String resourceName;

        private ResourceConfigHolder(String resourceName) {
            this.resourceName = resourceName;
        }

        @Override
        protected String getConfigFileName() {
            return resourceName;
        }

        @Override
        public boolean reloadFromDisk(boolean hardFail) {
            YamlConfiguration configuration = CustomAnvil.instance.reloadResource(
                    getConfigFileName() + getConfigFileExtension(), hardFail);
            if (configuration == null) return false;
            this.configuration = configuration;
            reload();
            return true;
        }

    }

    // Class for itemGroupsManager config
    public static class ItemGroupConfigHolder extends ResourceConfigHolder {
        private final static String FILE_NAME = "item_groups";

        ItemGroupManager itemGroupsManager;

        private ItemGroupConfigHolder() {
            super(FILE_NAME);
        }

        public ItemGroupManager getItemGroupsManager() {
            return itemGroupsManager;
        }

        @Override
        public void reload() {
            // not the most efficient way for in game reload TODO optimise
            this.itemGroupsManager = new ItemGroupManager();
            this.itemGroupsManager.prepareGroups(this.configuration);

            if (CONFLICT_HOLDER.getConfig() != null) {
                CONFLICT_HOLDER.reload();
            }
        }

    }

    // Class for enchant conflict config
    public static class ConflictConfigHolder extends ResourceConfigHolder {
        private final static String FILE_NAME = "enchant_conflict";

        EnchantConflictManager conflictManager;

        private ConflictConfigHolder() {
            super(FILE_NAME);
        }

        public EnchantConflictManager getConflictManager() {
            return conflictManager;
        }

        // We assume this is called after item group manager reload;,
        @Override
        public void reload() {
            // not the most efficient way for in game reload TODO optimise
            this.conflictManager = new EnchantConflictManager();
            this.conflictManager.prepareConflicts(this.configuration, ITEM_GROUP_HOLDER.getItemGroupsManager());
        }

    }

    // Class for unit repair config
    public static class UnitRepairHolder extends ResourceConfigHolder {
        private final static String ITEM_GROUP_FILE_NAME = "unit_repair_item";


        private UnitRepairHolder() {
            super(ITEM_GROUP_FILE_NAME);
        }

        @Override
        public void reload() {
        } // Do nothing

    }


    // Class for custom anvil craft
    public static class CustomAnvilCraftHolder extends ResourceConfigHolder {
        private final static String CUSTOM_CRAFT_FILE_NAME = "custom_recipes";
        CustomAnvilRecipeManager recipeManager;

        private CustomAnvilCraftHolder() {
            super(CUSTOM_CRAFT_FILE_NAME);
        }

        @Override
        public void reload() {
            this.recipeManager = new CustomAnvilRecipeManager();
            this.recipeManager.prepareRecipes(this.configuration);
        }

    }


}
