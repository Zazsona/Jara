package jara;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import commands.CmdUtil;
import configuration.SettingsUtil;
import exceptions.QuitException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Class to streamline message collection.
 */
public class MessageManager
{
	private final Object lock = new Object();
	private ArrayList<Message> messageLog;
	private int messagesToGet = 0;
	private Guild guildToListen;
	private TextChannel channelToListen;
	private final MessageHandler messageHandler;

	/**
	 * Constructor
	 */
	public MessageManager()
	{
		messageLog = new ArrayList<>();
		messageHandler = new MessageHandler();
	}
	/**
	 * This method returns the first messages to be sent after its invocation<br>
	 * within any channel of the guild that the bot has access to.<br>
	 * This method will end when either the timeout expires, or the message count is hit. Whichever comes first.<br>
	 * <br>
	 * This function will block the thread until the message count has been received, or the timeout elapses.<br>
	 * <br>
	 * @param guild The guild to listen to
	 * @param timeout The amount of time to record messages for
	 * @param messageCount The number of messages to record
	 * @return The messages sent after the method was called, size matches messageCount, or null if the operation is interrupted, or time runs out with no messages
	 * Message[] - <br>
	 * null - If the thread is interrupted before a message is received, or a timeout occurs.
	 */
	private Message[] futureGuildMessageCollector(Guild guild, int timeout, int messageCount)
	{
		guildToListen = guild;
		messagesToGet = messageCount;
		guild.getJDA().addEventListener(messageHandler);
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
							guild.getJDA().removeEventListener(messageHandler);
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
			guild.getJDA().removeEventListener(messageHandler);
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
			guild.getJDA().removeEventListener(messageHandler);
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
	 * @param channel The channel to listen to
	 * @param timeout The amount of time to record messages for
	 * @param messageCount The number of messages to record
	 * @return The messages sent after the method was called, size matches messageCount, or null if the operation is interrupted, or time runs out with no messages
	 */
	private Message[] futureChannelMessageCollector(TextChannel channel, long timeout, int messageCount)
	{
		channelToListen = channel;
		messagesToGet = messageCount;
		channel.getJDA().addEventListener(messageHandler);
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
						if (timeSinceStart <= 100)
						{
							break; //Timeout expired.
						}
						else
						{
							timeout = (timeout - timeSinceStart); //Maintain the timeout if a spurious unlock occurs. This ensures additional time isn't granted.
						}
					}
				}
			}
			channel.getJDA().removeEventListener(messageHandler);
			channelToListen = null;
			if (messageLogSize != messageLog.size())
			{
				int msgArraySize = messageCount;
				if (messageLog.size() < messageCount)	//Sets how big to declare the array.
				{
					msgArraySize = messageLog.size();	//This allows us to set a message count of Integer.MAX_VALUE without concern of how much space the array will reserve.
				}
				Message[] messages = new Message[msgArraySize];
				for (int i = 0; i<messages.length; i++)
				{
					messages[i] = messageLog.get(messageLog.size()-msgArraySize+i);
				}
				return messages;
			}
			else
			{
				return null;	//No messages were received
			}
		} 
		catch (InterruptedException e) 
		{
			channel.getJDA().removeEventListener(messageHandler);
			channelToListen = null;
			return null; //Let the calling method handle this.
		}
	}
	
	/**
	 * Waits and returns the first message to be sent in any channel of the guild after invocation.<br>
	 * This method will block the thread while waiting for a message.
	 * 
	 * @param guild The guild to listen to
	 * @return the message
	 */
	public Message getNextMessage(Guild guild)
	{
		try
		{
			return futureGuildMessageCollector(guild, 0, 1)[0];
		}
		catch (ArrayIndexOutOfBoundsException | NullPointerException e)
		{
			return null;
		}
	}
	/**
	 * Waits and returns the first message to be sent in the channel after invocation.<br>
	 * This method will block the thread while waiting for a message.
	 * 
	 * @param channel The channel to listen to
	 * @return the message
	 */
	public Message getNextMessage(TextChannel channel)
	{
		try
		{
			return futureChannelMessageCollector(channel, 0, 1)[0];
		}
		catch (ArrayIndexOutOfBoundsException | NullPointerException e)
		{
			return null;
		}
	}
	/**
	 * Waits and returns the first message to be sent in the guild after invocation and within a set time.<br>
	 * This method will block the thread while waiting for a message, or for the time limit to elapse.
	 * 
	 * @param guild The guild to listen to
	 * @param timeout The amount of time to record for
	 * @return the message
	 */
	public Message getNextMessage(Guild guild, int timeout)
	{
		try
		{
			return futureGuildMessageCollector(guild, timeout, 1)[0];
		}
		catch (ArrayIndexOutOfBoundsException | NullPointerException e)
		{
			return null;
		}
	}
	/**
	 * Waits and returns the first message to be sent in the channel after invocation and within a set time.<br>
	 * This method will block the thread while waiting for a message, or for the time limit to elapse.
	 * 
	 * @param channel The channel to listen to
	 * @param timeout The amount of time to record for
	 * @return the message
	 */
	public Message getNextMessage(TextChannel channel, int timeout)
	{
		try
		{
			return futureChannelMessageCollector(channel, timeout, 1)[0];
		}
		catch (ArrayIndexOutOfBoundsException | NullPointerException e)
		{
			return null; //No messages
		}

	}
	/**
	 * Waits and returns the first X messages to be sent in the guild after invocation.<br>
	 * This method will block the thread while waiting for all messages.
	 * 
	 * @param guild The guild to listen to
	 * @param count Message count required
	 * @return The messages sent after the method was called, size matches count, or null if the operation is interrupted
	 */
	public Message[] getNextMessages(Guild guild, int count)
	{
		return futureGuildMessageCollector(guild, 0, count);
	}
	/**
	 * Waits and returns the first X messages to be sent in the channel after invocation.<br>
	 * This method will block the thread while waiting for all messages.
	 * 
	 * @param channel The channel to listen to
	 * @param count Message count required
	 * @return The messages sent after the method was called, size matches count, or null if the operation is interrupted
	 */
	public Message[] getNextMessages(TextChannel channel, int count)
	{
		return futureChannelMessageCollector(channel, 0, count);
	}
	/**
	 * Waits and returns the first X messages to be sent in the guild after invocation and within the time limit.<br>
	 * This method will block the thread while waiting for all messages or for the time limit to expire.
	 * 
	 * @param guild The guild to listen to
	 * @param count Message count required
	 * @param timeout The amount of time to record for
	 * @return The messages sent after the method was called, size matches count, or null if the operation is interrupted, or time runs out with no messages
	 */
	public Message[] getNextMessages(Guild guild, int timeout, int count)
	{
		return futureGuildMessageCollector(guild, timeout, count);
	}
	/**
	 * Waits and returns the first X messages to be sent in the channel after invocation and within the time limit.<br>
	 * This method will block the thread while waiting for all messages or for the time limit to expire.
	 * 
	 * @param channel The channel to listen to
	 * @param count Message count required
	 * @param timeout The amount of time to record for
	 * @return The messages sent after the method was called, size matches count, or null if the operation is interrupted, or time runs out with no messages
	 */
	public Message[] getNextMessages(TextChannel channel, int timeout, int count)
	{
		return futureChannelMessageCollector(channel, timeout, count);
	}

	/**
	 * Gets confirmation (yes or no) from the specified user anywhere in the guild.<br><br>
	 *     In the case the user responds with an invalid answer, a notice is sent in the default embed style.
	 * @param guild the guild to listen to
	 * @param member the member to listen for
	 * @return true on confirmation, and vice versa.
	 * @throws QuitException the user quit the operation
	 */
	public boolean getNextConfirmation(Guild guild, Member member) throws QuitException
	{
		while (true)
		{
			Message msg = getNextMessage(guild);
			if (msg.getMember().equals(member))
			{
				return getConfirmation(msg, true, null);
			}
		}
	}

	/**
	 * Gets confirmation (yes or no) from the specified user anywhere in the guild.<br><br>
	 *     In the case the user responds with an invalid answer, a notice is sent in the supplied embed style.
	 * @param guild the guild to listen to
	 * @param member the member to listen for
	 * @param embed the embed style to use
	 * @return true on confirmation, and vice versa.
	 * @throws QuitException the user quit the operation
	 */
	public boolean getNextConfirmation(Guild guild, Member member, EmbedBuilder embed) throws QuitException
	{
		while (true)
		{
			Message msg = getNextMessage(guild);
			if (msg.getMember().equals(member))
			{
				return getConfirmation(msg, true, embed);
			}
		}
	}

	/**
	 * Gets confirmation (yes or no) from the specified user in the channel.<br><br>
	 *     In the case the user responds with an invalid answer, a notice is sent in the default embed style.
	 * @param channel the channel to listen to
	 * @param member the member to listen for
	 * @return true on confirmation, and vice versa.
	 * @throws QuitException the user quit the operation
	 */
	public boolean getNextConfirmation(TextChannel channel, Member member) throws QuitException
	{
		while (true)
		{
			Message msg = getNextMessage(channel);
			if (msg.getMember().equals(member))
			{
				return getConfirmation(msg, true, null);
			}
		}
	}

	/**
	 * Gets confirmation (yes or no) from the specified user in the channel.<br><br>
	 *     In the case the user responds with an invalid answer, a notice is sent in the supplied embed style.
	 * @param channel the channel to listen to
	 * @param member the member to listen for
	 * @param embed the embed style to use
	 * @return true on confirmation, and vice versa.
	 * @throws QuitException the user quit the operation
	 */
	public boolean getNextConfirmation(TextChannel channel, Member member, EmbedBuilder embed) throws QuitException
	{
		while (true)
		{
			Message msg = getNextMessage(channel);
			if (msg.getMember().equals(member))
			{
				return getConfirmation(msg, true, embed);
			}
		}
	}

	private boolean getConfirmation(Message message, boolean sendInvalidInputMessage, EmbedBuilder embedStyle) throws QuitException
	{
		String messageContent = message.getContentDisplay();
		if (messageContent.equalsIgnoreCase("yes") || messageContent.equalsIgnoreCase("confirm") || messageContent.equalsIgnoreCase("y"))
		{
			return true;
		}
		else if (messageContent.equalsIgnoreCase("no") ||  messageContent.equalsIgnoreCase("deny") || messageContent.equalsIgnoreCase("n"))
		{
			return false;
		}
		else if (messageContent.equalsIgnoreCase("quit") || messageContent.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(message.getGuild().getId())+"quit"))
		{
			throw new QuitException();
		}
		else if (sendInvalidInputMessage)
		{
			EmbedBuilder embed = (embedStyle != null) ? embedStyle : new EmbedBuilder().setColor(CmdUtil.getHighlightColour(message.getGuild().getSelfMember()));
			embed.setDescription("Unrecognised option. Yes/No expected.");
			message.getChannel().sendMessage(embed.build()).queue();
		}
	}

	/**
	 * Gets every message this object instance has gathered.
	 * @return the messages
	 */
	public List<Message> getMessageHistory()
	{
		return Collections.unmodifiableList(messageLog);
	}
	/**
	 * 
	 * This method retrieves all messages received by this instance.
	 * History is gathered starting from most recent and going back by the specified number, or until there are no more.
	 * 
	 * @param count The number of messages to get
	 * @return the messages
	 */
	public Message[] getMessageHistoryFromRecent(int count)
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
	 * This method retrieves all messages received by this instance.
	 * History is gathered starting from the first message and going forward by the specified number, or until there are no more.
	 * @param count The number of messages to get
	 * @return the messages
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
	 * The output here will match that of the last getNextMessage() call.
	 * @return the message
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
	 * Resets the message history for this instance.<br>
	 * @throws ConcurrentModificationException reset while gathering messages
	 */
	public void resetMessageHistory() throws ConcurrentModificationException
	{
		if (guildToListen == null && channelToListen == null)
		{
			messageLog = new ArrayList<>();
		}
		throw new ConcurrentModificationException("Cannot reset while gathering messages.");
	}

	/**
	 * An EventHandler that gathers messages
	 */
	private class MessageHandler extends ListenerAdapter
	{
		@Override
		public void onGuildMessageReceived(GuildMessageReceivedEvent msgEvent)
		{
			if (!msgEvent.getAuthor().isBot())
			{
				if (guildToListen != null)									//These checks set limits on where messages can be read from and are based on what parameters
				{															//were passed to the previous methods.
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

}
