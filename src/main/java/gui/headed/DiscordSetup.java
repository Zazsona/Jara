package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.w3c.dom.css.Rect;

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
            Rectangle backRect = (Rectangle) root.lookup("#backRect");
            backButton.setOnMouseClicked((event) ->
                                         {
                                             saveState(root);
                                             HeadedUtil.goBack(primaryStage);
                                         });
            backButton.setOnMouseEntered((event) -> HeadedUtil.backButtonHover(backRect));
            backButton.setOnMouseExited((event) -> HeadedUtil.backButtonHover(backRect));

            StackPane nextButton = (StackPane) root.lookup("#nextButton");
            Rectangle nextRect = (Rectangle) root.lookup("#nextRect");
            nextButton.setOnMouseClicked((event) ->
                                         {
                                             saveState(root);
                                             HeadedUtil.goNext(primaryStage);
                                         });
            nextButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(nextRect));
            nextButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(nextRect));

            Text navBarWelcomeSetup = (Text) root.lookup("#navBar_welcome_text");
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

            StackPane portalButton = (StackPane) root.lookup("#portalButton");
            Rectangle portalRect = (Rectangle) root.lookup("#portalRect");
            portalButton.setOnMouseClicked(null); //TODO
            portalButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(portalRect));
            portalButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(portalRect));


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
