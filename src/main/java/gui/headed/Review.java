package gui.headed;

import jara.CommandRegister;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;

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
        backButton.setOnMouseClicked((event) -> HeadedGUIUtil.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedGUIUtil.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_discord_text));

        navBar_welcome_text.setOnMouseClicked((event -> HeadedGUIUtil.manageTitleSelection(navBar_welcome_text)));

        navBar_configuration_text.setOnMouseClicked((event -> HeadedGUIUtil.manageTitleSelection(navBar_configuration_text)));

        inviteButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(inviteRect));
        inviteButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(inviteRect));



    }
    public void show(Stage stage)
    {
        CommandConfigSetup ccSetup = HeadedGUIUtil.getCcSetupController();

        StringBuilder supportListBuilder = new StringBuilder();
        for (Integer id : ccSetup.getSupportedCategories())
        {
            supportListBuilder.append(CommandRegister.getCategoryName(id)).append("\n");
        }
        supportListLbl.setText(supportListBuilder.toString());

        inviteButton.setOnMouseClicked((event) -> HeadedGUIUtil.openWebpage(generateInviteLink()));

        stage.getScene().setRoot(reviewScreen);
    }
    public static String generateInviteLink()
    {
        DiscordSetup discordSetup = HeadedGUIUtil.getDiscordSetupController();
        if (discordSetup.getClientID().equals(""))
        {
            JOptionPane.showMessageDialog(null, "You'll need to complete setup before you can invite the bot.");
            return "";
        }
        return "https://discordapp.com/oauth2/authorize?client_id="+discordSetup.getClientID()+"&scope=bot&permissions=8";
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
