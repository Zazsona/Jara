package gui.headed;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DiscordSetup
{
    @FXML
    private VBox discordSetupScreen;
    @FXML
    private StackPane backButton;
    @FXML
    private Rectangle backRect;
    @FXML
    private StackPane nextButton;
    @FXML
    private Rectangle nextRect;
    @FXML
    private Text navBar_configuration_text;
    @FXML
    private Text navBar_welcome_text;
    @FXML
    private Text navBar_review_text;
    @FXML
    private StackPane portalButton;
    @FXML
    private Rectangle portalRect;
    @FXML
    private TextField txtFieldToken;
    @FXML
    private TextField txtFieldClientID;

    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> HeadedGUIUtil.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedGUIUtil.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(nextRect));

        navBar_welcome_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_welcome_text));

        navBar_configuration_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_configuration_text));

        navBar_review_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_review_text));

        portalButton.setOnMouseClicked((event) -> HeadedGUIUtil.openWebpage("https://discordapp.com/developers/applications/"));
        portalButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(portalRect));
        portalButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(portalRect));

    }
    public void show(Stage stage)
    {
        stage.getScene().setRoot(discordSetupScreen);
    }

    public String getClientID()
    {
        return txtFieldClientID.getText();
    }
    public String getToken()
    {
        return txtFieldToken.getText();
    }

    public void setRoot(Parent root)
    {
        this.discordSetupScreen = (VBox) root;
    }
    public Parent getRoot()
    {
        return discordSetupScreen;
    }
}
