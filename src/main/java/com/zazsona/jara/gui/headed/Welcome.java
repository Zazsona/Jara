package com.zazsona.jara.gui.headed;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Welcome extends Application
{

    @FXML
    private VBox welcomeScreen;
    @FXML
    private StackPane backButton;
    @FXML
    private Rectangle backRect;
    @FXML
    private StackPane nextButton;
    @FXML
    private Rectangle nextRect;
    @FXML
    private Text navBar_discord_text;
    @FXML
    private Text navBar_configuration_text;
    @FXML
    private Text navBar_review_text;

    /**
     * Initializes the UI, setting element functionality
     */
    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> HeadedGUIUtil.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedGUIUtil.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_discord_text));

        navBar_configuration_text.setOnMouseClicked((event -> HeadedGUIUtil.manageTitleSelection(navBar_configuration_text)));

        navBar_review_text.setOnMouseClicked((event -> HeadedGUIUtil.manageTitleSelection(navBar_review_text)));

    }

    /**
     * Displays this screen on the stage.
     * @param stage the stage to show on
     */
    public void show(Stage stage)
    {
        stage.getScene().setRoot(welcomeScreen);
    }

    @Override
    public void start(Stage primaryStage)
    {
        if (primaryStage.getScene() != null)
        {
            primaryStage.getScene().setRoot(welcomeScreen);
        }
        else
        {
            primaryStage.setTitle("Jara Setup");
            primaryStage.setScene(new Scene(welcomeScreen, 1280, 800));
        }
        primaryStage.show();
    }

    /**
     * Sets the root
     * @param root the root
     */
    public void setRoot(Parent root)
    {
        this.welcomeScreen = (VBox) root;
    }

    /**
     * Gets the root
     * @return the root
     */
    public Parent getRoot()
    {
        return welcomeScreen;
    }
}
