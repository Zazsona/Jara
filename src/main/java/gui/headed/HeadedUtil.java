package gui.headed;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class HeadedUtil
{
    private static final Logger logger = LoggerFactory.getLogger(HeadedUtil.class);

    private static Welcome welcome = new Welcome();
    private static DiscordSetup discordSetup = new DiscordSetup();
    private static CommandConfigSetup ccSetup = new CommandConfigSetup();
    private static Review review = new Review();

    public static void manageTitleSelection(Text selection, Stage stage)
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

    public static void goBack(Stage stage)
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
    public static void goNext(Stage stage)
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
}
