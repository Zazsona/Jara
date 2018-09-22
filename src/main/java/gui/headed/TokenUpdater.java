package gui.headed;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class TokenUpdater extends Application
{
    @FXML
    private StackPane enterButton;
    @FXML
    private Rectangle enterRect;
    @FXML
    private TextField txtFieldToken;

    public void initialize()
    {
        enterButton.setOnMouseClicked((event) ->
                                      {
                                          HeadedGUIUtil.setUpdatedToken(txtFieldToken.getText());
                                          Platform.exit();
                                      });
        enterButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(enterRect));
        enterButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(enterRect));
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("gui/updateToken.fxml"));
        Parent root = fxmlLoader.load();

        primaryStage = new Stage();
        primaryStage.setTitle("Invalid Token");
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("jara.png")));
        primaryStage.show();

    }
}
