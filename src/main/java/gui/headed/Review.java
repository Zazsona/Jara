package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class Review extends Application
{
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

    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> HeadedUtil.goBack());
        backButton.setOnMouseEntered((event) -> HeadedUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedUtil.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) -> HeadedUtil.manageTitleSelection(navBar_discord_text));

        navBar_welcome_text.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBar_welcome_text)));

        navBar_configuration_text.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBar_configuration_text)));

        inviteButton.setOnMouseClicked((event) -> HeadedUtil.openWebpage(HeadedUtil.generateInviteLink()));
        inviteButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(inviteRect));
        inviteButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(inviteRect));
    }

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/Review.fxml"));
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
