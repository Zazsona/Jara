package gui.headed;

import configuration.SettingsUtil;
import jara.Core;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DiscordSetup extends ListenerAdapter
{
    /**
     * The last token with which we attempted to connect.
     */
    private String oldToken = "";
    /**
     * The ID of the client
     */
    private String clientID = "";
    /**
     * Whether or not the token held in the text box is valid
     */
    private boolean tokenIsValid = false;
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

    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> HeadedGUIUtil.goBack());
        backButton.setOnMouseEntered((event) -> HeadedGUIUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedGUIUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) ->
                                     {
                                         generateAccountData();
                                         HeadedGUIUtil.goNext();
                                     });
        nextButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(nextRect));

        navBar_welcome_text.setOnMouseClicked((event) ->
                                              {
                                                  generateAccountData();
                                                  HeadedGUIUtil.manageTitleSelection(navBar_welcome_text);
                                              });

        navBar_configuration_text.setOnMouseClicked((event) ->
                                                    {
                                                        generateAccountData();
                                                        HeadedGUIUtil.manageTitleSelection(navBar_configuration_text);
                                                    });

        navBar_review_text.setOnMouseClicked((event) ->
                                             {
                                                 generateAccountData();
                                                 HeadedGUIUtil.manageTitleSelection(navBar_review_text);
                                             });

        portalButton.setOnMouseClicked((event) -> HeadedGUIUtil.openWebpage("https://discordapp.com/developers/applications/"));
        portalButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(portalRect));
        portalButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(portalRect));

        if (SettingsUtil.getGlobalSettings().getToken() != null)
        {
            txtFieldToken.setText(SettingsUtil.getGlobalSettings().getToken());
        }


    }
    /**
     * Displays this screen on the stage.
     * @param stage
     */
    public void show(Stage stage)
    {
        stage.getScene().setRoot(discordSetupScreen);
    }

    /**
     * @return
     */
    public String getToken()
    {
        return txtFieldToken.getText();
    }

    /**
     * @return
     */
    public boolean isValidToken()
    {
        return tokenIsValid;
    }

    /**
     * @return
     */
    public String getClientID()
    {
        return clientID;
    }

    /**
     * @param root
     */
    public void setRoot(Parent root)
    {
        this.discordSetupScreen = (VBox) root;
    }

    /**
     * @return
     */
    public Parent getRoot()
    {
        return discordSetupScreen;
    }

    /**
     * Retrieves the account data for the bot's Discord account
     */
    private void generateAccountData()
    {
        if (!getToken().equals(oldToken)) //If the token has changed, retest it.
        {
            Task<Void> generateAccDataTask = new Task<Void>()
            {
                @Override
                protected Void call()
                {
                    oldToken = getToken();
                    tokenIsValid = Core.initialiseDiscordConnection(getToken());
                    Core.getShardManager().addEventListener(HeadedGUIUtil.getDiscordSetupController());
                    return null;
                }
            };
            new Thread(generateAccDataTask).start();
        }
    }
    @Override
    public void onReady(ReadyEvent re)
    {
        Platform.runLater(() ->
                          {

                              SelfUser selfUser = Core.getShardManager().getApplicationInfo().getJDA().getSelfUser();
                              clientID = selfUser.getId();
                              try //Attempt to fill the profile.
                              {
                                  HeadedGUIUtil.getReviewController().profileNameText.setText(selfUser.getName());
                                  HeadedGUIUtil.getReviewController().profileDiscrimText.setText("#"+selfUser.getDiscriminator());

                                  URLConnection connection = new URL(selfUser.getEffectiveAvatarUrl()).openConnection();
                                  connection.setRequestProperty("User-Agent", "Jara ("+System.getProperty("os.name")+")");
                                  InputStream stream = connection.getInputStream();

                                  HeadedGUIUtil.getReviewController().profileImage.setImage(new Image(stream));
                              }
                              catch (IOException e)
                              {
                                  //We already have a placeholder data for this case
                                  e.printStackTrace();
                              }

                          });
    }
}
