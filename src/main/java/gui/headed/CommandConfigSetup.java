package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class CommandConfigSetup extends Application
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
    private Text navBar_welcome_text;
    @FXML
    private Text navBar_review_text;
    @FXML
    private VBox adminList;

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

        navBar_review_text.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBar_review_text)));

    }
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/ccSetup.fxml"));
            primaryStage.getScene().setRoot(root);
            primaryStage.show();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
