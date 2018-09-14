package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class DiscordSetup extends Application
{
    private String clientID;
    private String token;

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/discordSetup.fxml"));

            StackPane backButton = (StackPane) root.lookup("#backButton");
            backButton.setOnMouseClicked((event) ->
                                         {
                                             saveState(root);
                                             HeadedUtil.goBack(primaryStage);
                                         });

            StackPane nextButton = (StackPane) root.lookup("#nextButton");
            nextButton.setOnMouseClicked((event) ->
                                         {
                                             saveState(root);
                                             HeadedUtil.goNext(primaryStage);
                                         });

            Text navBarWelcomeSetup = (Text) root.lookup("#navBar_welcome_text");//TODO: Avoid making so many event handlers(?) - Check the docs.
            navBarWelcomeSetup.setOnMouseClicked((event) ->
                                                 {
                                                     saveState(root);
                                                     HeadedUtil.manageTitleSelection(navBarWelcomeSetup, primaryStage);
                                                 });

            Text navBarCCSetup = (Text) root.lookup("#navBar_configuration_text");
            navBarCCSetup.setOnMouseClicked((event) ->
                                                {
                                                    saveState(root);
                                                    HeadedUtil.manageTitleSelection(navBarCCSetup, primaryStage);
                                                });

            Text navBarReview = (Text) root.lookup("#navBar_review_text");
            navBarReview.setOnMouseClicked((event) ->
                                           {
                                               saveState(root);
                                               HeadedUtil.manageTitleSelection(navBarReview, primaryStage);
                                           });

            restoreState(root);

            primaryStage.getScene().setRoot(root);
            primaryStage.show();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    private void restoreState(Parent root)
    {
        TextField txtFieldToken = (TextField) root.lookup("#txtFieldToken");
        TextField txtFieldClientID = (TextField) root.lookup("#txtFieldClientID");

        if (token != null)
        {
            txtFieldToken.setText(token);
        }
        if (clientID != null)
        {
            txtFieldClientID.setText(clientID);
        }
    }
    private void saveState(Parent root)
    {
        TextField txtFieldToken = (TextField) root.lookup("#txtFieldToken");
        TextField txtFieldClientID = (TextField) root.lookup("#txtFieldClientID");

        token = txtFieldToken.getText();
        clientID = txtFieldClientID.getText();
    }
}
