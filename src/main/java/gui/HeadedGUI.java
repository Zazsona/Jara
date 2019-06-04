package gui;

import gui.headed.HeadedGUIUtil;
import javafx.application.Application;

public class HeadedGUI
{
    public static void performFirstTimeSetup()
    {
        Application.launch(HeadedGUIUtil.class);
    }
    public static void showError(String errorMessage)
    {
        HeadedGUIUtil.showErrorPane(errorMessage);
    }
    public static String updateToken()
    {
        return HeadedGUIUtil.showUpdateTokenPane();
    }
}
