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
	 * 
	 * @param guild
	 * @return
	 * 
	 * This method returns the first message to be sent after its invocation
	 * within any channel of the guild that the bot has access to.
	 * 
	 * This function will block the thread until a message has been received.
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
					lock.wait(timeout);
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
	public Message[] getMessageHistory()
	{
		return (Message[]) messageLog.toArray();
	}
	public Message[] getMessageHistoryFromEnd(int count)
	{
		Message[] messages = new Message[count];
		for (int i = 0; i<count; i++)
		{
			messages[i] = messageLog.get(messageLog.size()-(i+1));
		}
		return messages;
	}
	public Message[] getMessageHistoryFromStart(int count)
	{
		Message[] messages = new Message[count];
		for (int i = 0; i<count; i++)
		{
			messages[i] = messageLog.get(i);
		}
		return messages;
	}
	public Message getLastMessage()
	{
		return messageLog.get(messageLog.size()-1);
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
