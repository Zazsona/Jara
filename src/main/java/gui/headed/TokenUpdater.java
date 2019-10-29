package gui.headed;

import jara.Core;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TokenUpdater extends Application
{
    @FXML
    private StackPane enterButton;
    @FXML
    private Rectangle enterRect;
    @FXML
    private TextField txtFieldToken;

    /**
     * Initializes the UI, setting element functionality
     */
    public void initialize()
    {
        enterButton.setOnMouseClicked((event) -> enterAction());
        enterButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(enterRect));
        enterButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(enterRect));

        txtFieldToken.setOnKeyPressed((event) ->
        {
            if (event.getCode() == KeyCode.ENTER)
            {
                enterAction();
            }
        });
    }

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("gui/updateToken.fxml"));
            Parent root = fxmlLoader.load();

            primaryStage = new Stage();
            primaryStage.setTitle("Invalid Token");
            primaryStage.setScene(new Scene(root, 500, 300));
            primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("jara.png")));
            primaryStage.show();
        }
        catch (IOException e)
        {
            LoggerFactory.getLogger(this.getClass()).error(e.toString());
        }
    }

    /**
     * Performs a check on the token to confirm it is valid. Closes the Window if so.
     */
    private void enterAction()
    {
        boolean tokenSuccess = Core.initialiseDiscordConnection(txtFieldToken.getText());
        if (!tokenSuccess)
        {
            HeadedGUI.showError("Token is invalid.");
        }
        else
        {
            HeadedGUIUtil.setUpdatedToken(txtFieldToken.getText());
            ((Stage) enterButton.getScene().getWindow()).close();
        }
    }
}
