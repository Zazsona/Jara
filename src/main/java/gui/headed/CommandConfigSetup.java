package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class CommandConfigSetup extends Application
{
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/ccSetup.fxml"));

            StackPane backButton = (StackPane) root.lookup("#backButton");
            backButton.setOnMouseClicked((event) -> HeadedUtil.goBack(primaryStage));

            StackPane nextButton = (StackPane) root.lookup("#nextButton");
            nextButton.setOnMouseClicked((event) -> HeadedUtil.goNext(primaryStage));

            Text navBarDiscordSetup = (Text) root.lookup("#navBar_discord_text");
            navBarDiscordSetup.setOnMouseClicked((event) -> HeadedUtil.manageTitleSelection(navBarDiscordSetup, primaryStage));     //TODO: Avoid making so many event handlers(?) - Check the docs.

            Text navBarWelcome = (Text) root.lookup("#navBar_welcome_text");
            navBarWelcome.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBarWelcome, primaryStage)));

            Text navBarReview = (Text) root.lookup("#navBar_review_text");
            navBarReview.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBarReview, primaryStage)));

            primaryStage.getScene().setRoot(root);
            primaryStage.show();


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
