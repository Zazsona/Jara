package jara;

import java.util.ArrayList;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter 
{
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
	public Message getNextMessage(Guild guild)
	{
		messagesToGet = 0;
		Runnable msgListener = () -> {guild.getJDA().addEventListener(this);};
		Thread msgListenerThread = new Thread(msgListener);
		msgListenerThread.start();
		try 
		{
			msgListenerThread.join();
			return messageLog.get(messageLog.size()-1)
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			return null; //Let the calling method handle this.
		}
		
	}
	public Message getNextMessage(TextChannel channel)
	{
		
	}
	public Message[] getNextMessages(Guild guild, int count)
	{
		
	}
	public Message[] getNextMessages(TextChannel channel, int count)
	{
		
	}
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
			//TODO: End thread
		}
	}
}
