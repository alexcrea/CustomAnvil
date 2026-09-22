package xyz.alexcrea.cuanvil.config;

import com.google.common.io.Files;
import io.delilaheve.CustomAnvil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.cuanvil.group.EnchantConflictManager;
import xyz.alexcrea.cuanvil.group.ItemGroupManager;
import xyz.alexcrea.cuanvil.recipe.CustomAnvilRecipeManager;
import xyz.alexcrea.cuanvil.util.LockedObjectProvider;
import xyz.alexcrea.cuanvil.util.MetricsUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;

@SuppressWarnings("unused")
@NotNullByDefault
public abstract class ConfigHolder {

    private static final ReentrantReadWriteLock DEFAULT_CONFIG_LOCK = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock ITEM_GROUP_LOCK = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock CONFLICT_LOCK = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock UNIT_REPAIR_LOCK = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock CUSTOM_RECIPE_LOCK = new ReentrantReadWriteLock();

    // Available configuration:
    // TODO replace usage with lock usage and set it to private
    public static @Nullable DefaultConfigHolder DEFAULT_CONFIG;

    public static @Nullable ItemGroupConfigHolder ITEM_GROUP_HOLDER;
    public static @Nullable ConflictConfigHolder CONFLICT_HOLDER;
    public static @Nullable UnitRepairHolder UNIT_REPAIR_HOLDER;
    public static @Nullable CustomAnvilCraftHolder CUSTOM_RECIPE_HOLDER;

    public static <T> LockedObjectProvider<T> createLocked(Supplier<@Nullable T> config, ReentrantReadWriteLock lock) {
        return new LockedObjectProvider<>(() -> {
            var value = config.get();
            if(value == null) {
                Bukkit.getPluginManager().disablePlugin(CustomAnvil.Companion.getInstance());
                throw new IllegalStateException("Configuration is not in a proper state... stoping...");
            }
            return value;
        }, lock
        );
    }

    public static final LockedObjectProvider<DefaultConfigHolder> DEFAULT =
            createLocked(() -> DEFAULT_CONFIG, DEFAULT_CONFIG_LOCK);
    public static final LockedObjectProvider<ItemGroupConfigHolder> ITEM_GROUP =
            createLocked(() -> ITEM_GROUP_HOLDER, ITEM_GROUP_LOCK);
    public static final LockedObjectProvider<ConflictConfigHolder> CONFLICT =
            createLocked(() -> CONFLICT_HOLDER, CONFLICT_LOCK);
    public static final LockedObjectProvider<UnitRepairHolder> UNIT_REPAIR =
            createLocked(() -> UNIT_REPAIR_HOLDER, UNIT_REPAIR_LOCK);
    public static final LockedObjectProvider<CustomAnvilCraftHolder> CUSTOM_RECIPE =
            createLocked(() -> CUSTOM_RECIPE_HOLDER, CUSTOM_RECIPE_LOCK);

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

    private static <T extends ConfigHolder> boolean reloadFromLocked(LockedObjectProvider<T> provider, boolean hardfail) {
        try(var lock = provider.write) {
            var config = lock.get();
            return config.reloadFromDisk(hardfail);
        }
    }

    public static boolean reloadAllFromDisk(boolean hardfail) {
        if(!reloadFromLocked(DEFAULT, hardfail)) return false;

        return removeNonDefaultFromDisk(hardfail);
    }

    private static boolean removeNonDefaultFromDisk(boolean hardfail) {
        if(!reloadFromLocked(ITEM_GROUP, hardfail)) return false;
        if(!reloadFromLocked(CONFLICT, hardfail)) return false;
        if(!reloadFromLocked(UNIT_REPAIR, hardfail)) return false;
        return reloadFromLocked(CUSTOM_RECIPE, hardfail);
    }


    // useful part of the file
    private static final File BACKUP_FOLDER = new File(CustomAnvil.instance.getDataFolder(), "backup");

    protected @Nullable FileConfiguration configuration;

    protected ConfigHolder() {

    }

    public abstract boolean reloadFromDisk(boolean hardFail);

    public abstract void reload();

    public FileConfiguration getConfig() {
        if(configuration == null) throw new IllegalStateException("Configuration is not initialized yet");

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

    public abstract LockedObjectProvider.LockedWrite<? extends ConfigHolder> getWriteLock();

    // Save logic
    public boolean saveToDisk(boolean doBackup) {
        try(var lock = getWriteLock()) {
            lock.lock();

            return saveToDiskUnsafe(doBackup);
        }
    }

    public boolean saveToDiskUnsafe(boolean doBackup) {
        CustomAnvil.Companion.log("Saving " + getConfigFileName());
        if(doBackup) {
            if(!saveBackup()) {
                CustomAnvil.instance.getLogger().severe("Could not save backup. see above.");
                return false;
            }
        }
        File base = getConfigFile();
        // if file exist and can't be deleted the file, then we gave up.
        if(base.exists() && !base.delete()) {
            CustomAnvil.instance.getLogger().severe("Could not save config: can't delete existing file.");
            return false;
        }
        FileConfiguration config = getConfig();
        try {
            config.save(base);
        } catch(IOException e) {
            e.printStackTrace();
            CustomAnvil.instance.getLogger().severe("Could not save config...");
            return false;
        }

        CustomAnvil.Companion.log(getConfigFileName() + " saved successfully");
        return true;
    }

    protected final boolean saveBackup() {
        try(var lock = getWriteLock()) {
            lock.lock();

            return saveBackupUnsafe();
        }
    }

    private boolean saveBackupUnsafe() {
        File base = getConfigFile();
        if(!base.exists()) return true; // We did back up everything we had to (nothing in this case)
        boolean sufficientSuccess = false;

        BACKUP_FOLDER.mkdirs();
        // save first backup if it does not exist
        File firstBackup = getFirstBackup();
        if(!firstBackup.exists()) {
            try {
                Files.copy(base, firstBackup);
                sufficientSuccess = true;
            } catch(IOException e) {
                CustomAnvil.instance.getLogger().log(Level.WARNING, "Could not copy backup saving config " + base.getName(), e);
                MetricsUtil.INSTANCE.trackError(e);
            }
        }
        // save last backup
        File lastBackup = getLastBackup();
        // if file exist and can't be deleted the file, then we gave up.
        if(lastBackup.exists() && !lastBackup.delete()) {
            return sufficientSuccess;
        }

        try {
            Files.move(base, lastBackup);
            sufficientSuccess = true;
        } catch(IOException e) {
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
            try(var lock = getWriteLock()) {
                lock.lock();

                CustomAnvil.instance.saveDefaultConfig();
                CustomAnvil.instance.reloadConfig();
                this.configuration = CustomAnvil.instance.getConfig();
            }
            return true;
        }

        @Override
        public LockedObjectProvider.LockedWrite<DefaultConfigHolder> getWriteLock() {
            return DEFAULT.write;
        }

        @Override
        public void reload() {
        }// Nothing to do

    }

    // Abstract class for non default config
    public abstract static class ResourceConfigHolder extends ConfigHolder {

        final String resourceName;

        private ResourceConfigHolder(String resourceName) {
            this.resourceName = resourceName;
        }

        @Override
        protected String getConfigFileName() {
            return resourceName;
        }

        @Override
        public boolean reloadFromDisk(boolean hardFail) {
            try(var lock = getWriteLock()) {
                lock.lock();

                YamlConfiguration configuration = CustomAnvil.instance.reloadResource(
                        getConfigFileName() + getConfigFileExtension(), hardFail);
                if(configuration == null)
                    return false;

                this.configuration = configuration;
                reload();
            }

            return true;
        }
    }

    public abstract static class DeletableResource extends ResourceConfigHolder {

        private static final String DELETED_FOLDER_PATH = "deleted";

        private final File parent;
        private final File deletedConfigFile;

        private @Nullable YamlConfiguration deletedListConfig;

        private DeletableResource(String resourceName) {
            super(resourceName);
            this.parent = new File(CustomAnvil.instance.getDataFolder(), DELETED_FOLDER_PATH);
            this.deletedConfigFile = new File(this.parent, "deleted_" + resourceName + getConfigFileExtension());
        }

        @Override
        public boolean reloadFromDisk(boolean hardFail) {
            try(var lock = getWriteLock()) {
                lock.lock();

                if(!super.reloadFromDisk(hardFail))
                    return false;

                loadDeletedListFile(hardFail);
            }

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
            if(this.deletedListConfig == null) return false;

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
            try(var lock = getWriteLock()) {
                lock.lock();
                return deleteUnsafe(objectPath, doSave, doBackup);
            }
        }

        private boolean deleteUnsafe(String objectPath, boolean doSave, boolean doBackup) {
            // Create deleted list if it does not yet exist
            if(this.deletedListConfig == null) {
                this.parent.mkdirs();
                try {
                    this.deletedConfigFile.createNewFile();
                } catch(IOException e) {
                    CustomAnvil.instance.getLogger().log(Level.WARNING, "Could not create " + this.deletedConfigFile.getPath(), e);
                    MetricsUtil.INSTANCE.trackError(e);
                }
                loadDeletedListFile(false);

                // Something was wrong somehow
                if(this.deletedListConfig == null) {
                    return false;
                }
            }

            // Add to the deleted config
            this.deletedListConfig.set(objectPath, true);
            this.getConfig().set(objectPath, null);

            // Save the deleted config (may not be the most efficient, but I will handle it later)
            if(doSave)
                return saveToDisk(doBackup);

            return true;
        }

        @Override
        public boolean saveToDisk(boolean doBackup) {
            try(var lock = getWriteLock()) {
                lock.lock();

                boolean deletedSaveSuccess = saveDeletedList();

                return super.saveToDisk(doBackup) && deletedSaveSuccess;
            }
        }

        /**
         * Save list of deleted elements.
         *
         * @return true if successful.
         */
        public boolean saveDeletedList() {
            try(var lock = getWriteLock()) {
                lock.lock();

                return saveDeletedListUnsafe();
            }
        }

        public boolean saveDeletedListUnsafe() {
            if(this.deletedListConfig == null) {
                return true;
            }

            try {
                this.deletedListConfig.save(this.deletedConfigFile);
            } catch(IOException e) {
                CustomAnvil.instance.getLogger().log(Level.WARNING, "Could not save " + this.deletedConfigFile.getPath(), e);
                MetricsUtil.INSTANCE.trackError(e);
                return false;
            }

            return true;
        }
    }


    // Class for itemGroupsManager config
    public static class ItemGroupConfigHolder extends DeletableResource {
        private static final String FILE_NAME = "item_groups";

        @Nullable ItemGroupManager itemGroupsManager;

        private ItemGroupConfigHolder() {
            super(FILE_NAME);
        }

        public ItemGroupManager getItemGroupsManager() {
            if(itemGroupsManager == null) throw new IllegalStateException("Configuration is not initialized yet");
            return itemGroupsManager;
        }

        @Override
        public void reload() {
            try(var lock = getWriteLock()) {
                lock.lock();

                reloadUnsafe();
            }
        }

        public void reloadUnsafe() {
            // not the most efficient way for in game reload TODO optimise
            this.itemGroupsManager = new ItemGroupManager();
            this.itemGroupsManager.prepareGroups(getConfig());

            try(var lock = CONFLICT.write) {
                var conflict = lock.get();

                if(conflict.configuration != null)
                    conflict.reload();
            }
        }

        @Override
        public LockedObjectProvider.LockedWrite<ItemGroupConfigHolder> getWriteLock() {
            return ITEM_GROUP.write;
        }
    }

    // Class for enchant conflict config
    public static class ConflictConfigHolder extends DeletableResource {
        private static final String FILE_NAME = "enchant_conflict";

        @Nullable EnchantConflictManager conflictManager;

        private ConflictConfigHolder() {
            super(FILE_NAME);
        }

        public EnchantConflictManager getConflictManager() {
            if(conflictManager == null) throw new IllegalStateException("Configuration is not initialized yet");
            return conflictManager;
        }

        // We assume this is called after item group manager reload
        @Override
        public void reload() {
            try(var lock = getWriteLock()) {
                lock.lock();

                reloadUnsafe();
            }
        }

        public void reloadUnsafe() {
            try(var lock = ITEM_GROUP.write) {
                var item_group = lock.get();

                // not the most efficient way for in game reload TODO optimise
                this.conflictManager = new EnchantConflictManager();
                this.conflictManager.prepareConflicts(getConfig(), item_group.getItemGroupsManager());
            }
        }

        @Override
        public LockedObjectProvider.LockedWrite<ConflictConfigHolder> getWriteLock() {
            return CONFLICT.write;
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

        @Override
        public LockedObjectProvider.LockedWrite<UnitRepairHolder> getWriteLock() {
            return UNIT_REPAIR.write;
        }
    }

    // Class for custom anvil craft
    public static class CustomAnvilCraftHolder extends DeletableResource {
        private static final String CUSTOM_RECIPE_FILE_NAME = "custom_recipes";
        @Nullable CustomAnvilRecipeManager recipeManager;

        private CustomAnvilCraftHolder() {
            super(CUSTOM_RECIPE_FILE_NAME);
        }

        public CustomAnvilRecipeManager getRecipeManager() {
            if(recipeManager == null) throw new IllegalStateException("Configuration is not initialized yet");
            return recipeManager;
        }

        @Override
        public void reload() {
            try(var lock = getWriteLock()) {
                lock.lock();

                this.recipeManager = new CustomAnvilRecipeManager();
                this.recipeManager.prepareRecipes(getConfig());
            }
        }

        @Override
        public LockedObjectProvider.LockedWrite<CustomAnvilCraftHolder> getWriteLock() {
            return CUSTOM_RECIPE.write;
        }
    }


}
