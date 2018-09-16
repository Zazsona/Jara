package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class Review extends Application
{
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/Review.fxml"));

            StackPane backButton = (StackPane) root.lookup("#backButton");
            Rectangle backRect = (Rectangle) root.lookup("#backRect");
            backButton.setOnMouseClicked((event) -> HeadedUtil.goBack(primaryStage));
            backButton.setOnMouseEntered((event) -> HeadedUtil.backButtonHover(backRect));
            backButton.setOnMouseExited((event) -> HeadedUtil.backButtonHover(backRect));

            StackPane nextButton = (StackPane) root.lookup("#nextButton");
            Rectangle nextRect = (Rectangle) root.lookup("#nextRect");
            nextButton.setOnMouseClicked((event) -> HeadedUtil.goNext(primaryStage));
            nextButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(nextRect));
            nextButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(nextRect));

            Text navBarDiscordSetup = (Text) root.lookup("#navBar_discord_text");
            navBarDiscordSetup.setOnMouseClicked((event) -> HeadedUtil.manageTitleSelection(navBarDiscordSetup, primaryStage));

            Text navBarWelcome = (Text) root.lookup("#navBar_welcome_text");
            navBarWelcome.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBarWelcome, primaryStage)));

            Text navBarCCSetup = (Text) root.lookup("#navBar_configuration_text");
            navBarCCSetup.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBarCCSetup, primaryStage)));

            StackPane inviteButton = (StackPane) root.lookup("#inviteButton");
            Rectangle inviteRect = (Rectangle) root.lookup("#inviteRect");
            inviteButton.setOnMouseClicked(null); //TODO
            inviteButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(inviteRect));
            inviteButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(inviteRect));

            primaryStage.getScene().setRoot(root);
            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //TODO: Minimise to tray once started
    }
}
