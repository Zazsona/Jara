package gui.headed;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class HeadedGUIManager extends Application
{
    private static final Logger logger = LoggerFactory.getLogger(HeadedGUIManager.class);

    private static Welcome welcome;
    private static DiscordSetup discordSetup;
    private static CommandConfigSetup ccSetup;
    private static Review review;
    private static Stage stage;


    @Override
    public void start(Stage primaryStage) throws Exception
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(HeadedGUIManager.class.getClassLoader().getResource("gui/welcome.fxml"));
            Parent welcomeRoot = fxmlLoader.load();
            welcome = fxmlLoader.getController();
            welcome.setRoot(welcomeRoot);

            fxmlLoader = new FXMLLoader(HeadedGUIManager.class.getClassLoader().getResource("gui/discordSetup.fxml"));
            Parent discordRoot = fxmlLoader.load();
            discordSetup = fxmlLoader.getController();
            discordSetup.setRoot(discordRoot);

            fxmlLoader = new FXMLLoader(HeadedGUIManager.class.getClassLoader().getResource("gui/ccSetup.fxml"));
            Parent ccRoot = fxmlLoader.load();
            ccSetup = fxmlLoader.getController();
            ccSetup.setRoot(ccRoot);

            fxmlLoader = new FXMLLoader(HeadedGUIManager.class.getClassLoader().getResource("gui/review.fxml"));
            Parent reviewRoot = fxmlLoader.load();
            review = fxmlLoader.getController();
            review.setRoot(reviewRoot);

            stage = new Stage();
            stage.setTitle("Jara Setup");
            stage.setScene(new Scene(welcomeRoot, 1280, 800));
            stage.getIcons().add(new Image(HeadedGUIManager.class.getClassLoader().getResourceAsStream("jara.png")));
            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void manageTitleSelection(Text selection)
    {
        String selectionID = selection.getId();
        switch (selectionID)
        {
            case "navBar_welcome_text":
                welcome.show(stage);
                break;
            case "navBar_discord_text":
                discordSetup.show(stage);
                break;
            case "navBar_configuration_text":
                ccSetup.show(stage);
                break;
            case "navBar_review_text":
                review.show(stage);
                break;
            default:
                logger.info("Cannot load page. It does not exist.");
                break;
        }
    }

    public static void goBack()
    {
        String screenID = stage.getScene().getRoot().getId();

        switch (screenID)
        {
            case "welcomeScreen":
                logger.info("User has cancelled setup.");
                Platform.exit();
                break;
            case "discordSetupScreen":
                welcome.show(stage);
                //TODO for all: Save progress, Load new window and its state
                break;
            case "ccSetupScreen":
                discordSetup.show(stage);
                break;
            case "reviewScreen":
                ccSetup.show(stage);
                break;
            default:
                logger.error("Unknown setup window. Cannot go back.");
                break;
        }
    }
    public static void goNext()
    {
        String screenID = stage.getScene().getRoot().getId();

        switch (screenID)
        {
            case "welcomeScreen":
                discordSetup.show(stage);
                break;
            case "discordSetupScreen":
                ccSetup.show(stage);
                //TODO for all: Save progress, Load new window and its state
                break;
            case "ccSetupScreen":
                review.show(stage);
                break;
            case "reviewScreen":
                //TODO: Create config, launch bot
                break;
            default:
                logger.error("Unknown setup window. Cannot go to next.");
                break;
        }
    }

    public static void nextButtonHover(Rectangle rectangle)
    {
        if (rectangle.getFill().equals(Paint.valueOf("#7289da")))
        {
            rectangle.setFill(Paint.valueOf("#5c71b5"));
        }
        else
        {
            rectangle.setFill(Paint.valueOf("#7289da"));
        }
    }
    public static void backButtonHover(Rectangle rectangle)
    {
        if (rectangle.getFill().equals(Paint.valueOf("#99aab5")))
        {
            rectangle.setFill(Paint.valueOf("#808e96"));
        }
        else
        {
            rectangle.setFill(Paint.valueOf("#99aab5"));
        }
    }
    public static void openWebpage(String url)
    {
        if (url.equals(""))
        {
            return;
        }
        try
        {
            if (Desktop.isDesktopSupported())
            {
                try
                {
                    Desktop.getDesktop().browse(new URI(url));
                }
                catch (URISyntaxException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec("xdg-open " + url);
                if (process.exitValue() != 0)
                {
                    throw new IOException();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "ERROR: Unable to locate default browser. Please go to https://discordapp.com/developers/applications/ to set up your bot account.");
        }
    }
    public static DiscordSetup getDiscordSetupController()
    {
        return discordSetup;
    }
    public static CommandConfigSetup getCcSetupController()
    {
        return ccSetup;
    }
    public static Welcome getWelcomeController()
    {
        return welcome;
    }
    public static Review getReviewController()
    {
        return review;
    }

}