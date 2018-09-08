package gui.windowed;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Welcome extends Application
{

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            primaryStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("gui/welcome.fxml"));
            primaryStage.setTitle("Jara Setup");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
