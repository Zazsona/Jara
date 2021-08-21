package com.zazsona.jara.gui.headed;
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

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class HeadedGUIUtil extends Application
{
    private static final Logger logger = LoggerFactory.getLogger(HeadedGUIUtil.class);

    private static Welcome welcome;
    private static DiscordSetup discordSetup;
    private static CommandConfigSetup ccSetup;
    private static Review review;
    private static Stage stage;

    private static String updatedToken;
    private static String errorMessage;

    private static boolean complete = false;

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(HeadedGUIUtil.class.getClassLoader().getResource("gui/welcome.fxml"));
            Parent welcomeRoot = fxmlLoader.load();
            welcome = fxmlLoader.getController();
            welcome.setRoot(welcomeRoot);

            fxmlLoader = new FXMLLoader(HeadedGUIUtil.class.getClassLoader().getResource("gui/discordSetup.fxml"));
            Parent discordRoot = fxmlLoader.load();
            discordSetup = fxmlLoader.getController();
            discordSetup.setRoot(discordRoot);

            fxmlLoader = new FXMLLoader(HeadedGUIUtil.class.getClassLoader().getResource("gui/ccSetup.fxml"));
            Parent ccRoot = fxmlLoader.load();
            ccSetup = fxmlLoader.getController();
            ccSetup.setRoot(ccRoot);

            fxmlLoader = new FXMLLoader(HeadedGUIUtil.class.getClassLoader().getResource("gui/review.fxml"));
            Parent reviewRoot = fxmlLoader.load();
            review = fxmlLoader.getController();
            review.setRoot(reviewRoot);

            stage = new Stage();
            stage.setTitle("Jara Setup");
            stage.setScene(new Scene(welcomeRoot, 1400, 800));
            stage.getIcons().add(new Image(HeadedGUIUtil.class.getClassLoader().getResourceAsStream("jara.png")));
            stage.show();
        }
        catch (IOException e)
        {
            logger.error(e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
        if (!isSetupComplete())
        {
            logger.info("User has cancelled setup. Aborting...");
            System.exit(0);
        }
    }

    /**
     * Loads the page based on the ID of the NavBar button.
     * @param selection the NavBar id.
     */
    protected static void manageTitleSelection(Text selection)
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

    /**
     * Returns to the previous page
     */
    protected static void goBack()
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

    /**
     * Proceeds to the next page
     */
    protected static void goNext()
    {
        String screenID = stage.getScene().getRoot().getId();

        switch (screenID)
        {
            case "welcomeScreen":
                discordSetup.show(stage);
                break;
            case "discordSetupScreen":
                ccSetup.show(stage);
                break;
            case "ccSetupScreen":
                review.show(stage);
                break;
            default:
                logger.error("Unknown setup window. Cannot go to next.");
                break;
        }
    }

    /**
     * Performs the hover over animation for the next button
     * @param rectangle the button
     */
    protected static void nextButtonHover(Rectangle rectangle)
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

    /**
     * Performs the hover over animation for the back button
     * @param rectangle the button
     */
    protected static void backButtonHover(Rectangle rectangle)
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

    /**
     * Opens a URL in the default browser
     * @param url the url
     */
    protected static void openWebpage(String url)
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
                    logger.error(e.toString());
                }
                catch (UnsupportedOperationException e)
                {
                    logger.info("OS was reported as compatible but is not.\nPlease go to https://discordapp.com/developers/applications/ to set up your bot account.");
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
            logger.error(e.toString());
            HeadedGUI.showError("ERROR: Unable to locate default browser. Please go to https://discordapp.com/developers/applications/ to set up your bot account.");
        }
    }

    /**
     * Shows the GUI to update the application token
     * @return the application token
     */
    protected static String showUpdateTokenPane()
    {
        Application.launch(TokenUpdater.class);
        return updatedToken;
    }

    /**
     * Shows the error window
     * @param error the error to display
     */
    protected static void showErrorPane(String error)
    {
        errorMessage = error;
        new ErrorPane().start(null);
    }

    /**
     * Gets the error to display on the error pane
     * @return the error message
     */
    protected static String getError()
    {
        return errorMessage;
    }

    /**
     * Gets the Discord Setup Controller
     * @return the controller
     */
    protected static DiscordSetup getDiscordSetupController()
    {
        return discordSetup;
    }
    /**
     * Gets the Command Configuration Controller
     * @return the controller
     */
    protected static CommandConfigSetup getCcSetupController()
    {
        return ccSetup;
    }

    /**
     * Gets the Welcome Controller
     * @return the controller
     */
    protected static Welcome getWelcomeController()
    {
        return welcome;
    }

    /**
     * Gets the Review Controller
     * @return the controller
     */
    protected static Review getReviewController()
    {
        return review;
    }

    /**
     * Sets the token set by the Update Token GUI
     * @param token the token, or null if none has been set this session
     */
    protected static void setUpdatedToken(String token)
    {
        updatedToken = token;
    }

    /**
     * Sets the setup state to complete
     * @param state the state of setup
     */
    protected static void setSetupComplete(boolean state)
    {
        complete = state;
    }

    /**
     * Checks if the setup is complete
     * @return the setup state
     */
    protected static boolean isSetupComplete()
    {
        return complete;
    }

}
