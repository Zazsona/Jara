package jara;

import java.util.ArrayList;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter 
{
	private Object lock = new Object();
	private ArrayList<Message> messageLog = new ArrayList<Message>();
	private int messagesToGet = 0;
	/**
	 * 	 * This method returns the first message to be sent after its invocation
	 * within any channel of the guild that the bot has access to.
	 * 
	 * This function will block the thread until a message has been received.
	 * 
	 * @param guild
	 * @return
	 * Message - The first message to be sent after the method is called
	 * null - If the thread is interrupted before a message is received, or a timeout occurs.
	 */
	public Message getNextMessage(Guild guild, int timeout)
	{
		messagesToGet = 0;
		guild.getJDA().addEventListener(this);
		try 
		{
			int messageLogSize = messageLog.size();
			synchronized (lock)
			{
				while (messageLogSize == messageLog.size()) //If no new entries have been added, continue the wait, as the thread wasn't meant to notify.
				{
					lock.wait(timeout); //TODO: Deal with timeout
				}
			}
			guild.getJDA().removeEventListener(this);
			return messageLog.get(messageLog.size()-1);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			guild.getJDA().removeEventListener(this);
			return null; //Let the calling method handle this.
		}
		
	}
	/*public Message getNextMessage(TextChannel channel, int timeout)
	{
		TODO: Uncomment this and below methods
	}
	public Message getNextMessage(TextChannel channel)
	{
		//return getNextMessage(channel, 0);
	}
	public Message getNextMessage(Guild guild)
	{
		return getNextMessage(guild, 0);
	}
	public Message[] getNextMessages(Guild guild, int count)
	{
		
	}
	public Message[] getNextMessages(TextChannel channel, int count)
	{
		
	}*/
	/**
	 * Takes all messages received from this instance and returns them as an array.
	 * 
	 * @return
	 * Message[] - Full history of all messages from this instance
	 */
	public Message[] getMessageHistory()
	{
		return (Message[]) messageLog.toArray();
	}
	/**
	 * 
	 * @param count
	 * @return
	 * 
	 * This method retrieves all messages received by this MessageListener instance. 
	 * History is gathered starting from most recent and going back by the specified number.
	 */
	public Message[] getMessageHistoryFromEnd(int count)
	{
		Message[] messages = new Message[count];
		for (int i = 0; i<count; i++)
		{
			messages[i] = messageLog.get(messageLog.size()-(i+1));
		}
		return messages;
	}
	/**
	 * 
	 * @param count
	 * @return
	 * 
	 * This method retrieves all messages received by this MessageListener instance. 
	 * History is gathered starting from the first message and going forward by the specified number.
	 */
	public Message[] getMessageHistoryFromStart(int count)
	{
		Message[] messages = new Message[count];
		for (int i = 0; i<count; i++)
		{
			messages[i] = messageLog.get(i);
		}
		return messages;
	}
	/**
	 * Returns the last received message. 
	 * The output here will match that of the last getNextMessage.
	 * 
	 * @return
	 * Message - The last received message
	 * null - No messages have been received.
	 */
	public Message getLastMessage()
	{
		if (messageLog.size() > 0)
		{
			return messageLog.get(messageLog.size()-1);
		}
		else
		{
			return null;
		}

	} 
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent msgEvent)
	{
		messageLog.add(msgEvent.getMessage());
		messagesToGet--;
		if (messagesToGet <= 0)
		{
			synchronized (lock)
			{
				lock.notifyAll();
			}
		}
	}
}
