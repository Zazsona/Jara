package gui.headed;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

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
        backButton.setOnMouseClicked((event) -> HeadedGUIManager.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIManager.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIManager.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedGUIManager.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedGUIManager.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIManager.nextButtonHover(nextRect));

        navBar_welcome_text.setOnMouseClicked((event) -> HeadedGUIManager.manageTitleSelection(navBar_welcome_text));

        navBar_configuration_text.setOnMouseClicked((event) -> HeadedGUIManager.manageTitleSelection(navBar_configuration_text));

        navBar_review_text.setOnMouseClicked((event) -> HeadedGUIManager.manageTitleSelection(navBar_review_text));

        portalButton.setOnMouseClicked((event) -> HeadedGUIManager.openWebpage("https://discordapp.com/developers/applications/"));
        portalButton.setOnMouseEntered((event) -> HeadedGUIManager.nextButtonHover(portalRect));
        portalButton.setOnMouseExited((event) -> HeadedGUIManager.nextButtonHover(portalRect));

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
