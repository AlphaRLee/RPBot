package com.rlee.discordbots.rpbot;

import java.util.Arrays;
import java.util.List;

import com.rlee.discordbots.rpbot.command.CommandParser;
import com.rlee.discordbots.rpbot.map.MapCommandHandler;
import com.rlee.discordbots.rpbot.dice.RollCalculator;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.profile.Attribute;
import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.profile.ProfilePrinter;
import com.rlee.discordbots.rpbot.reader.ProfileReader;
import com.rlee.discordbots.rpbot.regitstry.ProfileRegistry;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

	public static String COMMAND_PREFIX = "&";	//Official RPBot
	private RollCalculator rollCalculator;
	private MapCommandHandler mapCommandHandler;
	
	public MessageListener() {
		rollCalculator = new RollCalculator();
		mapCommandHandler = new MapCommandHandler();
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
		String message = event.getMessage().getContentRaw();

		if (message.toLowerCase().startsWith(COMMAND_PREFIX + "ping")) {
			MessageChannel channel = event.getChannel();
			// TextChannel textChannel = event.getTextChannel();

			boolean canSendMessage = true;
			String output = "Pong! I'm growing to build up your tabletop RPG experience!"
					+ "\n```\nResponse ping time: "
					+ event.getJDA().getGatewayPing() + "\n```";

			canSendMessage = event.getTextChannel().canTalk();

			if (!canSendMessage) {
				System.out.println("Error, RPBot cannot send message: \n\t" + output
						+ "\nIs it lacking MESSAGE_WRITE permission?");
				return;
			}

			MessageBuilder outputBuilder = new MessageBuilder();
			outputBuilder.append(output);
//			outputBuilder.setColor(Color.ORANGE);

			channel.sendMessage(outputBuilder.build()).queue();
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		final int COMMAND_ARG = 0;
		
		Message message = event.getMessage();
		Member member = event.getMember();
		String content = message.getContentRaw();
		TextChannel channel = event.getChannel();
		
		if (content.startsWith(COMMAND_PREFIX)) {
			content = content.substring(COMMAND_PREFIX.length()); //Get rid of command prefix from front
		} else {
			return; //Does not have command prefix, ignore
		}
		
		String[] args = content.split(" ");
		
		if (args.length < 1 || Util.isEmptyString(content)) {
			return; //Nothing placed after command prefix!
		}
		
		RPGame game = RPBot.getGame(event.getGuild());
		
		if (game == null) {
			channel.sendMessage("Sorry, you need to set up your game before you can play. Please contact the developer!").queue();
			return;
		}
		
		CommandParser cmdParser = new CommandParser(args, channel);
		String lowerCommand = args[COMMAND_ARG].toLowerCase();
		switch (lowerCommand) {
		case "help": case "?": {
			cmdParser.setErrorDescription("https://github.com/AlphaRLee/RPBot/blob/main/Commands.md");
			cmdParser.sendUserError("Please visit the following link for help:");
			break;
		}
		case "roll": case "r":
			rollCalculator.compute(content.substring(args[COMMAND_ARG].length()), channel, message, true);
			break;
		
		case "alias": case "multialias": {
			if (!cmdParser.validateParameterLength(new String[] {"alias\\_name", "full\\_name"}, "alias\\_2", "full\\_2", "alias\\_3", "full\\_3", "...")) {
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
				channel.sendMessage("No characters have been added yet! Try using `" + COMMAND_PREFIX + "addchar`").queue();
				break;
			}
			
			ProfilePrinter profilePrinter = new ProfilePrinter();
			for (CharProfile profile : game.getProfileRegistry().getProfilesByName().values()) {
				profilePrinter.printProfileToChannel(profile, channel);
			}
			break;
		}
		case "listchar": case "char": {
			CharProfile profile = getProfileOrSendError(args.length > 1 ? args[1] : null, game, member, channel);
			if (profile == null) {
				break;
			}
			
			ProfilePrinter profilePrinter = new ProfilePrinter();
			profilePrinter.printProfileToChannel(profile, channel);
			break;
		}
		case "listattr": case "listattribute": case "attr": case "attribute": {
			if (!cmdParser.validateParameterLength(new String[] {"attribute"}, "character")) {
				break;
			}

			CharProfile profile = getProfileOrSendError(args.length > 2 ? args[2] : null, game, member, channel);
			if (profile == null) {
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
			if (!cmdParser.validateParameterLength(new String[] {"#channel", "message\\_ID"})) {
				break;
			}

			List<TextChannel> mentionedChannels = message.getMentionedChannels();
			if (mentionedChannels.isEmpty()) {
				event.getChannel().sendMessage("Sorry, you must mention a **#channel** to read the profile from!").queue();
				return;
			}
			
			//Operation blocks thread until request is completed. Avoid using the .complete() method when possible!
			Message sourceMessage = mentionedChannels.get(0).retrieveMessageById(args[2]).complete();

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
			if (!cmdParser.validateParameterLength(new String[] {"character"})) {
				break;
			}

			// Explicitly check for profile here - not an optional param
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
			CharProfile profile = getProfileOrSendError(args.length > 1 ? args[1] : null, game, member, channel);
			if (profile == null) {
				break;
			}
			
			game.saveProfile(profile);
			channel.sendMessage(profile.getName() + "'s information has been saved!").queue();
			break;
		}
		case "claim": case "claimchar": {
			claimProfileCmd(args, game, channel, message);
			break;
		}
		case "unclaim": case "unclaimchar": {
			unclaimProfileCmd(args, game, channel, message);
			break;
		}
		case "set": case "setattr": {
			/*
			 * Set an existing attribute to the given score
			 * &set <value>[/maxvalue] <attribute> [character]
			 * Eg: &set hp 20
			 * Eg: &set stam 20/30 Bob
			 */
			
			if (!cmdParser.validateParameterLength(new String[] {"attribute", "value"}, "\b/max\\_value", "character")) {
				break;
			}

			CharProfile profile = getProfileOrSendError(args.length > 3 ? args[3] : null, game, member, channel);
			if (profile == null) {
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
			if (!cmdParser.validateParameterLength(new String[] {"attribute"}, "character")) {
				return;
			}

			CharProfile profile = getProfileOrSendError(args.length > 2 ? args[2] : null, game, member, channel);
			if (profile == null) {
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
		case "map": case "m": {
			mapCommandHandler.handleCommand(args, member, game, channel);
			break;
		}
		//TODO Delete these test cases
		case "guildid": {
			channel.sendMessage("The guild ID is: " + game.getGuild().getId()).queue();
			break;
		}
		}
		
		//Cases for "+" and "-" commands:
		switch (lowerCommand.charAt(0)) {
		case '+': case '-':
			addToAttributeCmd(args, member, channel, game);
			break;
		}
		
		if (lowerCommand.startsWith("addchar") || lowerCommand.startsWith("addcharacter")) {
			if (args.length <= 1 || content.split("\n").length <= 1) { // TODO Convert to use the cmdParser
				channel.sendMessage("Usage: `" + COMMAND_PREFIX + args[0] + "\nName: <name>\n<attribute_name>: <attribute_value>\n<attribute_name>: <attribute_value>\n...`").queue();
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
	
	private void claimProfileCmd(String[] args, RPGame game, MessageChannel channel, Message message) {
		CommandParser cmdParser = new CommandParser(args, (TextChannel) channel);
		if (!cmdParser.validateParameterLength(new String[] {"character"}, "@User")) {
			return;
		}

		Member member = getMemberOrSendError(message, channel, args.length > 2 ? args[2] : null);
		if (member == null) {
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
	
	private void unclaimProfileCmd(String[] args, RPGame game, MessageChannel channel, Message message) {
		if (channel == null) {
			return;
		}

		Member member = getMemberOrSendError(message, channel, args.length > 1 ? args[1] : null);
		if (member == null) {
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
			// TODO Refactor to use cmdParser
			channel.sendMessage("Usage: `" + COMMAND_PREFIX + args[OPERATOR_ARG] + " <attribute> [duration] [character]`").queue();
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
					//Profile was specified and duration is invalid. Send error
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

		profile = getProfileOrSendError(profileArg != INVALID_VAL ? args[profileArg] : null, game, member, channel);
		if (profile == null) {
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
			channel.sendMessage("Please add a number to the end of your operation! Eg. `" + COMMAND_PREFIX + "+1`").queue();
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
	 * Get the profile based on the profile name. If profileName is empty string or null, tries to find the profile claimed by the member.
	 * Sends an error message to the channel if no profile can be found.
	 * @param profileName Name of profile to find. Case-insensitive. If set to nul or empty string, uses member's profile if available
	 * @param game Game to get profile from
	 * @param member Member sending message
	 * @param channel Channel to send error message to
	 * @return Profile or null if none found
	 */
	private CharProfile getProfileOrSendError(String profileName, RPGame game, Member member, MessageChannel channel) {
		CharProfile profile;
		if (Util.isEmptyString(profileName)) {
			profile = game.getProfileRegistry().getProfile(member);
			if (profile == null) {
				channel.sendMessage("No character profile found!").queue();
			}
		} else {
			profile = game.getProfileRegistry().getProfile(profileName);
			if (profile == null) {
				channel.sendMessage("No character profile was found by the name of **" + profileName + "**!").queue();
			}
		}

		return profile;
	}

	/**
	 * Get the targeted member
	 * @param message The sent message
	 * @param channel The channel the message was sent from
	 * @param memberName The name of the member to get. If null then uses the sender of the message
	 * @return The targeted member or null if no member found
	 */
	private Member getMemberOrSendError(Message message, MessageChannel channel, String memberName) {
		Member member = null;
		if (!Util.isEmptyString(memberName)) {
			List<Member> mentionedMembers = message.getMentionedMembers();
			if (!mentionedMembers.isEmpty()) {
				member = mentionedMembers.get(0);
			}
		} else {
			member = message.getMember();
		}

		if (member == null) {
			channel.sendMessage("No Discord user was found by the name of **" + memberName + "**.").queue();
		}

		return member;
	}
}
