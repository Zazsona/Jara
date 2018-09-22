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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ErrorPane extends Application
{
    @FXML
    private StackPane okButton;
    @FXML
    private Rectangle okRect;
    @FXML
    private Text msgTxt;

    public void initialize()
    {
        okButton.setOnMouseClicked((event) -> ((Stage) okButton.getScene().getWindow()).close());
        okButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(okRect));
        okButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(okRect));

        msgTxt.setText(HeadedGUIUtil.getError());
    }

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("gui/error.fxml"));
            Parent root = fxmlLoader.load();
            primaryStage = new Stage();
            primaryStage.setTitle("Error");
            primaryStage.setScene(new Scene(root, 500, 200));
            primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("jara.png")));
            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
