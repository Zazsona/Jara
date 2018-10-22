package gui.headed;

import configuration.SettingsUtil;
import gui.HeadedGUI;
import jara.CommandAttributes;
import jara.CommandRegister;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static jara.CommandRegister.Category.*;

/**
 * This screen is based off of CommandConfigSetup, but replaces the button functionality.
 */
public class BotUpdateConfiguration extends Application
{
    @FXML
    private VBox botUpdateConfigurationScreen;
    @FXML
    private StackPane backButton;
    @FXML
    private Rectangle backRect;
    @FXML
    private StackPane nextButton;
    @FXML
    private Rectangle nextRect;
    @FXML
    private VBox adminList;
    @FXML
    private VBox gamesList;
    @FXML
    private VBox toysList;
    @FXML
    private VBox utilityList;
    @FXML
    private VBox audioList;
    @FXML
    private CheckBox adminCategoryCheckBox;
    @FXML
    private CheckBox audioCategoryCheckBox;
    @FXML
    private CheckBox gamesCategoryCheckBox;
    @FXML
    private CheckBox toysCategoryCheckBox;
    @FXML
    private CheckBox utilityCategoryCheckBox;

    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> System.exit(0));
        backButton.setOnMouseEntered((event) -> HeadedGUIUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) ->
                                     {
                                         try
                                         {
                                             SettingsUtil.getGlobalSettings().setCommandConfigMap(getCommandConfig());
                                             SettingsUtil.getGlobalSettings().save();
                                             HeadedGUIUtil.setSetupComplete(true);
                                             botUpdateConfigurationScreen.getScene().getWindow().hide();
                                         }
                                         catch (IOException e)
                                         {
                                             HeadedGUI.showError("Unable to save settings.");
                                             e.printStackTrace();
                                         }

                                     });
        nextButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(nextRect));

        adminCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(adminCategoryCheckBox, ADMIN));
        audioCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(audioCategoryCheckBox, AUDIO));
        gamesCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(gamesCategoryCheckBox, GAMES));
        toysCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(toysCategoryCheckBox, TOYS));
        utilityCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(utilityCategoryCheckBox, UTILITY));

        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            Pane topSpacePane = new Pane();
            topSpacePane.setMinHeight(5);
            topSpacePane.setMaxHeight(10);
            topSpacePane.setPrefHeight(10);
            Pane bottomSpacePane = new Pane();
            bottomSpacePane.setMinHeight(topSpacePane.getMinHeight());
            bottomSpacePane.setMaxHeight(topSpacePane.getMaxHeight());
            bottomSpacePane.setPrefHeight(topSpacePane.getPrefHeight());

            switch (ca.getCategoryID())
            {
                case GAMES:
                    gamesList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
                case UTILITY:
                    utilityList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
                case TOYS:
                    toysList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
                case AUDIO:
                    audioList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
                case ADMIN:
                    adminList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
            }
        }
    }

    /**
     * This method will add the required fields onto the menu for the passed command, allowing the user to view and select it.
     * @param commandAttributes
     * @return
     */
    private BorderPane generateCommandListElement(CommandAttributes commandAttributes)
    {
        BorderPane bp = new BorderPane();
        Pane topPane = new Pane();
        topPane.setStyle("-fx-background-color: #36393F;");
        topPane.setMinHeight(2);
        topPane.setMaxHeight(2);
        topPane.setPrefWidth(270);
        bp.setTop(topPane);
        Pane bottomPane = new Pane();
        bottomPane.setStyle(topPane.getStyle());
        bottomPane.setMinHeight(topPane.getMinHeight());
        bottomPane.setMaxHeight(topPane.getMaxHeight());
        bottomPane.setPrefWidth(topPane.getPrefWidth());
        bp.setBottom(bottomPane);
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        if (SettingsUtil.getGlobalSettings().getCommandConfigMap() != null) //Restore from existing config (if possible)
        {
            try
            {
                checkBox.setSelected(SettingsUtil.getGlobalSettings().getCommandConfigMap().get(commandAttributes.getCommandKey()));
            }
            catch (NullPointerException e) //New command
            {
                checkBox.setSelected(false);
                bp.setStyle("-fx-background-color: #51555e;");
            }
        }

        if (!commandAttributes.isDisableable())
        {
            checkBox.setDisable(true);
        }
        checkBox.setId(commandAttributes.getCommandKey()+"CheckBox");
        bp.setRight(checkBox);
        bp.setAlignment(checkBox, Pos.CENTER);
        VBox vbox = new VBox();
        Text nameText = new Text(commandAttributes.getCommandKey());
        Text descText = new Text(commandAttributes.getDescription());
        nameText.setFont(new Font("System", 12));
        nameText.setStyle("-fx-font-weight: bold;");
        nameText.setFill(Paint.valueOf("#FFFFFF"));
        nameText.setId(commandAttributes.getCommandKey()+"Title");
        descText.setFont(new Font("System", 12));
        descText.setFill(Paint.valueOf("#FFFFFF"));
        vbox.getChildren().addAll(nameText, descText);
        bp.setCenter(vbox);
        return bp;

    }

    /**
     * This method will have all commands in the category match the check box's state
     * @param categoryCheckBox
     * @param categoryID
     */
    private void toggleCategory(CheckBox categoryCheckBox, CommandRegister.Category categoryID)
    {
        for (CommandAttributes ca : CommandRegister.getCommandsInCategory(categoryID))
        {
            CheckBox checkBox = (CheckBox) botUpdateConfigurationScreen.lookup("#" + ca.getCommandKey() + "CheckBox");
            if (!checkBox.isDisabled())
            {
                checkBox.setSelected(categoryCheckBox.isSelected());
            }
        }
    }

    /**
     * Retrives category IDs where at least one command is enabled.
     * @return
     */
    public ArrayList<CommandRegister.Category> getSupportedCategories()
    {
        ArrayList<CommandRegister.Category> supportedCategories = new ArrayList<>();
        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            if (!supportedCategories.contains(ca.getCategoryID()) && ca.getCategoryID() != NOGROUP)
            {
                if (((CheckBox) botUpdateConfigurationScreen.lookup("#"+ca.getCommandKey()+"CheckBox")).isSelected())
                {
                    supportedCategories.add(ca.getCategoryID());
                }
            }
        }
        return supportedCategories;
    }

    /**
     * Returns the selected commands as a CommandConfig compatible with GlobalSettings.
     * @return
     */
    public HashMap<String, Boolean> getCommandConfig()
    {
        HashMap<String, Boolean> commandConfig = new HashMap<>();

        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            if (CommandRegister.getCommand(ca.getCommandKey()).getCategoryID() != NOGROUP)
            {
                commandConfig.put(ca.getCommandKey(), ((CheckBox) botUpdateConfigurationScreen.lookup("#"+ca.getCommandKey()+"CheckBox")).isSelected());
            }
            else
            {
                commandConfig.put(ca.getCommandKey(), true);
            }

        }
        return commandConfig;
    }

    /**
     * @param root
     */
    public void setRoot(Parent root)
    {
        this.botUpdateConfigurationScreen = (VBox) root;
    }

    /**
     * @return
     */
    public Parent getRoot()
    {
        return botUpdateConfigurationScreen;
    }


    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader fxmlLoader = new FXMLLoader(BotUpdateConfiguration.class.getClassLoader().getResource("gui/botUpdateConfig.fxml"));
        Parent updateRoot = fxmlLoader.load();

        Stage stage = new Stage();
        stage.setTitle("Jara Setup");
        stage.setScene(new Scene(updateRoot, 1280, 800));
        stage.getIcons().add(new Image(HeadedGUIUtil.class.getClassLoader().getResourceAsStream("jara.png")));
        stage.show();
    }
}