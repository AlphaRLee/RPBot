package com.rlee.discordbots.rpbot;

import java.util.Arrays;
import java.util.List;

import com.rlee.discordbots.rpbot.command.CommandParser;
import com.rlee.discordbots.rpbot.dice.RollCalculator;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.map.RPMap;
import com.rlee.discordbots.rpbot.profile.Attribute;
import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.profile.ProfilePrinter;
import com.rlee.discordbots.rpbot.reader.ProfileReader;
import com.rlee.discordbots.rpbot.regitstry.ProfileRegistry;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

	public static String COMMAND_PREFIX = "&";	//Official RPBot
//	public static String COMMAND_PREFIX = "^";	//RPTester
	private RollCalculator rollCalculator;
	
	public MessageListener() {
		rollCalculator = new RollCalculator();
	}
	
	public MessageListener(String commandPrefix) {
		this();
		setCommandPrefix(commandPrefix);
	}
	
	public static void setCommandPrefix(String prefix) {
		COMMAND_PREFIX = prefix;
	}
	
	/**
	 * General message received event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();

		// TODO: Fill me!

		if (message.toLowerCase().startsWith(COMMAND_PREFIX + "ping")) {
			MessageChannel channel = event.getChannel();
			// TextChannel textChannel = event.getTextChannel();

			boolean canSendMessage = true;
			String output = "Pong! I'm growing to build up your tabletop RPG experience!"
					+ "\n```\nResponse ping time: "
					+ event.getJDA().getPing() + "\n```";

			canSendMessage = event.getTextChannel().canTalk();

			if (!canSendMessage) {
				System.out.println("Error, RPBot cannot send message: \n\t" + output
						+ "\nIs it lacking MESSAGE_WRITE permission?");
				return;
			}

			MessageBuilder outputBuilder = new MessageBuilder();
			outputBuilder.append(output);
			// outputBuilder.setColor(Color.ORANGE);

			channel.sendMessage(outputBuilder.build()).queue();
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		final int COMMAND_ARG = 0;
		
		Message message = event.getMessage();
		Member member = event.getMember();
		String content = message.getContent();
		TextChannel channel = event.getChannel();
		
		if (content.startsWith(COMMAND_PREFIX)) {
			content = content.substring(COMMAND_PREFIX.length()); //Get rid of command prefix from front
		} else {
			return; //Does not have command prefix, ignore
		}
		
		String[] args = content.split(" ");
		
		if (args.length < 1 || RPBot.isEmptyString(content)) {
			return; //Nothing placed after command prefix!
		}
		
		RPGame game = RPBot.getGame(event.getGuild());
		
		if (game == null) {
			return; //TODO: Add some error dialogue here
		}
		
		CommandParser cmdParser = new CommandParser(args, channel);
		String lowerCommand = args[COMMAND_ARG].toLowerCase();
		switch (lowerCommand) {
		case "roll": case "r":
			rollCalculator.compute(content.substring(args[COMMAND_ARG].length()), channel, event.getAuthor(), true);
			break;
		
		case "alias": case "multialias": {
			if (!cmdParser.validateParameterLength(new String[] {"alias name", "full name"}, new String[] {"alias 2", "full 2", "alias 3", "full 3", "..."})) {
				break;
			}
			
			String output = "Alias(es) set:\n";
			
			//HEY! LISTEN! Iterating every other arg, starting on args[2] (first full name)
			for (int i = 2; i < args.length; i += 2) {
				game.getAliasRegistry().setAlias(args[i - 1], args[i]);
				output += "\t" + args[i - 1].toLowerCase() + " **->** " + args[i].toLowerCase();
			}
			
			//Send message, removing last character
			channel.sendMessage(output).queue();
			
			break;
		}	
		case "read": {
			readCmd(game.getProfileRegistry(), event);
			break;
		}	
		case "list": {
			if (game.getProfileRegistry().getProfilesByName().isEmpty()) {
				channel.sendMessage("No characters have been added yet! Try using " + COMMAND_PREFIX + "addchar").queue();
				break;
			}
			
			ProfilePrinter profilePrinter = new ProfilePrinter();
			for (CharProfile profile : game.getProfileRegistry().getProfilesByName().values()) {
				profilePrinter.printProfileToChannel(profile, channel);
			}
			break;
		}
		case "listchar": {
			CharProfile profile = null;
			if (args.length >= 2 && !RPBot.isEmptyString(args[1])) {
				 profile = game.getProfileRegistry().getProfile(args[1]);
			} else {
				profile = game.getProfileRegistry().getProfile(member);
			}
			
			if (profile == null) {
				channel.sendMessage("No character profile found!").queue();
				break;
			}
			
			ProfilePrinter profilePrinter = new ProfilePrinter();
			profilePrinter.printProfileToChannel(profile, channel);
			break;
		}
		case "listattr": case "listattribute": case "attr": case "attribute": {
			if (!cmdParser.validateParameterLength(new String[] {"alias name"}, new String[] {"alias 2", "full 2", "alias 3", "full 3", "..."})) {
				break;
			}
			
			CharProfile profile = null;
			if (args.length >= 3 && !RPBot.isEmptyString(args[2])) {
				 profile = game.getProfileRegistry().getProfile(args[2]);
			} else {
				profile = game.getProfileRegistry().getProfile(member);
			}
			
			if (profile == null) {
				channel.sendMessage("No character profile found!").queue();
				break;
			}
			
			Attribute attribute = game.getAliasRegistry().getAttribute(args[1], profile);
			
			if (attribute == null) {
				channel.sendMessage("No attribute found by the name: **" + args[1] + "** for " + profile.getName() + ".").queue();
				break;
			}
			
			ProfilePrinter profilePrinter = new ProfilePrinter();
			profilePrinter.messageAttributeDetail(profile, attribute, channel);
			break;
		}
		case "addcharid": case "addcharfromid": case "addcharacterfromid": {
			if (!cmdParser.validateParameterLength(new String[] {"#channel", "message ID"})) {
				break;
			}
			
			List<TextChannel> mentionedChannels = message.getMentionedChannels();
			if (mentionedChannels.isEmpty()) {
				event.getChannel().sendMessage("Sorry, you must mention a **#channel** to read the profile from!").queue();
				return;
			}
			
			//Operation blocks thread until request is completed. Avoid using the .complete() method when possible!
			Message sourceMessage = mentionedChannels.get(0).getMessageById(args[2]).complete();
			
			ProfileReader reader = new ProfileReader();
			CharProfile profile = reader.readProfileFromMessage(sourceMessage, game.getProfileRegistry());
			
			if (!reader.isValidProfile(profile)) {
				channel.sendMessage("Character profile creation failed! Make sure the source message has a unique name!").queue();
				break;
			}
			
			game.getProfileRegistry().addProfile(profile);
			channel.sendMessage("Character **" + profile.getName() + "** added!").queue();
			break;
		}
		case "delchar": case "deletecharacter": {
			if (!cmdParser.validateParameterLength(new String[] {"name"})) {
				break;
			}
			
			CharProfile profile = game.getProfileRegistry().getProfile(args[1]);
			
			if (profile == null) {
				channel.sendMessage("No character profile found.").queue();
				break;
			}
			
			//TODO: Add check for deleting profiles with members
			game.getProfileRegistry().removeProfile(profile);
			
			channel.sendMessage("Character **" + profile.getName() + "** has been deleted!").queue();
			break;
		}
		case "save": case "savechar": {
			CharProfile profile = null;
			if (args.length >= 2 && !RPBot.isEmptyString(args[1])) {
				profile = game.getProfileRegistry().getProfile(args[1]);
			} else {
				profile = game.getProfileRegistry().getProfile(member);
			}
			
			if (profile == null) {
				channel.sendMessage("No character profile found!").queue();
				break;
			}
			
			game.saveProfile(profile);
			channel.sendMessage(profile.getName() + "'s information has been saved!").queue();
			break;
		}
		case "claim": case "claimchar": {
			claimProfileCmd(args, member, game, channel);
			break;
		}
		case "unclaim": {
			unclaimProfileCmd(member, game, channel);
			break;
		}
		case "set": case "setattr": {
			/*
			 * Set an existing attribute to the given score
			 * &set <value>[/maxvalue] <attribute> [character]
			 * Eg: &set hp 20
			 * Eg: &set stam 20/30 Bob
			 */
			
			if (!cmdParser.validateParameterLength(new String[] {"attribute", "value"}, new String[] {"\b/maxvalue", "character"})) {
				break;
			}
			
			CharProfile profile = null;
			if (args.length >= 4 && !RPBot.isEmptyString(args[3])) {
				 profile = game.getProfileRegistry().getProfile(args[3]);
			} else {
				profile = game.getProfileRegistry().getProfile(member);
			}
			
			if (profile == null) {
				channel.sendMessage("No character profile found!").queue();
				break;
			}
			
			Attribute attribute = game.getAliasRegistry().getAttribute(args[1], profile);
			if (attribute == null) {	
				//Insert a new attribute
				//TODO Set the following code into constructor of Attribute
				attribute = new Attribute(args[1]);
				attribute.setProfile(profile);
				profile.setAttribute(args[1], attribute);
			}
			
			String[] values = args[2].split("/"); //TODO Remove magic value
			Integer value = null, maxValue = null;
			try {
				value = Integer.valueOf(values[0]);
			} catch (NumberFormatException e) {
				channel.sendMessage("Sorry, **" + values[0] + "** is not recognized as a number").queue();
				break;
			}
			
			if (values.length >= 2 && !values[1].isEmpty()) {
				//Max value has been found
				try {
					maxValue = Integer.valueOf(values[1]);
				} catch (NumberFormatException e) {
					channel.sendMessage("Sorry, **" + values[1] + "** is not recognized as a number").queue();
					break;
				}
			}
			
			attribute.setValue(value, true);
			if (maxValue != null) {
				attribute.setMaxValue(maxValue);
			}
			
			ProfilePrinter profilePrinter = new ProfilePrinter();
			profilePrinter.messageAttributeDetail(profile, attribute, channel);
			break;
		}
		case "delattr": case "deleteattribute": {
			/*
			 * Delete an attribute from a profile
			 * &delattr <attribute> <player>
			 */
			if (!cmdParser.validateParameterLength(new String[] {"attribute"}, new String[] {"character"})) {
				return;
			}
			
			CharProfile profile = null;
			if (args.length >= 3 && !RPBot.isEmptyString(args[2])) {
				profile = game.getProfileRegistry().getProfile(args[2]);
			} else {
				profile = game.getProfileRegistry().getProfile(member);
			}
			
			if (profile == null) {
				channel.sendMessage("No character profile found!").queue();
				break;
			}
			
			Attribute attribute = game.getAliasRegistry().getAttribute(args[1], profile);
			if (attribute == null) {
				channel.sendMessage("No attribute found by the name: **" + args[1] + "** for " + profile.getName() + ".").queue();
				break;
			}
			
			profile.removeAttribute(attribute);
			channel.sendMessage("The attribute **" + attribute.getName() + "** has been removed from **" + profile.getName() + "**.").queue();
			break;
		}	
		//TODO Delete these test cases
		case "guildid": {
			channel.sendMessage("The guild ID is: " + game.getGuild().getId()).queue();
			break;
		}
		case "map": {
			RPMap rpMap = new RPMap();

			rpMap.setAt(2, 3, 'c', "Camel");
			rpMap.setAt(2, 4, 'd', "Dingo");
			rpMap.setAt(1, 5, 'e', "Emu");
			rpMap.setAt(1, 2, '/', "Wall");

			rpMap.setAt(7, 4, '\u2588', "Wall");
			rpMap.setAt(6, 4, '\u2588', "Wall");
			rpMap.setAt(6, 5, '\u2588', "Wall");
			channel.sendMessage("TEST Map generated:\n" + rpMap.showMap()).queue();
		}
		
		}
		
		//Cases for "+" and "-" commands:
		switch (lowerCommand.charAt(0)) {
		case '+': case '-':
			addToAttributeCmd(args, member, channel, game);
			break;
		}
		
		if (lowerCommand.startsWith("addchar") || lowerCommand.startsWith("addcharacter")) {
			if (args.length <= 1 || content.split("\n").length <= 1) {
				channel.sendMessage("Format: " + COMMAND_PREFIX + args[0] + "\nName: <name>\n<attribute name>: <attribute value>\n...").queue();
				return;
			}
					
			ProfileReader reader = new ProfileReader();
			CharProfile profile = reader.readProfileFromMessage(message, game.getProfileRegistry());
			
			if (!reader.isValidProfile(profile)) {
				channel.sendMessage("Character profile creation failed, make sure the profile name is unique!").queue();
				return;
			}
			
			game.getProfileRegistry().addProfile(profile);
			channel.sendMessage("Character **" + profile.getName() + "** added!").queue();
			return;
		}
	}

	// TODO: Delete test function
	private void readCmd(ProfileRegistry profileRegistry, GuildMessageReceivedEvent event) {
		ProfileReader reader = new ProfileReader();
		Message message = event.getMessage();
		List<TextChannel> mentionedChannels = message.getMentionedChannels();

		if (mentionedChannels.isEmpty()) {
			event.getChannel().sendMessage("Sorry, you must mention a #channel to read the character profiles from!").queue();
			return;
		}

		reader.readProfilesFromChannel(profileRegistry, mentionedChannels.get(0), 50);
		event.getChannel().sendMessage("Character profiles read from " + mentionedChannels.get(0).getAsMention() + "!").queue();
	}
	
	private void claimProfileCmd(String[] args, Member member, RPGame game, MessageChannel channel) {
		CommandParser cmdParser = new CommandParser(args, (TextChannel) channel);
		if (!cmdParser.validateParameterLength(new String[] {"profile name"})) {
			return;
		}
		
		CharProfile profile = game.getProfileRegistry().getProfile(args[1]);
		if (profile == null) {
			channel.sendMessage("No character profile was found by the name of **" + args[1] + "**.").queue();
			return;
		}
		
		if (profile.getMember() != null) {
			//Profile is already claimed
			channel.sendMessage("The profile **" + profile.getName() + "** is already claimed by " + profile.getMember().getEffectiveName() + "!").queue();
			return;
		}
		
		CharProfile oldProfile = game.getProfileRegistry().getProfile(member);
		boolean hasUnclaimedOldProfile = (oldProfile != null);
		if (hasUnclaimedOldProfile) {
			game.getProfileRegistry().unclaimProfile(member);
		}
		
		game.getProfileRegistry().claimProfile(member, profile);
		
		String output = member.getAsMention() + " has claimed the character profile **" + profile.getName() + "**"
				+ (hasUnclaimedOldProfile ? " and has unclaimed **" + oldProfile.getName() + "**" : "")
				+ ".";
		channel.sendMessage(output).queue();
		return;
	}
	
	private void unclaimProfileCmd(Member member, RPGame game, MessageChannel channel) {
		if (member == null || channel == null) {
			return;
		}
		
		CharProfile profile = game.getProfileRegistry().unclaimProfile(member);
		if (profile == null) {
			channel.sendMessage("You can't unclaim a profile that you never had...").queue();
			return;
		}
		
		channel.sendMessage(member.getAsMention() + " has unclaimed the character profile **" + profile.getName() + "**.").queue();
	}
	
	/**
	 * Add the numeric value from args[0] into the attribute from args[1] 
	 * @param args arguments passed in the order of operation, attribute name, duration (optional) and profile name (optional)
	 * <b>REQUIRES</b> that first character in args[0] is either '+' or '-'
	 * @param channel
	 *
	 * @author R Lee
	 */
	private void addToAttributeCmd(String[] args, Member member, MessageChannel channel, RPGame game) {
		final int OPERATOR_ARG = 0;
		final int ATTR_ARG = 1;
		final int DURATION_ARG = 2; //Variable argument: Can either be duration or profile name. Can ONLY be duration if args[3] exists
		final int PROFILE_ARG = 3;
		final int MAX_COUNT = 10;
		final int INVALID_VAL = -999; //Marker to set invalid integers to (with exception to signVal)
		
		int profileArg = INVALID_VAL; //Variable argument holding value of name, dependent on number of inputs
		
		//Determine if we have addition operator, subtraction operator or neither (illegal)
		//Sets to 1 for addition, -1 for subtraction, 0 for illegal
		int signVal = (args[OPERATOR_ARG].charAt(0) == '+' ? 1 : args[OPERATOR_ARG].charAt(0) == '-' ? -1 : 0);
		int signCount = 1; //ASSUMED 1 because of the expected inputs
		int operatorArgLength = args[OPERATOR_ARG].length();

		int value;
		Attribute attribute = null;
		int duration = INVALID_VAL;
		CharProfile profile = game.getProfileRegistry().getProfile(member);
		
		
		if (args.length < ATTR_ARG + 1) {
			channel.sendMessage("Format: " + COMMAND_PREFIX + args[OPERATOR_ARG] + " <attribute> [duration] [character]").queue();
			return;
		}
		
		if (signVal == 0) {
			return; //TODO: Add error message? Theoretically impossible to reach
		}
		
		//Determine nature of duration and profile, if applicable
		if (args.length >= DURATION_ARG + 1) {
			//Give priority parsing to duration
			try {
				duration = Integer.parseInt(args[DURATION_ARG]);
			} catch (NumberFormatException e) {
				//Failed to recognize as Integer, attempt to read as name
				profile = game.getProfileRegistry().getProfile(args[DURATION_ARG]);
			}
			
			if (args.length >= PROFILE_ARG + 1) {
				if (duration == INVALID_VAL) {
					//Profile was specifed and duration is invalid. Send error
					channel.sendMessage("Format: " + args[OPERATOR_ARG] + " <attribute> [duration] [character]").queue();
					return;
				}
				
				profileArg = PROFILE_ARG; //Profile specified and duration valid. Select profile arg for profile
			} else {
				if (duration == INVALID_VAL) {
					//Duration not specified, attempt to parse as profile instead
					profileArg = DURATION_ARG; //Profile specified and duration valid. Select profile arg for profile
				}
			}
		}
		
		if (profileArg != INVALID_VAL) {
			profile = game.getProfileRegistry().getProfile(args[profileArg]);
		} else {
			profile = game.getProfileRegistry().getProfile(member); //Get default profile name by member
		}
		
		//Validate profile
		if (profile == null) {
			channel.sendMessage("No character profile found!").queue();
			return;
		}
		
		//TODO: Add attr if attr is missing from profile
		attribute = game.getAliasRegistry().getAttribute(args[ATTR_ARG], profile);
		if (attribute == null) {
			channel.sendMessage("No attribute found by the name: **" + args[ATTR_ARG] + "** for " + profile.getName() + ".").queue();
			return;
		}
		
		//Get how many consecutive operator signs there are (eg. "+" vs "++" vs "---")
		while (signCount < operatorArgLength && signCount < MAX_COUNT
				&& args[OPERATOR_ARG].charAt(signCount) == args[OPERATOR_ARG].charAt(signCount - 1)) {
			signCount++;
		}
		
		boolean bypassLimits = false;
		
		switch (signCount) {
		case 1:
			break;
		case 2:
			bypassLimits = true;
			break;
		default:
			channel.sendMessage("Operation not recognized! Try reducing the number of operators!").queue();
			return;
		}
		
		if (signCount >= operatorArgLength) {
			//Entire string is just operator, no number
			channel.sendMessage("Please add a number to the end of your operation! Eg. " + COMMAND_PREFIX + "+1").queue();
			return;
		}
		
		//Get the value to be added/subtracted (FINALLY!)
		try {
			value = Integer.parseInt(args[OPERATOR_ARG].substring(signCount));
		} catch (NumberFormatException e) {
			channel.sendMessage("Cannot read **" + args[OPERATOR_ARG].substring(signCount) + "** as an integer. Try try again...");
			return;
		}
		
		String output = profile.getName();
		if (duration != INVALID_VAL) {
			attribute.setBuff(value * signVal, duration);
			
			output += " got a **" + (signVal == 1 ? "+" + value + "** buff" : "-" + value + "** debuff") 
					+ " on " + attribute.getName().toLowerCase()
					+ " for " + duration + " " + (duration == 1 ? "roll" : "rolls") + ".";
		} else {
			int oldValue = attribute.getValue();
			
			attribute.addToValue(value * signVal, bypassLimits);
			
			String maxValOutput = (attribute.hasMaxValue() ? "/" + attribute.getMaxValue() : "");
			output += " " + (signVal == 1 ? "gained" : "lost") + " **" + value + "** " + attribute.getName().toLowerCase() + ".";
			output += " (" + oldValue + maxValOutput + " -> " + attribute.getValue() + maxValOutput + ")";
		}
	
		channel.sendMessage(output).queue();
	}
	
	/**
	 * Get the 2nd (index of 1) arg and all args following that one; get the parameters
	 * @param args
	 * @return
	 *
	 * @author R Lee
	 * 
	 * @deprecated Convention: All args should be passed, including command arg - RLee 16/06/2018
	 */
	@Deprecated
	public static String[] paramArgs(String[] args)
	{
		//For Arrays.copyOfRange:
		//1st parameter: Original array list
		//2nd parameter: From index (inclusive)
		//3rd parameter: To index (exclusive)
		return Arrays.copyOfRange(args, 1, args.length);
	}
}
