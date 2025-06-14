package xyz.alexcrea.cuanvil.config;

import com.google.common.io.Files;
import io.delilaheve.CustomAnvil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.group.EnchantConflictManager;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.recipe.CustomAnvilRecipeManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@SuppressWarnings("unused")
public abstract class ConfigHolder {

    // Available configuration:
    public static DefaultConfigHolder DEFAULT_CONFIG;
    public static ItemGroupConfigHolder ITEM_GROUP_HOLDER;
    public static ConflictConfigHolder CONFLICT_HOLDER;
    public static UnitRepairHolder UNIT_REPAIR_HOLDER;
    public static CustomAnvilCraftHolder CUSTOM_RECIPE_HOLDER;

    /**
     * Load default configuration.
     *
     * @return True if successful.
     */
    public static boolean loadDefaultConfig() {
        DEFAULT_CONFIG = new DefaultConfigHolder();

        return DEFAULT_CONFIG.reloadFromDisk(true);
    }

    /**
     * Load non default configuration.
     *
     * @return True if successful.
     */
    public static boolean loadNonDefaultConfig() {
        ITEM_GROUP_HOLDER = new ItemGroupConfigHolder();
        CONFLICT_HOLDER = new ConflictConfigHolder();
        UNIT_REPAIR_HOLDER = new UnitRepairHolder();
        CUSTOM_RECIPE_HOLDER = new CustomAnvilCraftHolder();

        return removeNonDefaultFromDisk(true);
    }

    public static boolean reloadAllFromDisk(boolean hardfail) {
        boolean sucess = DEFAULT_CONFIG.reloadFromDisk(hardfail);
        if (!sucess) return false;

        return removeNonDefaultFromDisk(hardfail);
    }

    private static boolean removeNonDefaultFromDisk(boolean hardfail) {
        boolean sucess = ITEM_GROUP_HOLDER.reloadFromDisk(hardfail);
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
        CustomAnvil.Companion.log("Saving " + getConfigFileName());
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
            CustomAnvil.instance.getLogger().log(Level.SEVERE, "Could not save config...", e);
            return false;
        }

        CustomAnvil.Companion.log(getConfigFileName() + " saved successfully");
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
                CustomAnvil.instance.getLogger().log(Level.WARNING, "Could not copy backup saving config " + base.getName(), e);
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
            CustomAnvil.instance.getLogger().log(Level.SEVERE, "Exception while moving backup file " + base.getName(), e);
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

    public abstract static class DeletableResource extends ResourceConfigHolder {

        private static final String DELETED_FOLDER_PATH = "deleted";

        private final @NotNull File parent;
        private final @NotNull File deletedConfigFile;

        private @Nullable YamlConfiguration deletedListConfig;

        private DeletableResource(String resourceName) {
            super(resourceName);
            this.parent = new File(CustomAnvil.instance.getDataFolder(), DELETED_FOLDER_PATH);
            this.deletedConfigFile = new File(this.parent, "deleted_" + resourceName + getConfigFileExtension());
        }

        @Override
        public boolean reloadFromDisk(boolean hardFail) {
            if (!super.reloadFromDisk(hardFail)) return false;
            loadDeletedListFile(hardFail);

            return true;
        }

        private void loadDeletedListFile(boolean hardFail) {
            this.deletedListConfig = CustomAnvil.instance.reloadResource(this.deletedConfigFile, hardFail);

        }

        /**
         * Test if the provided element was deleted.
         *
         * @param objectPath The object path to delete.
         * @return True if successful.
         */
        public boolean isDeleted(String objectPath) {
            if (this.deletedListConfig == null) return false;

            return this.deletedListConfig.getBoolean(objectPath, false);
        }

        /**
         * Delete a certain object by its path. do not save the config.
         *
         * @param objectPath The object path to delete.
         * @return True if successful.
         */
        public boolean delete(String objectPath) {
            return delete(objectPath, false, false);
        }

        /**
         * Delete a certain object by its path.
         *
         * @param objectPath The object path to delete.
         * @param doSave     If we should save the config after deleting.
         * @param doBackup   If we should create a backup.
         * @return True if successful.
         */
        public boolean delete(String objectPath, boolean doSave, boolean doBackup) {
            // Create deleted list if it does not yet exist
            if (this.deletedListConfig == null) {
                this.parent.mkdirs();
                try {
                    if (!this.deletedConfigFile.createNewFile()) {
                        throw new RuntimeException("Could not create \"deleted config\" file");
                    }
                } catch (IOException e) {
                    CustomAnvil.instance.getLogger().log(Level.WARNING, "Could not create " + this.deletedConfigFile.getPath(), e);
                }
                loadDeletedListFile(false);

                // Something was wrong somehow
                if (this.deletedListConfig == null) return false;
            }

            // Add to the deleted config
            this.deletedListConfig.set(objectPath, true);
            this.getConfig().set(objectPath, null);

            // Save the deleted config (may not be the most efficient, but I will handle it later)
            if (doSave) {
                return saveToDisk(doBackup);
            }

            return true;
        }

        @Override
        public boolean saveToDisk(boolean doBackup) {
            boolean deletedSaveSuccess = saveDeletedList();

            return super.saveToDisk(doBackup) && deletedSaveSuccess;
        }

        /**
         * Save list of deleted elements.
         *
         * @return true if successful.
         */
        public boolean saveDeletedList() {
            if (this.deletedListConfig == null) return true;

            try {
                this.deletedListConfig.save(this.deletedConfigFile);
            } catch (IOException e) {
                CustomAnvil.instance.getLogger().log(Level.WARNING, "Could not save " + this.deletedConfigFile.getPath(), e);
                return false;
            }

            return true;
        }


    }


    // Class for itemGroupsManager config
    public static class ItemGroupConfigHolder extends DeletableResource {
        private static final String FILE_NAME = "item_groups";

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
    public static class ConflictConfigHolder extends DeletableResource {
        private static final String FILE_NAME = "enchant_conflict";

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
    public static class UnitRepairHolder extends DeletableResource {
        private static final String ITEM_GROUP_FILE_NAME = "unit_repair_item";

        private UnitRepairHolder() {
            super(ITEM_GROUP_FILE_NAME);
        }

        @Override
        public void reload() {
        } // Do nothing

    }


    // Class for custom anvil craft
    public static class CustomAnvilCraftHolder extends DeletableResource {
        private static final String CUSTOM_RECIPE_FILE_NAME = "custom_recipes";
        CustomAnvilRecipeManager recipeManager;

        private CustomAnvilCraftHolder() {
            super(CUSTOM_RECIPE_FILE_NAME);
        }

        public CustomAnvilRecipeManager getRecipeManager() {
            return recipeManager;
        }

        @Override
        public void reload() {
            this.recipeManager = new CustomAnvilRecipeManager();
            this.recipeManager.prepareRecipes(this.configuration);
        }
    }


}
