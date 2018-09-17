package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class DiscordSetup extends Application
{
    private String clientID;
    private String token;

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
        backButton.setOnMouseClicked((event) ->
                                     {
                                         saveState();
                                         HeadedUtil.goBack();
                                     });
        backButton.setOnMouseEntered((event) -> HeadedUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) ->
                                     {
                                         saveState();
                                         HeadedUtil.goNext();
                                     });
        nextButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(nextRect));

        navBar_welcome_text.setOnMouseClicked((event) ->
                                             {
                                                 saveState();
                                                 HeadedUtil.manageTitleSelection(navBar_welcome_text);
                                             });

        navBar_configuration_text.setOnMouseClicked((event) ->
                                        {
                                            saveState();
                                            HeadedUtil.manageTitleSelection(navBar_configuration_text);
                                        });

        navBar_review_text.setOnMouseClicked((event) ->
                                       {
                                           saveState();
                                           HeadedUtil.manageTitleSelection(navBar_review_text);
                                       });

        portalButton.setOnMouseClicked((event) -> HeadedUtil.openWebpage("https://discordapp.com/developers/applications/"));
        portalButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(portalRect));
        portalButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(portalRect));


    }
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/discordSetup.fxml"));


            restoreState();

            primaryStage.getScene().setRoot(root);
            primaryStage.show();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    private void restoreState()
    {
        if (token != null)
        {
            txtFieldToken.setText(token);
        }
        if (clientID != null)
        {
            txtFieldClientID.setText(clientID);
        }
    }
    private void saveState()
    {
        token = txtFieldToken.getText();
        clientID = txtFieldClientID.getText();
    }
    public String getClientID()
    {
        if (clientID == null)
        {
            return "";
        }
        return clientID;
    }
    public String getToken()
    {
        if (token == null)
        {
            return "";
        }
        return token;
    }
}
