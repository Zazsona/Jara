package gui.headed;

import configuration.SettingsUtil;
import jara.ModuleAttributes;
import jara.ModuleManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;

import static jara.ModuleManager.Category.*;

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
    private HBox categoryBox;
    @FXML
    private ScrollPane categoryScrollPane;
    @FXML
    private VBox adminList;
    @FXML
    private VBox gamesList;
    @FXML
    private VBox toysList;
    @FXML
    private VBox utilityList;
    @FXML
    private VBox seasonalList;
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
    @FXML
    private CheckBox seasonalCategoryCheckBox;

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

        navBar_welcome_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_welcome_text));

        navBar_review_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_review_text));

        categoryBox.setOnScroll(event -> //Set scrolling to be "set" - That is, no matter how many elements are in the list, the moved pixels is the same.
                                         {
                                             double change = categoryScrollPane.getWidth()/categoryScrollPane.getPrefWidth();
                                             double scrollAmount = (event.getDeltaY() < 0) ? -0.2f : 0.2f;
                                             categoryScrollPane.setHvalue(categoryScrollPane.getHvalue() + (scrollAmount*change));
                                         });

        adminCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(adminCategoryCheckBox, ADMIN));
        audioCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(audioCategoryCheckBox, AUDIO));
        gamesCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(gamesCategoryCheckBox, GAMES));
        toysCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(toysCategoryCheckBox, TOYS));
        utilityCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(utilityCategoryCheckBox, UTILITY));
        seasonalCategoryCheckBox.setOnMouseClicked((event) -> toggleCategory(seasonalCategoryCheckBox, SEASONAL));

        for (ModuleAttributes ma : ModuleManager.getModules())
        {
            Pane topSpacePane = new Pane();
            topSpacePane.setMinHeight(5);
            topSpacePane.setMaxHeight(10);
            topSpacePane.setPrefHeight(10);
            Pane bottomSpacePane = new Pane();
            bottomSpacePane.setMinHeight(topSpacePane.getMinHeight());
            bottomSpacePane.setMaxHeight(topSpacePane.getMaxHeight());
            bottomSpacePane.setPrefHeight(topSpacePane.getPrefHeight());

            switch (ma.getCategory())
            {
                case GAMES:
                    gamesList.getChildren().addAll(topSpacePane, generateCommandListElement(ma), bottomSpacePane);
                    break;
                case UTILITY:
                    utilityList.getChildren().addAll(topSpacePane, generateCommandListElement(ma), bottomSpacePane);
                    break;
                case TOYS:
                    toysList.getChildren().addAll(topSpacePane, generateCommandListElement(ma), bottomSpacePane);
                    break;
                case AUDIO:
                    audioList.getChildren().addAll(topSpacePane, generateCommandListElement(ma), bottomSpacePane);
                    break;
                case ADMIN:
                    adminList.getChildren().addAll(topSpacePane, generateCommandListElement(ma), bottomSpacePane);
                    break;
                case SEASONAL:
                    seasonalList.getChildren().addAll(topSpacePane, generateCommandListElement(ma), bottomSpacePane);
                    break;
            }
        }
    }
    /**
     * Displays this screen on the stage.
     * @param stage the stage to display on
     */
    public void show(Stage stage)
    {
        stage.getScene().setRoot(ccSetupScreen);
    }

    /**
     * This method will add the required fields onto the menu for the passed module, allowing the user to view and select it.
     * @param moduleAttributes the attributes to display
     * @return the root of the command list element
     */
    private BorderPane generateCommandListElement(ModuleAttributes moduleAttributes)
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
        if (SettingsUtil.getGlobalSettings().getModuleConfigMap() != null) //Restore from existing config (if possible)
        {
            try
            {
                checkBox.setSelected(SettingsUtil.getGlobalSettings().getModuleConfigMap().get(moduleAttributes.getKey()));
            }
            catch (NullPointerException e) //New command
            {
                bp.setStyle("-fx-background-color: #51555e;");
            }
        }

        if (!moduleAttributes.isDisableable())
        {
            checkBox.setDisable(true);
        }
        checkBox.setId(moduleAttributes.getKey()+"CheckBox");
        bp.setRight(checkBox);
        bp.setAlignment(checkBox, Pos.CENTER);
        VBox vbox = new VBox();
        String key = (moduleAttributes.getKey().length() > 22) ? moduleAttributes.getKey().substring(0, 19)+"..." : moduleAttributes.getKey();
        Text nameText = new Text(key);
        String description = (moduleAttributes.getDescription().length() > 30) ? moduleAttributes.getDescription().substring(0, 27)+"..." : moduleAttributes.getDescription();
        Text descText = new Text(description);
        nameText.setFont(new Font("System", 12));
        nameText.setStyle("-fx-font-weight: bold;");
        nameText.setFill(Paint.valueOf("#FFFFFF"));
        nameText.setId(moduleAttributes.getKey()+"Title");
        descText.setFont(new Font("System", 12));
        descText.setFill(Paint.valueOf("#FFFFFF"));
        vbox.getChildren().addAll(nameText, descText);
        bp.setCenter(vbox);
        return bp;

    }

    /**
     * This method will have all modules in the category match the check box's state
     * @param categoryCheckBox the check box for this category
     * @param categoryID the id of the category
     */
    private void toggleCategory(CheckBox categoryCheckBox, ModuleManager.Category categoryID)
    {
        for (ModuleAttributes ma : ModuleManager.getModulesInCategory(categoryID))
        {
            CheckBox checkBox = (CheckBox) ccSetupScreen.lookup("#" + ma.getKey() + "CheckBox");
            if (!checkBox.isDisabled())
            {
                checkBox.setSelected(categoryCheckBox.isSelected());
            }
        }
    }

    /**
     * Retrieves category IDs where at least one command is enabled.
     * @return list of supported categories
     */
    public ArrayList<ModuleManager.Category> getSupportedCategories()
    {
        ArrayList<ModuleManager.Category> supportedCategories = new ArrayList<>();
        for (ModuleAttributes ma : ModuleManager.getModules())
        {
            if (!supportedCategories.contains(ma.getCategory()) && ma.getCategory() != NOGROUP)
            {
                if (((CheckBox) ccSetupScreen.lookup("#"+ma.getKey()+"CheckBox")).isSelected())
                {
                    supportedCategories.add(ma.getCategory());
                }
            }
        }
        return supportedCategories;
    }

    /**
     * Returns the selected modules as a config compatible with GlobalSettings.
     * @return HashMap, mapping command keys to their enabled state
     */
    public HashMap<String, Boolean> getModuleConfig()
    {
        HashMap<String, Boolean> moduleConfig = new HashMap<>();

        for (ModuleAttributes ma : ModuleManager.getModules())
        {
            if (ModuleManager.getModule(ma.getKey()).getCategory() != NOGROUP)
            {
                moduleConfig.put(ma.getKey(), ((CheckBox) ccSetupScreen.lookup("#"+ma.getKey()+"CheckBox")).isSelected());
            }
            else
            {
                moduleConfig.put(ma.getKey(), true);
            }

        }
        return moduleConfig;
    }

    /**
     * Sets the root
     * @param root the root
     */
    public void setRoot(Parent root)
    {
        this.ccSetupScreen = (VBox) root;
    }

    /**
     * Gets the root
     * @return the root
     */
    public Parent getRoot()
    {
        return ccSetupScreen;
    }



}
