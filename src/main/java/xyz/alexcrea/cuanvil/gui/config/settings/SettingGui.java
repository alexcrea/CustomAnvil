package xyz.alexcrea.cuanvil.gui.config.settings;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;

public interface SettingGui {

    /**
     * Called when the associated setting need to be saved.
     *
     * @return true if the save was successful. false otherwise
     */
    boolean onSave();

    /**
     * If this function return true
     * the gui assume the associated setting can be saved.
     *
     * @return true if there is a change to the setting. false otherwise
     */
    boolean hadChange();

    interface SettingGuiFactory {

        /**
         * Create a gui using setting parameters and current setting value.
         *
         * @return A new instance of the implemented setting gui.
         */
        Gui create();

    }

}
