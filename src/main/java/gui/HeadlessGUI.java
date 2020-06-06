package gui;

import configuration.GlobalSettings;
import configuration.SettingsUtil;

import java.io.IOException;
import java.util.Scanner;

public class HeadlessGUI
{
    private static Scanner scanner = new Scanner(System.in);

    private static void printLogo()
    {
        System.out.print("\n" +
                               "=============================================================================\n" +
                               "                                                                             \n" +
                               "                                                                             \n" +
                               "          JJJJJJJJJJJ                                                        \n" +
                               "          J:::::::::J                                                        \n" +
                               "          J:::::::::J                                                        \n" +
                               "          JJ:::::::JJ                                                        \n" +
                               "            J:::::J    aaaaaaaaaaaaa   rrrrr   rrrrrrrrr     aaaaaaaaaaaaa   \n" +
                               "            J:::::J    a::::::::::::a  r::::rrr:::::::::r    a::::::::::::a  \n" +
                               "            J:::::J    aaaaaaaaa:::::a r:::::::::::::::::r   aaaaaaaaa:::::a \n" +
                               "            J:::::j             a::::a rr::::::rrrrr::::::r           a::::a \n" +
                               "            J:::::J      aaaaaaa:::::a  r:::::r     r:::::r    aaaaaaa:::::a \n" +
                               "JJJJJJJ     J:::::J    aa::::::::::::a  r:::::r     rrrrrrr  aa::::::::::::a \n" +
                               "J:::::J     J:::::J   a::::aaaa::::::a  r:::::r             a::::aaaa::::::a \n" +
                               "J::::::J   J::::::J  a::::a    a:::::a  r:::::r            a::::a    a:::::a \n" +
                               "J:::::::JJJ:::::::J  a::::a    a:::::a  r:::::r            a::::a    a:::::a \n" +
                               " JJ:::::::::::::JJ   a:::::aaaa::::::a  r:::::r            a:::::aaaa::::::a \n" +
                               "   JJ:::::::::JJ      a::::::::::aa:::a r:::::r             a::::::::::aa:::a\n" +
                               "     JJJJJJJJJ         aaaaaaaaaa  aaaa rrrrrrr              aaaaaaaaaa  aaaa\n" +
                               "                                                                             \n" +
                               "                                                                             \n");
    }

    public static void runFirstTimeSetup() throws IOException
    {
        printLogo();
        String input;
        System.out.println("Hey there, and welcome to your new Discord bot.");
        System.out.println("First things first, have you set up a bot account in Discord? (Y/N)");
        input = scanner.nextLine();
        if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no"))
        {
            System.out.println("Alright then. Here's how to create a bot account. You'll need this to run the bot.\n" +
                                       "1. Go to https://discordapp.com/developers/applications/ and log in.\n" +
                                       "2. Select \"New Application\"\n" +
                                       "3. Select the \"Bot\" tab, and create a bot user.\n" +
                                       "4. Set your bot's name and profile picture.\n" +
                                       "5. Reveal the client token.\n");
        }
        String token = requestToken();
        if (token.length() > 0)
        {
            SettingsUtil.getGlobalSettings().setToken(token);
            //SettingsUtil.getGlobalSettings().setAll(true);
        }
        else
            System.out.println("Invalid token.");

    }

    public static String requestToken() throws IOException
    {
        System.out.println("Enter the client token below:");
        String token = scanner.nextLine();
        return token;
    }
}
