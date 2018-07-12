package jara;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageManager
{
	private Object lock = new Object();
	private ArrayList<Message> messageLog;
	private int messagesToGet = 0;
	private Guild guildToListen;
	private TextChannel channelToListen;
	MessageListener messageListener;
	public MessageManager()
	{
		messageLog = new ArrayList<Message>();
		messageListener = new MessageListener();
	}
	/**
	 * This method returns the first message to be sent after its invocation<br>
	 * within any channel of the guild that the bot has access to.<br>
	 * <br>
	 * This function will block the thread until the message count has been received, or the timeout elapses.<br>
	 * <br>
	 * @param guild
	 * @param timeout
	 * @param messageCount
	 * @return
	 * Message[] - The messages sent after the method was called, size matches messageCount
	 * null - If the thread is interrupted before a message is received, or a timeout occurs.
	 */
	private Message[] futureGuildMessageCollector(Guild guild, int timeout, int messageCount)
	{
		guildToListen = guild;
		messagesToGet = messageCount;
		guild.getJDA().addEventListener(messageListener);
		try 
		{
			int messageLogSize = messageLog.size();
			synchronized (lock)
			{
				while (messageLog.size() != (messageLogSize + messageCount)) //If no new entries have been added, continue the wait, as the thread wasn't meant to notify.
				{
			        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
			        long startTime = runtimeBean.getUptime(); //Getting the time to check for spurious wake-up or timeout later.
					lock.wait(timeout); 
					if (timeout > 0)
					{
						long timeSinceStart = runtimeBean.getUptime() - startTime - timeout; //This will be zero or less if timeout has expired
						if (timeSinceStart <= 0)
						{
							guild.getJDA().removeEventListener(messageListener);
							guildToListen = null;
							return null; //Timeout expired, and we didn't get anything.
						}
						else
						{
							timeout = (int) (timeout - timeSinceStart); //Maintain the timeout if a spurious unlock occurs. This ensures additional time isn't granted.
						}
					}
				}
			}
			guild.getJDA().removeEventListener(messageListener);
			Message[] messages = new Message[messageCount];
			for (int i = 0; i<messageCount; i++)
			{
				messages[i] = messageLog.get(messageLog.size()-messageCount-i);
			}
			guildToListen = null;
			return messages;
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			guild.getJDA().removeEventListener(messageListener);
			guildToListen = null;
			return null; //Let the calling method handle this.
		}
	}
	/**
	 * This method returns the first message to be sent after its invocation<br>
	 * within a specific channel of the guild that the bot has access to.<br>
	 * <br>
	 * This function will block the thread until the message count has been received, or the timeout elapses.<br>
	 * <br>
	 * @param channel
	 * @param timeout
	 * @param messageCount
	 * @return
	 * Message[] - The messages sent after the method was called, size matches messageCount
	 * null - If the thread is interrupted before a message is received, or a timeout occurs.
	 */
	private Message[] futureChannelMessageCollector(TextChannel channel, int timeout, int messageCount)
	{
		channelToListen = channel;
		messagesToGet = messageCount;
		channel.getJDA().addEventListener(messageListener);
		try 
		{
			int messageLogSize = messageLog.size();
			synchronized (lock)
			{
				while (messageLog.size() != (messageLogSize + messageCount)) //If no new entries have been added, continue the wait, as the thread wasn't meant to notify.
				{
			        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
			        long startTime = runtimeBean.getUptime(); //Getting the time to check for spurious wake-up or timeout later.
					lock.wait(timeout); 
					if (timeout > 0)
					{
						long timeSinceStart = runtimeBean.getUptime() - startTime - timeout; //This will be zero or less if timeout has expired
						if (timeSinceStart <= 0)
						{
							channel.getJDA().removeEventListener(messageListener);
							channelToListen = null;
							return null; //Timeout expired, and we didn't get anything.
						}
						else
						{
							timeout = (int) (timeout - timeSinceStart); //Maintain the timeout if a spurious unlock occurs. This ensures additional time isn't granted.
						}
					}
				}
			}
			channel.getJDA().removeEventListener(messageListener);
			Message[] messages = new Message[messageCount];
			for (int i = 0; i<messageCount; i++)
			{
				messages[i] = messageLog.get(messageLog.size()-messageCount-i);
			}
			channelToListen = null;
			return messages;
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			channel.getJDA().removeEventListener(messageListener);
			channelToListen = null;
			return null; //Let the calling method handle this.
		}
	}
	
	/**
	 * Waits and returns the first message to be sent in any channel of the guild after invocation.<br>
	 * This method will block the thread while waiting for a message.
	 * 
	 * @param guild
	 * @return
	 * Message - The message.
	 */
	public Message getNextMessage(Guild guild)
	{
		return futureGuildMessageCollector(guild, 0, 1)[0];
	}
	/**
	 * Waits and returns the first message to be sent in the channel after invocation.<br>
	 * This method will block the thread while waiting for a message.
	 * 
	 * @param channel
	 * @return
	 * Message - The message.
	 */
	public Message getNextMessage(TextChannel channel)
	{
		return futureChannelMessageCollector(channel, 0, 1)[0];
	}
	/**
	 * Waits and returns the first message to be sent in the guild after invocation and within a set time.<br>
	 * This method will block the thread while waiting for a message, or for the time limit to elapse.
	 * 
	 * @param guild
	 * @param timeout
	 * @return
	 * Message - The message.
	 */
	public Message getNextMessage(Guild guild, int timeout)
	{
		return futureGuildMessageCollector(guild, timeout, 1)[0];
	}
	/**
	 * Waits and returns the first message to be sent in the channel after invocation and within a set time.<br>
	 * This method will block the thread while waiting for a message, or for the time limit to elapse.
	 * 
	 * @param channel
	 * @param timeout
	 * @return
	 * Message - The message.
	 */
	public Message getNextMessage(TextChannel channel, int timeout)
	{
		return futureChannelMessageCollector(channel, timeout, 1)[0];
	}
	/**
	 * Waits and returns the first X messages to be sent in the guild after invocation.<br>
	 * This method will block the thread while waiting for all messages.
	 * 
	 * @param guild
	 * @param count - Message count required
	 * @return
	 * Message[] - Array of messages received equal to count.
	 */
	public Message[] getNextMessages(Guild guild, int count)
	{
		return futureGuildMessageCollector(guild, 0, count);
	}
	/**
	 * Waits and returns the first X messages to be sent in the channel after invocation.<br>
	 * This method will block the thread while waiting for all messages.
	 * 
	 * @param channel
	 * @param count - Message count required
	 * @return
	 * Message[] - Array of messages received equal to count.
	 */
	public Message[] getNextMessages(TextChannel channel, int count)
	{
		return futureChannelMessageCollector(channel, 0, count);
	}
	/**
	 * Waits and returns the first X messages to be sent in the guild after invocation and within the time limit.<br>
	 * This method will block the thread while waiting for all messages or for the time limit to expire.
	 * 
	 * @param guild
	 * @param count - Message count required
	 * @param timeout
	 * @return
	 * Message[] - Array of messages received equal to count.
	 */
	public Message[] getNextMessages(Guild guild, int timeout, int count)
	{
		return futureGuildMessageCollector(guild, timeout, count);
	}
	/**
	 * Waits and returns the first X messages to be sent in the channel after invocation and within the time limit.<br>
	 * This method will block the thread while waiting for all messages or for the time limit to expire.
	 * 
	 * @param channel
	 * @param count - Message count required
	 * @param timeout
	 * @return
	 * Message[] - Array of messages received equal to count.
	 */
	public Message[] getNextMessages(TextChannel channel, int timeout, int count)
	{
		return futureChannelMessageCollector(channel, timeout, count);
	}
	/**
	 * Takes all messages received from this instance and returns them as an array.
	 * 
	 * @return
	 * Message[] - Full history of all messages from this instance
	 */
	public Message[] getMessageHistory()
	{
		return messageLog.toArray(new Message[messageLog.size()]);
	}
	/**
	 * 
	 * This method retrieves all messages received by this MessageListener instance. 
	 * History is gathered starting from most recent and going back by the specified number.
	 * 
	 * If the total amount of messages requested is less than the total available, as many as possible will be returned.
	 * 
	 * @param count
	 * @return
	 * Message[] - Array of messages requested
	 */
	public Message[] getMessageHistoryFromEnd(int count)
	{
		if (count > messageLog.size())
		{
			count = messageLog.size();
		}
		Message[] messages = new Message[count];
		for (int i = 0; i<count; i++)
		{
			messages[i] = messageLog.get(messageLog.size()-(i+1));
		}
		return messages;
	}
	/**
	 * This method retrieves all messages received by this MessageListener instance. 
	 * History is gathered starting from the first message and going forward by the specified number.
	 * 
	 * If the total amount of messages requested is less than the total available, as many as possible will be returned.
	 * 
	 * @param count
	 * @return
	 * Message[] - Array of messages requested
	 */
	public Message[] getMessageHistoryFromStart(int count)
	{
		if (count > messageLog.size())
		{
			count = messageLog.size();
		}
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
	/**
	 * Resets the message history from this instance.<br>
	 * Note: Running this while gathering future messages will not work.
	 * 
	 * @return
	 * true - Message history reset.
	 * false - Message history not reset.
	 */
	public boolean resetMessageHistory()
	{
		if (guildToListen == null)
		{
			if (channelToListen == null)
			{
				messageLog = new ArrayList<Message>();
				return true;
			}
		}
		return false;
	}
	private class MessageListener extends ListenerAdapter
	{
		@Override
		public void onGuildMessageReceived(GuildMessageReceivedEvent msgEvent)
		{
			if (guildToListen != null)									//These checks set limits on where messages can be read from and are based on what parameters			
			{															//where passed to the previous methods.
				if (!guildToListen.equals(msgEvent.getGuild()))
				{
					return;
				}
			}
			if (channelToListen != null)
			{
				if (!channelToListen.equals(msgEvent.getChannel()))
				{
					return;
				}
			}
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

}
