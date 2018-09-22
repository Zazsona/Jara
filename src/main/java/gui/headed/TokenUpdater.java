package gui.headed;

import configuration.SettingsUtil;
import jara.CommandRegister;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

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
                                          HeadedGUIManager.setUpdatedToken(txtFieldToken.getText());
                                          Platform.exit();
                                      });
        enterButton.setOnMouseEntered((event) -> HeadedGUIManager.nextButtonHover(enterRect));
        enterButton.setOnMouseExited((event) -> HeadedGUIManager.nextButtonHover(enterRect));
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
