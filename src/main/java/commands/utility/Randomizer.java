package commands.utility;

import commands.Command;
import jara.CommandRegister;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Random;

/**
 * A randomizer to randomly select an element from a list, or a value from a range of numbers.
 */
public class Randomizer extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        if (parameters.length > 1)
        {
            if (!runNumberRandomisation(msgEvent, parameters))
            {
                runListRandomisation(msgEvent, parameters);
            }
        }
        else
        {
            CommandRegister.sendHelpInfo(msgEvent, getClass());
        }
    }

    /**
     * Parses the user's request and calls for the result to be displayed.
     * @param msgEvent the context
     * @param parameters the user's request
     * @return boolean of success
     */
    private boolean runNumberRandomisation(GuildMessageReceivedEvent msgEvent, String[] parameters)
    {
        int lowerBound = 0;
        int upperBound = 1;

        if (parameters.length == 2)
        {
            String[] bounds = parameters[parameters.length-1].split("[-]");
            if (bounds.length > 1 && bounds[0].trim().matches("[0-9]*") && bounds[1].trim().matches("[0-9]*"))
            {
                lowerBound = Integer.parseInt(bounds[0]);
                upperBound = Integer.parseInt(bounds[1]);
                if (lowerBound > upperBound)
                {
                    upperBound = lowerBound;
                    lowerBound = Integer.parseInt(bounds[1]);
                }
            }
        }
        else if (parameters.length == 3)
        {
            if (parameters[1].matches("[0-9]*") && parameters[2].matches("[0-9]*"))
            {
                lowerBound = Integer.parseInt(parameters[1]);
                upperBound = Integer.parseInt(parameters[2]);
                if (lowerBound > upperBound)
                {
                    upperBound = lowerBound;
                    lowerBound = Integer.parseInt(parameters[2]);
                }
            }
        }
        else
        {
            return false;
        }
        displayNumber(msgEvent, lowerBound, upperBound);
        return true;
    }

    /**
     * Displays the number with a dramatic animation.<br>
     *     Note: The animation is currently disabled due to lag & API usage concerns.
     * @param msgEvent the context
     * @param lowerBound the lower value to randomise (inclusive)
     * @param upperBound the upper value to randomise (inclusive)
     */
    private void displayNumber(GuildMessageReceivedEvent msgEvent, int lowerBound, int upperBound)
    {
        Random r = new Random();
        Message randMsg = msgEvent.getChannel().sendMessage(""+(r.nextInt(upperBound+1-lowerBound)+lowerBound)).complete();

        /*try
        {
            for (int interval = 100; interval<400; interval=interval+100)
            {
                randMsg.editMessage(""+(r.nextInt(upperBound+1-lowerBound)+lowerBound)).queue();
                Thread.sleep(interval);
            }

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            randMsg.editMessage(""+(r.nextInt(upperBound+1-lowerBound)+lowerBound)).queue();
        }*/
    }

    /**
     * Parses the user's request and calls for the result to be displayed.
     * @param msgEvent the context
     * @param parameters the list of elements the user wants to select from (Separated by spaces or commas)
     */
    private void runListRandomisation(GuildMessageReceivedEvent msgEvent, String[] parameters)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ,");
        for (int i = 1; i<parameters.length; i++)
        {
            stringBuilder.append(parameters[i]).append(" ");
        }

        String[] list = stringBuilder.toString().split("[,]");
        if (list.length > 2)
        {
            displayListResult(msgEvent, list);
        }
        else
        {
            displayListResult(msgEvent, parameters);
        }
    }

    /**
     * Displays the number with a dramatic animation.<br>
     *     Note: The animation is currently disabled due to lag & API usage concerns.
     * @param msgEvent the context
     * @param list the list
     */
    private void displayListResult(GuildMessageReceivedEvent msgEvent, String[] list)
    {
        Random r = new Random();
        Message randMsg = msgEvent.getChannel().sendMessage(list[r.nextInt(list.length-1)+1]).complete(); //-1/+1 is being used here as this ignores 0 (the command trigger) without hitting the bounds of the parameters array.

        /*try
        {
            for (int interval = 100; interval<400; interval=interval+100)
            {
                randMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
                Thread.sleep(interval);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            randMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
        }*/
    }
}
