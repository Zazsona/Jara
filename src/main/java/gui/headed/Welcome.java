package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class Welcome extends Application
{
    private static Stage stage;

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
    private Text navBar_review_text;

    public void initialize()
    {
        backButton.setOnMouseClicked((event) -> HeadedUtil.goBack());
        backButton.setOnMouseEntered((event) -> HeadedUtil.backButtonHover(backRect));
        backButton.setOnMouseExited((event) -> HeadedUtil.backButtonHover(backRect));

        nextButton.setOnMouseClicked((event) -> HeadedUtil.goNext());
        nextButton.setOnMouseEntered((event) -> HeadedUtil.nextButtonHover(nextRect));
        nextButton.setOnMouseExited((event) -> HeadedUtil.nextButtonHover(nextRect));

        navBar_discord_text.setOnMouseClicked((event) -> HeadedUtil.manageTitleSelection(navBar_discord_text));

        navBar_configuration_text.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBar_configuration_text)));

        navBar_review_text.setOnMouseClicked((event -> HeadedUtil.manageTitleSelection(navBar_review_text)));
    }

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/welcome.fxml"));

            if (primaryStage.getScene() != null)
            {
                primaryStage.getScene().setRoot(root);
            }
            else
            {
                primaryStage.setTitle("Jara Setup");
                primaryStage.setScene(new Scene(root, 1280, 800));
                stage = primaryStage;
            }

            primaryStage.show();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Stage getStage()
    {
        return stage;
    }
}
