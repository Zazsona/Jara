package gui.headed;

import gui.headed.HeadedGUIUtil;
import javafx.application.Application;

public class HeadedGUI
{
    /**
     * Runs the GUI for first time setup
     */
    public static void performFirstTimeSetup()
    {
        Application.launch(HeadedGUIUtil.class);
    }

    /**
     * Shows an error on the GUI
     * @param errorMessage the error to display
     */
    public static void showError(String errorMessage)
    {
        HeadedGUIUtil.showErrorPane(errorMessage);
    }

    /**
     * Shows the update application token GUI
     * @return the application token
     */
    public static String updateToken()
    {
        return HeadedGUIUtil.showUpdateTokenPane();
    }
}
