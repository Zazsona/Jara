package gui.headed;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class HeadedUtil
{
    private static final Logger logger = LoggerFactory.getLogger(HeadedUtil.class);

    private static Welcome welcome = new Welcome();
    private static DiscordSetup discordSetup = new DiscordSetup();
    private static CommandConfigSetup ccSetup = new CommandConfigSetup();
    private static Review review = new Review();
    private static Stage stage = welcome.getStage();

    public static void manageTitleSelection(Text selection)
    {
        String selectionID = selection.getId();
        if (selectionID.equals("navBar_welcome_text"))
        {
            welcome.start(stage);
        }
        else if (selectionID.equals("navBar_discord_text"))
        {
            discordSetup.start(stage);
        }
        else if (selectionID.equals("navBar_configuration_text"))
        {
            ccSetup.start(stage);
        }
        else if (selectionID.equals("navBar_review_text"))
        {
            review.start(stage);
        }
        else
        {
            logger.info("Cannot load page. It does not exist.");
        }
    }

    public static void goBack()
    {
        String screenID = stage.getScene().getRoot().getId();

        if (screenID.equals("welcomeScreen"))
        {
            logger.info("User has cancelled setup.");
            Platform.exit();
        }
        else if (screenID.equals("discordSetupScreen"))
        {
            welcome.start(stage);
            //TODO for all: Save progress, Load new window and its state
        }
        else if (screenID.equals("ccSetupScreen"))
        {
            discordSetup.start(stage);
        }
        else if (screenID.equals("reviewScreen"))
        {
            ccSetup.start(stage);
        }
        else
        {
            logger.error("Unknown setup window. Cannot go back.");
        }
    }
    public static void goNext()
    {
        String screenID = stage.getScene().getRoot().getId();

        if (screenID.equals("welcomeScreen"))
        {
            discordSetup.start(stage);
        }
        else if (screenID.equals("discordSetupScreen"))
        {
            ccSetup.start(stage);
            //TODO for all: Save progress, Load new window and its state
        }
        else if (screenID.equals("ccSetupScreen"))
        {
            review.start(stage);
        }
        else if (screenID.equals("reviewScreen"))
        {
            //TODO: Create config, launch bot
        }
        else
        {
            logger.error("Unknown setup window. Cannot go to next.");
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
    public static String generateInviteLink()
    {
        if (discordSetup.getClientID().equals(""))
        {
            JOptionPane.showMessageDialog(null, "You'll need to complete setup before you can invite the bot.");
            return "";
        }
        return "https://discordapp.com/oauth2/authorize?client_id="+discordSetup.getClientID()+"&scope=bot&permissions=8";
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
}
