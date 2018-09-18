package gui.headed;

import jara.CommandRegister;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class Review
{
    @FXML
    private VBox reviewScreen;
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
    private Text navBar_welcome_text;
    @FXML
    private StackPane inviteButton;
    @FXML
    private Rectangle inviteRect;
    @FXML
    private Label supportListLbl;

    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> HeadedGUIManager.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIManager.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIManager.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedGUIManager.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedGUIManager.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIManager.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) -> HeadedGUIManager.manageTitleSelection(navBar_discord_text));

        navBar_welcome_text.setOnMouseClicked((event -> HeadedGUIManager.manageTitleSelection(navBar_welcome_text)));

        navBar_configuration_text.setOnMouseClicked((event -> HeadedGUIManager.manageTitleSelection(navBar_configuration_text)));

        inviteButton.setOnMouseClicked((event) -> HeadedGUIManager.openWebpage(HeadedGUIManager.generateInviteLink()));
        inviteButton.setOnMouseEntered((event) -> HeadedGUIManager.nextButtonHover(inviteRect));
        inviteButton.setOnMouseExited((event) -> HeadedGUIManager.nextButtonHover(inviteRect));



    }
    public void refresh()
    {

        CommandConfigSetup ccSetup = HeadedGUIManager.getCcSetupController();

        StringBuilder supportListBuilder = new StringBuilder();
        for (Integer id : ccSetup.getSupportedCategories())
        {
            supportListBuilder.append(CommandRegister.getCategoryName(id)).append("\n");
        }
        supportListLbl.setText(supportListBuilder.toString());
    }
    public void setRoot(Parent root)
    {
        this.reviewScreen = (VBox) root;
    }
    public Parent getRoot()
    {
        return reviewScreen;
    }


}
