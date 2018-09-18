package gui.headed;

import jara.CommandAttributes;
import jara.CommandRegister;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.control.CheckBox;

import java.io.IOException;
import java.util.ArrayList;

public class CommandConfigSetup extends Application
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

    private ArrayList<String> enabledCommands;
    private boolean adminToggle;
    private boolean audioToggle;
    private boolean gamesToggle;
    private boolean toysToggle;
    private boolean utilityToggle;

    public void initialize()
    {
        backButton.setOnMouseClicked((event) ->
                                     {
                                         saveState();
                                         HeadedGUIManager.goBack();

                                     });
        backButton.setOnMouseEntered((event) -> HeadedGUIManager.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIManager.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) ->
                                     {
                                         saveState();
                                         HeadedGUIManager.goNext();
                                     });
        nextButton.setOnMouseEntered((event) -> HeadedGUIManager.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIManager.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) ->
                                              {
                                                  saveState();
                                                  HeadedGUIManager.manageTitleSelection(navBar_discord_text);
                                              });

        navBar_welcome_text.setOnMouseClicked((event) ->
                                              {
                                                  saveState();
                                                  HeadedGUIManager.manageTitleSelection(navBar_welcome_text);
                                              });

        navBar_review_text.setOnMouseClicked((event) ->
                                             {
                                                 saveState();
                                                 HeadedGUIManager.manageTitleSelection(navBar_review_text);
                                             });

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
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/ccSetup.fxml"));

            restoreState();

            primaryStage.getScene().setRoot(root);
            primaryStage.show();

        }
        catch (IOException e)
        {
            e.printStackTrace();
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
    /*private void toggleCategory(CheckBox categoryCheckBox, int categoryID)
    {
        VBox list;
        switch (categoryID)
        {
            case CommandRegister.GAMES:
                list = gamesList;
                break;
            case CommandRegister.UTILITY:
                list = utilityList;
                break;
            case CommandRegister.TOYS:
                list = toysList;
                break;
            case CommandRegister.AUDIO:
                list = audioList;
                break;
            case CommandRegister.ADMIN:                                                         //Alternate method.
                list = adminList;
                break;
            default:
                list = new VBox();
        }
        for (Node node : list.getChildren())
        {
            if (node instanceof BorderPane)
            {
                CheckBox checkBox = ((CheckBox) ((BorderPane) node).getRight());
                if (!checkBox.isDisabled())
                {
                    checkBox.setSelected(categoryCheckBox.isSelected());
                }
            }
        }
    }*/
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
    private void saveState()
    {
        System.out.println("Saving state");
        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            enabledCommands.add(ca.getCommandKey());
        }
        adminToggle = adminCategoryCheckBox.isSelected();
        audioToggle = audioCategoryCheckBox.isSelected();
        gamesToggle = gamesCategoryCheckBox.isSelected();
        toysToggle = toysCategoryCheckBox.isSelected();
        utilityToggle = utilityCategoryCheckBox.isSelected();
    }
    private void restoreState()
    {
        System.out.println("Restoring state");
        if (enabledCommands != null)
        {
            for (CommandAttributes ca : CommandRegister.getRegister())
            {
                ((CheckBox) ccSetupScreen.lookup("#"+ca.getCommandKey()+"CheckBox")).setSelected(enabledCommands.contains(ca.getCommandKey())); //If the commandKey is in enabledCommands, it returns true. We just plug this directly as the state of the CheckBox, as if the commandKey is in the list, it is enabled..
            }
            adminCategoryCheckBox.setSelected(adminToggle);
            audioCategoryCheckBox.setSelected(audioToggle);
            gamesCategoryCheckBox.setSelected(gamesToggle);
            toysCategoryCheckBox.setSelected(toysToggle);
            utilityCategoryCheckBox.setSelected(utilityToggle);
        }
        enabledCommands = new ArrayList<String>();

    }



}
