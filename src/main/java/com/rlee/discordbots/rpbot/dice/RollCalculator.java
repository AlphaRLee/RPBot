package com.rlee.discordbots.rpbot.dice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.rlee.discordbots.rpbot.RPBot;
import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.profile.Attribute;
import com.rlee.discordbots.rpbot.profile.NumberAttribute;
import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.regitstry.AliasRegistry;
import com.rlee.discordbots.rpbot.regitstry.ProfileRegistry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class RollCalculator {

	private static final int DEFAULT_RANGE = 20;
	
	/**
	 * Calculate the roll outcome and output it to the specified channel.
	 * Note: This command is only supported when sent through a guild with a registered game
	 * @param expression
	 * @param channel
	 * @param message
	 *
	 * @author R Lee
	 */
	public void compute(String expression, MessageChannel channel, Message message) {
		List<Integer> numbers = new LinkedList<>();
		User author = message.getAuthor();
		String sender = author.getAsMention();
		boolean inGame = channel instanceof TextChannel;
		boolean rollAttribute = false; // By default, set rollAttribute to be false

		if (!Util.isEmptyString(expression)) {
    		RPGame game = null;
    		CharProfile profile = null;
    		Map<String, NumberAttribute> quickAttributes = new HashMap<String, NumberAttribute>(); // Quick list used to reduce attribute-searching
    		
    		if (inGame) {
    			game = RPBot.getGame(((TextChannel) channel).getGuild()); // Get game from channel
    			
    			inGame = (game != null); // Validate game exists
    		}
    		
    		expression = expression.trim();

    		// FIXME: Refactor into a function
    		getProfile: if (inGame) {
    			rollAttribute = game.getRollConfig().getRollAttribute();

    			ProfileRegistry profileRegistry = game.getProfileRegistry();

    			// Based on how JDA caches guild members, members need to be loaded directly from the message itself
    			Member authorMember = message.getMember();
    			profile = profileRegistry.getProfile(authorMember);

    			int lastIndex = expression.lastIndexOf(' ');
    			
    			if (lastIndex == -1) {
    				break getProfile; // Char does not occur
    			}
    			
    			String endArg = expression.substring(lastIndex + 1);
    			
    			// Try computing the arg. Search for empty array as indicator this is not a valid expression
    			if (!computeArg(endArg, endArg.startsWith("\\-"), profile, rollAttribute, false, false, game.getAliasRegistry(), quickAttributes).isEmpty()) {
    				break getProfile;
    			}
    			
    			profile = getProfile(endArg, profileRegistry);
    			
    			if (profile != null) {
    				expression = expression.substring(0, lastIndex); // Get rid of profile name from rest of expression if profile was successfully found
    				sender = profile.getName();	
    			}
    		}
    		
    		// Remove all whitespaces and split everything by the "+" character (will split by "-" later)
    		// List<String> args = new ArrayList<String>(Arrays.asList(expression.replaceAll(" ", "").split("\\+")));
    		String[] args = expression.replaceAll(" ", "").split("\\+");
    		AliasRegistry aliasRegistry = (inGame ? game.getAliasRegistry() : null);
   
    		for (int i = 0; i < args.length; i++) {
    			String[] innerArgs = args[i].split("\\-"); // Split along "-" character
    			
    			if (i == 0) {
    				// Append default roll in front of first attr roll if applicable
    				numbers.addAll(computeArg(innerArgs[0], false, profile, rollAttribute, true, true, aliasRegistry, quickAttributes));
    			} else {
    				// Not first roll, do not attach default roll
    				numbers.addAll(computeArg(innerArgs[0], false, profile, rollAttribute, aliasRegistry, quickAttributes));
    			}
    			
    			// Handle negative numbers
    			for (int j = 1; j < innerArgs.length; j++) {
    				numbers.addAll(computeArg(innerArgs[j], true, profile, rollAttribute, aliasRegistry, quickAttributes));
    			}
    		}
    		
		} else {
			// Empty string
			numbers.add(rollDefaultDie());
		}
    		
		print(numbers, channel, sender);
	}
	
	/**
	 * Parse a roll arg and get a list of numbers carrying all roll values.
	 * Will pad invalid entries with 0
	 * @param arg
	 * @param isNegative Set to true to have all returned values as negative numbers
	 * @return List of numbers where entries are ordered by parsing.
	 *
	 * @author R Lee
	 */
	private List<Integer> computeArg(String arg, boolean isNegative, CharProfile profile, boolean rollAttribute, 
			AliasRegistry aliasRegistry, Map<String, NumberAttribute> quickAttributes) {
		return computeArg(arg, isNegative, profile, rollAttribute, false, true, aliasRegistry, quickAttributes);
	}
	
	/**
	 * Parse a roll arg and get 
	 * @param arg
	 * @param isNegative
	 * @param profile
	 * @param rollAttribute Set to true to consider any attributes found as a dice with the number of faces equal to the attribute value
	 * @param padZero If the output cannot be computed, insert a 0 into the final expression
	 * @param prependDefault If true and arg is an attribute name, will insert new entry of default roll at the beginning of the list
	 * @return
	 *
	 * @author R Lee
	 */
	private List<Integer> computeArg(String arg, boolean isNegative, CharProfile profile, boolean rollAttribute, boolean prependDefault, boolean padZero,
			AliasRegistry aliasRegistry, Map<String, NumberAttribute> quickAttributes) {
		if (Util.isEmptyString(arg)) {
			return new LinkedList<Integer>();
		}
		
		List<Integer> numbers = rollDice(arg, isNegative);
		if (!numbers.isEmpty()) {
			// Dice expression successfully rolled, terminate here
			return numbers;
		}
		
		if (prependDefault) {
			numbers.add(rollDefaultDie()); // Inject a default roll if this is the only roll and not a dice expression
		}
		
		String name = arg.toLowerCase();
		Attribute<?> attribute = null;
		
		if (quickAttributes.containsKey(name)) {
			attribute = quickAttributes.get(name);
		} else if (profile != null && aliasRegistry != null) {
			attribute = aliasRegistry.getAttribute(name, profile);
			
			if (attribute != null) {
				// Valid attribute, store the value
				// FIXME: Remove hardcoded cast
				if (attribute instanceof NumberAttribute) {
					quickAttributes.put(name, (NumberAttribute) attribute);
				}
			}
		}

		// FIXME: Get rid of instanceof restriction
		if (attribute != null && attribute instanceof NumberAttribute) {
			NumberAttribute numberAttribute = (NumberAttribute) attribute;
			int attrVal = numberAttribute.getValue();
			
			// Valid attribute, perform roll check and negative check
			if (rollAttribute) {
				attrVal = getRandInt(attrVal);
			}
			
			if (isNegative) {
				attrVal *= -1;
			}
			
			numbers.add(attrVal);
			
			if (numberAttribute.hasItemEffects()) { // Apply item effects
				numbers.add(numberAttribute.getItemEffectsSum());
			}
			
			if (numberAttribute.hasBuff()) { // Apply buffs
				numbers.add(numberAttribute.getBuff());
				numberAttribute.decrementBuffDuration(true);
			}
		} else if (padZero) {
			numbers.add(0); // Add an empty value to serve as a cue to the end user
							// Only add zero if requested
		}
		
		return numbers;
	}
	
	/**
	 * Roll a single die with the default number of faces
	 * @return
	 *
	 * @author R Lee
	 */
	private int rollDefaultDie() {
		return getRandInt(DEFAULT_RANGE);
	}
	
	/**
	 * Roll a die/dice based on a given dice expression
	 * @param diceExpression Standard dice expression (eg. d20, 3d5)
	 * @param isNegative If set to true, all outputted numbers will be negative
	 * @return Random values based on diceExpression where entries are individual dice rolls,
	 * 	or empty list if input is invalid.
	 *
	 * @author R Lee
	 */
	private List<Integer> rollDice(String diceExpression, boolean isNegative) {
		final String DICE_CHAR = "d";
		final int COUNT_ARG = 0; // Number of dice to roll
		final int RANGE_ARG = 1; // Number of faces on dice
		
		List<Integer> values = new LinkedList<Integer>();
		String[] args = diceExpression.split(DICE_CHAR);
		int maxCount = 1;
		int range = 0;
		int output;
		
		if (args.length < 1 || args.length > 2) {
			return values;
		}
		
		// Try getting the max count, if requested
		if (!Util.isEmptyString(args[COUNT_ARG])) {
    		try {
    			maxCount = Integer.parseInt(args[COUNT_ARG]);
    		} catch (NumberFormatException e) {
    			return values;
    		}
		}
		
		if (args.length <= 1) {
			output = maxCount; // Interpreting expression like !roll d20 + 3, grabbing the 3
			values.add((isNegative ? output * -1 : output));
			return values;
		}

		// Try getting the range
		try {
			range = Integer.parseInt(args[RANGE_ARG]);
		} catch (NumberFormatException e) {
			return values;
		}
		
		if (range < 1) {
			return values;
		}
		
		for (int i = 0; i < maxCount; i++) {
			output = getRandInt(range);
			values.add((isNegative ? output * -1 : output));
		}
		
		return values;
	}
	
	/**
	 * Get a random int between 1 and range (inclusive)
	 * @param range
	 * @return
	 *
	 * @author R Lee
	 */
	private int getRandInt(int range) {
		return range > 0 ? ThreadLocalRandom.current().nextInt(range) + 1 : 0;
	}
	
	private CharProfile getProfile(String name, ProfileRegistry registry) {
		if (registry == null || Util.isEmptyString(name)) {
			return null;
		}
		
		return registry.getProfile(name);
	}
	
	private int getSum(List<Integer> numbers) {
		int sum = 0;
		
		for (int i : numbers) {
			sum += i;
		}
		
		return sum;
	}
	
	private void print(List<Integer> numbers, MessageChannel channel, String sender) {
		 // Start message with sender name, or self mention
		String output = (sender != null && !sender.isEmpty() ? sender : RPBot.selfUser().getAsMention());
		output += " rolled **" + getSum(numbers) + "**.";
		
		if (numbers.size() > 1) {
			output += " (";
		
    		for (int i : numbers) {
    			output += i + " + "; 
    		}
    		
    		output = output.substring(0, output.length() - 3); // Get rid of " + " on the end
    		output += ")";
		}
		
		channel.sendMessage(output).queue();
	}
}
