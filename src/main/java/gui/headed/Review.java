package gui.headed;

import configuration.SettingsUtil;
import gui.HeadedGUI;
import jara.CommandRegister;
import jara.Core;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import org.slf4j.LoggerFactory;

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
    @FXML
    public ImageView profileImage;
    @FXML
    public Text profileNameText;
    @FXML
    public Text profileDiscrimText;

    /**
     * Whether the invite button has been pressed.
     */
    private boolean invitePressed = false;

    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> HeadedGUIUtil.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> completeSetup());
        nextButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) -> HeadedGUIUtil.manageTitleSelection(navBar_discord_text));

        navBar_welcome_text.setOnMouseClicked((event -> HeadedGUIUtil.manageTitleSelection(navBar_welcome_text)));

        navBar_configuration_text.setOnMouseClicked((event -> HeadedGUIUtil.manageTitleSelection(navBar_configuration_text)));

        inviteButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(inviteRect));
        inviteButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(inviteRect));

    }
    /**
     * Displays this screen on the stage.
     * @param stage
     */
    public void show(Stage stage)
    {
        CommandConfigSetup ccSetup = HeadedGUIUtil.getCcSetupController();

        StringBuilder supportListBuilder = new StringBuilder();
        for (CommandRegister.Category id : ccSetup.getSupportedCategories())
        {
            supportListBuilder.append(CommandRegister.getCategoryName(id)).append("\n");
        }
        supportListLbl.setText(supportListBuilder.toString());

        inviteButton.setOnMouseClicked((event) ->
                                       {
                                           invitePressed = true;
                                           HeadedGUIUtil.openWebpage(generateInviteLink(true));
                                       });
        stage.getScene().setRoot(reviewScreen);
    }

    /**
     * Generates the URL to invite the bot. Required account details to be retrieved via DiscordSetup.generateAccountData()
     * @param showError Whether to show a loading error
     * @return the URL as String
     */
    private static String generateInviteLink(boolean showError)
    {
        DiscordSetup discordSetup = HeadedGUIUtil.getDiscordSetupController();
        if (discordSetup.getToken().equals(""))
        {
            HeadedGUI.showError("You'll need to complete setup before you can invite the bot.");
            return "";
        }
        else if (discordSetup.getClientID() != null)
        {
            String clientID = discordSetup.getClientID();
            Core.getShardManager().shutdown();
            return "https://discordapp.com/oauth2/authorize?client_id="+clientID+"&scope=bot&permissions=8";
        }
        else
        {
            if (showError)
            {
                HeadedGUI.showError("The bot is still loading. Please wait for a few seconds.");
            }
            return "";
        }

    }

    /**
     * @param root
     */
    public void setRoot(Parent root)
    {
        this.reviewScreen = (VBox) root;
    }

    /**
     * @return
     */
    public Parent getRoot()
    {
        return reviewScreen;
    }

    /**
     * Accumulates all the data the user has entered and saves it to a Global Settings file.<br>
     *     This will also open the bot invite page, if the user has not already.
     */
    private void completeSetup()
    {
        try
        {
            String token = HeadedGUIUtil.getDiscordSetupController().getToken();
            if (!HeadedGUIUtil.getDiscordSetupController().isValidToken())
            {
                HeadedGUI.showError("The token is blank or invalid.");
            }
            else
            {
                SettingsUtil.getGlobalSettings().setToken(token);
                SettingsUtil.getGlobalSettings().setCommandConfigMap(HeadedGUIUtil.getCcSetupController().getCommandConfig());
                if (!invitePressed) //Just so we don't have a useless bot running...
                {
                    while (generateInviteLink(false).equals(""));
                    {
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e)
                        {
                            LoggerFactory.getLogger(this.getClass()).error(e.toString());
                        }
                    }
                    HeadedGUIUtil.openWebpage(generateInviteLink(false));
                }
                HeadedGUIUtil.setSetupComplete(true);
                Platform.exit();
            }
        }
        catch (IOException e)
        {
            HeadedGUIUtil.showErrorPane("Could not save settings. Please restart the application.");
            Platform.exit();
            System.exit(0);
        }

    }

}
