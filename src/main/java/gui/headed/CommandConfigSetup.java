package gui.headed;

import configuration.SettingsUtil;
import jara.CommandAttributes;
import jara.CommandRegister;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;

import static jara.CommandRegister.Category.*;

public class CommandConfigSetup
{
    @FXML
    private VBox ccSetupScreen;
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
    private Text navBar_welcome_text;
    @FXML
    private Text navBar_review_text;
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
        backButton.setOnMouseClicked((event) -> HeadedGUIUtil.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedGUIUtil.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_discord_text));

        navBar_welcome_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_welcome_text));

        navBar_review_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_review_text));

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
     * Displays this screen on the stage.
     * @param stage
     */
    public void show(Stage stage)
    {
        stage.getScene().setRoot(ccSetupScreen);
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
            checkBox.setSelected(SettingsUtil.getGlobalSettings().getCommandConfigMap().get(commandAttributes.getCommandKey()));
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
            CheckBox checkBox = (CheckBox) ccSetupScreen.lookup("#" + ca.getCommandKey() + "CheckBox");
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
                if (((CheckBox) ccSetupScreen.lookup("#"+ca.getCommandKey()+"CheckBox")).isSelected())
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
                commandConfig.put(ca.getCommandKey(), ((CheckBox) ccSetupScreen.lookup("#"+ca.getCommandKey()+"CheckBox")).isSelected());
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
        this.ccSetupScreen = (VBox) root;
    }

    /**
     * @return
     */
    public Parent getRoot()
    {
        return ccSetupScreen;
    }



}
