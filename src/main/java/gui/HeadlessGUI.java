package gui;

//import configuration.GlobalSettingsManager;
//import configuration.JsonFormats;
//import jara.CommandRegister;

//import java.util.ArrayList;
//import java.util.Scanner;

public class HeadlessGUI
{
//    private static void printLogo()
//    {
//        System.out.print("\n" +
//                               "=============================================================================\n" +
//                               "                                                                             \n" +
//                               "                                                                             \n" +
//                               "          JJJJJJJJJJJ                                                        \n" +
//                               "          J:::::::::J                                                        \n" +
//                               "          J:::::::::J                                                        \n" +
//                               "          JJ:::::::JJ                                                        \n" +
//                               "            J:::::J    aaaaaaaaaaaaa   rrrrr   rrrrrrrrr     aaaaaaaaaaaaa   \n" +
//                               "            J:::::J    a::::::::::::a  r::::rrr:::::::::r    a::::::::::::a  \n" +
//                               "            J:::::J    aaaaaaaaa:::::a r:::::::::::::::::r   aaaaaaaaa:::::a \n" +
//                               "            J:::::j             a::::a rr::::::rrrrr::::::r           a::::a \n" +
//                               "            J:::::J      aaaaaaa:::::a  r:::::r     r:::::r    aaaaaaa:::::a \n" +
//                               "JJJJJJJ     J:::::J    aa::::::::::::a  r:::::r     rrrrrrr  aa::::::::::::a \n" +
//                               "J:::::J     J:::::J   a::::aaaa::::::a  r:::::r             a::::aaaa::::::a \n" +
//                               "J::::::J   J::::::J  a::::a    a:::::a  r:::::r            a::::a    a:::::a \n" +
//                               "J:::::::JJJ:::::::J  a::::a    a:::::a  r:::::r            a::::a    a:::::a \n" +
//                               " JJ:::::::::::::JJ   a:::::aaaa::::::a  r:::::r            a:::::aaaa::::::a \n" +
//                               "   JJ:::::::::JJ      a::::::::::aa:::a r:::::r             a::::::::::aa:::a\n" +
//                               "     JJJJJJJJJ         aaaaaaaaaa  aaaa rrrrrrr              aaaaaaaaaa  aaaa\n" +
//                               "                                                                             \n" +
//                               "                                                                             \n");
//    }
//    public static void firstTimeSetupWizard()
//    {
//        printLogo();
//        String input;
//        //JsonFormats.GlobalSettingsJson config = GlobalSettingsManager.getGlobalSettings();
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Hey there, and welcome to your new Discord bot.");
//        System.out.println("First things first, have you set up a bot account in Discord? (Y/N)");
//        input = scanner.nextLine();
//        if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no"))
//        {
//            System.out.println("Alright then. Here's how to create a bot account. You'll need this to run the bot.\n" +
//                                       "1. Go to https://discordapp.com/developers/applications/ and log in.\n" +
//                                       "2. Select \"New Application\"\n" +
//                                       "3. Select the \"Bot\" tab, and create a bot user.\n" +
//                                       "4. Set your bot's name and profile picture.\n" +
//                                       "5. Reveal the client token, and paste it here:");
//        }
//        else
//        {
//            System.out.println("Dandy! Enter the client token below:");
//        }
//        GlobalSettingsManager.setClientToken(scanner.nextLine());
//        scanner.close();
//        configureCommands();
//        return;
//
//    }
//    public static void manageNewCommands()
//    {
//        printLogo();
//        System.out.println("Update complete! Some new commands have been added. Opening configuration...");
//        configureCommands();
//    }
//    private static void configureCommands()
//    {
//        Scanner scanner = new Scanner(System.in);
//        String input;
//        printGlobalConfig(GlobalSettingsManager.getGlobalCommandConfig()); //The command list is printed beforehand so that instructions are always at the end, ensuring they are seen
//        System.out.println("\nCommand configuration.\n" +
//                                   "Each guild can enable and disable commands as they wish, however any commands disabled here cannot be seen at all.\n\n" +
//                                   "Instructions:\n" +
//                                   "enable [Command]\n" +
//                                   "disable [Command]\n" +
//                                   "Q (Save & Quit)\n" +
//                                   "You can also use a category to manage a subset of commands (games, utilities, etc.) or \"*\" to manage all commands.");
//        while (true)
//        {
//            input = scanner.nextLine();
//            String[] query = input.split(" ");
//            if (query[0].equalsIgnoreCase("q"))
//            {
//                GlobalSettingsManager.saveGlobalSettings();
//                System.out.println("Alright then, you're all set. Starting the bot...");
//                return;
//            }
//            if (query.length == 1)
//            {
//                if (query[0].equalsIgnoreCase("enable") || query[0].equalsIgnoreCase("disable"))
//                {
//                    System.out.println("Seems like you're missing a parameter, such as a command name or category. For example: enable Ping");
//                }
//                else
//                {
//                    System.out.println("Unrecognised command.");
//                }
//            }
//            else if (query.length == 2)
//            {
//                if (query[0].equalsIgnoreCase("enable"))
//                {
//                    if (CommandRegister.getCategoryNames().contains(query[1]))
//                    {
//                        GlobalSettingsManager.setCategoryEnabledGlobally(query[1], true);
//                    }
//                    else
//                    {
//                        GlobalSettingsManager.setCommandEnabledGlobally(query[1], true);
//                    }
//                    printGlobalConfig(GlobalSettingsManager.getGlobalCommandConfig());
//
//                }
//                else if (query[0].equalsIgnoreCase("disable"))
//                {
//                    if (CommandRegister.getCategoryNames().contains(query[1]))
//                    {
//                        GlobalSettingsManager.setCategoryEnabledGlobally(query[1], false);
//                    }
//                    else
//                    {
//                        GlobalSettingsManager.setCommandEnabledGlobally(query[1], false);
//                    }
//                    printGlobalConfig(GlobalSettingsManager.getGlobalCommandConfig());
//                }
//                else
//                {
//                    System.out.println("Unrecognised command.");
//                }
//            }
//            else
//            {
//                System.out.println("Invalid parameters. Please check the instructions for help.");
//            }
//
//        }
//    }
//
//    private static void printGlobalConfig(JsonFormats.GlobalCommandConfigJson[] moduleConfig)
//    {
//        ArrayList<String> games = new ArrayList<String>();
//        ArrayList<String> toys = new ArrayList<String>();
//        ArrayList<String> utility = new ArrayList<String>();
//        ArrayList<String> audio = new ArrayList<String>();
//        ArrayList<String> admin = new ArrayList<String>();
//        for (JsonFormats.GlobalCommandConfigJson aCommandConfig : moduleConfig)
//        {
//            if (CommandRegister.getModule(aCommandConfig.getKey()).isDisableable())
//            {
//                int category = CommandRegister.getModule(aCommandConfig.getKey()).getCategoryID();
//                switch (category)
//                {
//                    case CommandRegister.NOGROUP:
//                    {
//                        //Ignore
//                        break;
//                    }
//                    case CommandRegister.GAMES:
//                    {
//                        games.add(aCommandConfig.getKey() + " :: " + aCommandConfig.isEnabled());
//                        break;
//                    }
//                    case CommandRegister.TOYS:
//                    {
//                        toys.add(aCommandConfig.getKey() + " :: " + aCommandConfig.isEnabled());
//                        break;
//                    }
//                    case CommandRegister.UTILITY:
//                    {
//                        utility.add(aCommandConfig.getKey() + " :: " + aCommandConfig.isEnabled());
//                        break;
//                    }
//                    case CommandRegister.AUDIO:
//                    {
//                        audio.add(aCommandConfig.getKey() + " :: " + aCommandConfig.isEnabled());
//                        break;
//                    }
//                    case CommandRegister.ADMIN:
//                    {
//                        admin.add(aCommandConfig.getKey() + " :: " + aCommandConfig.isEnabled());
//                        break;
//                    }
//                }
//            }
//        }
//        System.out.println("====== Games ======");
//        for (String details : games)
//        {
//            System.out.println(details);
//        }
//        System.out.println("===== Toys =====");
//        for (String details : toys)
//        {
//            System.out.println(details);
//        }
//        System.out.println("===== Utilities =====");
//        for (String details : utility)
//        {
//            System.out.println(details);
//        }
//        System.out.println("===== Audio =====");
//        for (String details : audio)
//        {
//            System.out.println(details);
//        }
//        System.out.println("===== Admin =====");
//        for (String details : admin)
//        {
//            System.out.println(details);
//        }
//
//    }
//    public static String updateToken()
//    {
//        System.out.println("Please enter a new token:");
//        Scanner scanner = new Scanner(System.in);
//        String token = scanner.nextLine();
//        GlobalSettingsManager.setClientToken(token);
//        return token;
//    }
}
