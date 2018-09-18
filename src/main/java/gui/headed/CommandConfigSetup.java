package gui.headed;

import configuration.JsonFormats;
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

import java.lang.reflect.Array;
import java.util.ArrayList;

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

    private ArrayList<Integer> supportedCategories;

    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> HeadedGUIManager.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIManager.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIManager.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedGUIManager.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedGUIManager.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIManager.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) -> HeadedGUIManager.manageTitleSelection(navBar_discord_text));

        navBar_welcome_text.setOnMouseClicked((event) -> HeadedGUIManager.manageTitleSelection(navBar_welcome_text));

        navBar_review_text.setOnMouseClicked((event) -> HeadedGUIManager.manageTitleSelection(navBar_review_text));

        adminCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(adminCategoryCheckBox, CommandRegister.ADMIN));
        audioCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(audioCategoryCheckBox, CommandRegister.AUDIO));
        gamesCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(gamesCategoryCheckBox, CommandRegister.GAMES));
        toysCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(toysCategoryCheckBox, CommandRegister.TOYS));
        utilityCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(utilityCategoryCheckBox, CommandRegister.UTILITY));

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
                case CommandRegister.GAMES:
                    gamesList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
                case CommandRegister.UTILITY:
                    utilityList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
                case CommandRegister.TOYS:
                    toysList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
                case CommandRegister.AUDIO:
                    audioList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
                case CommandRegister.ADMIN:
                    adminList.getChildren().addAll(topSpacePane, generateCommandListElement(ca), bottomSpacePane);
                    break;
            }
        }
    }
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
    private void toggleCategory(CheckBox categoryCheckBox, int categoryID)
    {
        CommandAttributes[] ca = CommandRegister.getCommandsInCategory(categoryID);
        for (int i = 0; i<ca.length; i++)
        {
            CheckBox checkBox = (CheckBox) ccSetupScreen.lookup("#"+ca[i].getCommandKey()+"CheckBox");
            if (!checkBox.isDisabled())
            {
                checkBox.setSelected(categoryCheckBox.isSelected());
            }
        }
    }

    public ArrayList<Integer> getSupportedCategories()
    {
        supportedCategories = new ArrayList<>();
        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            if (!supportedCategories.contains(ca.getCategoryID()) && ca.getCategoryID() != CommandRegister.NOGROUP)
            {
                if (((CheckBox) ccSetupScreen.lookup("#"+ca.getCommandKey()+"CheckBox")).isSelected())
                {
                    supportedCategories.add(ca.getCategoryID());
                }
            }
        }
        return supportedCategories;
    }

    public ArrayList<JsonFormats.GlobalCommandConfigJson> getCommandConfig()
    {
        ArrayList<JsonFormats.GlobalCommandConfigJson> commandConfig = new ArrayList<>();

        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            commandConfig.add(new JsonFormats().new GlobalCommandConfigJson(ca.getCommandKey(), ((CheckBox) ccSetupScreen.lookup("#"+ca.getCommandKey()+"CheckBox")).isSelected()));
        }
        return commandConfig;
    }

    public void setRoot(Parent root)
    {
        this.ccSetupScreen = (VBox) root;
    }
    public Parent getRoot()
    {
        return ccSetupScreen;
    }



}
