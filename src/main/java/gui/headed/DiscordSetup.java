package gui.headed;

import commands.CmdUtil;
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
import java.util.HashMap;
import java.util.Set;

public class DiscordSetup extends ListenerAdapter
{
    private String oldToken = "";
    private String clientID = "";
    boolean tokenIsValid = false;
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
    public void show(Stage stage)
    {
        stage.getScene().setRoot(discordSetupScreen);
    }

    public String getToken()
    {
        return txtFieldToken.getText();
    }
    public boolean isValidToken()
    {
        return tokenIsValid;
    }
    public String getClientID()
    {
        return clientID;
    }

    public void setRoot(Parent root)
    {
        this.discordSetupScreen = (VBox) root;
    }
    public Parent getRoot()
    {
        return discordSetupScreen;
    }
    private void generateAccountData()
    {
        if (!getToken().equals(oldToken)) //If the token has changed, retest it.
        {
            Task<Void> generateAccDataTask = new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
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
                              HeadedGUIUtil.getReviewController().profileNameText.setText(selfUser.getName());
                              HeadedGUIUtil.getReviewController().profileDiscrimText.setText("#"+selfUser.getDiscriminator());            //TODO Messy
                              try
                              {
                                  URLConnection connection = new URL(selfUser.getEffectiveAvatarUrl()).openConnection();
                                  connection.setRequestProperty("User-Agent", "Jara ("+System.getProperty("os.name")+")");
                                  InputStream stream = connection.getInputStream();

                                  HeadedGUIUtil.getReviewController().profileImage.setImage(new Image(stream));
                              }
                              catch (IOException e)
                              {
                                  e.printStackTrace();
                              }

                          });
    }
}
