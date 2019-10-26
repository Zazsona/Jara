package gui.headed;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ErrorPane extends Application
{
    @FXML
    private HBox root;
    @FXML
    private StackPane okButton;
    @FXML
    private Rectangle okRect;
    @FXML
    private Text msgTxt;

    /**
     * Initializes the UI, setting element functionality
     */
    public void initialize()
    {

        okButton.setOnMouseClicked((event) -> okButton.getScene().getWindow().hide());
        okButton.setOnMouseEntered((event) -> HeadedGUIUtil.nextButtonHover(okRect));
        okButton.setOnMouseExited((event) -> HeadedGUIUtil.nextButtonHover(okRect));

        msgTxt.setText(HeadedGUIUtil.getError());

        root.setOnKeyPressed((event) ->
                                      {
                                          if (event.getCode() == KeyCode.ENTER)
                                          {
                                              root.getScene().getWindow().hide();
                                          }
                                      });
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
            root.requestFocus();
        }
        catch (IOException e)
        {
            LoggerFactory.getLogger(this.getClass()).error(e.toString());
        }
    }
}
