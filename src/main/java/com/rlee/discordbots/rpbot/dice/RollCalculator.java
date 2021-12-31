package com.rlee.discordbots.rpbot.dice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
	// Choose a small recursion limit because recursive ExpressionAttributes like "&setattr rec rec+rec" has an O(n^m) size
	private static final int RECURSION_LIMIT = 10;

	// Delimiters separating terms in an expression
	private static List<String> termDelimiters = Arrays.asList("+", "-");

	/**
	 * Calculate the roll outcome and output it to the specified channel.
	 * Note: This command is only supported when sent through a guild with a registered game
	 * @param expression The roll expression (without the &roll command). Eg. in "&roll d6 - 2d2 + str Bob", the expression "d6 - 2d2 + str" has the terms "d6", "2d2" and "str"
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
    		Map<String, String> cachedAliases = new HashMap<>(); // Quick list used to reduce alias-searching FIXME: Move to class member or delete outright

    		if (inGame) {
    			game = RPBot.getGame(((TextChannel) channel).getGuild()); // Get game from channel
    			
    			inGame = (game != null); // Validate game exists
    		}
    		
    		expression = expression.trim();

    		if (inGame) {
				rollAttribute = game.getRollConfig().getRollAttribute();
				String explicitProfileName = getExplicitProfileNameInExpression(expression);
				// Get the profile from the message (if explicitProfileName is null, use sender's profile)
				profile = getProfileFromExpression(explicitProfileName, game, message);

				if (explicitProfileName != null) {
					// Get rid of profile name from rest of expression if profile was successfully found
					expression = expression.substring(0, expression.length() - explicitProfileName.length() - 1);
				}

				if (profile != null) {
					sender = profile.getName();
				}
			}

    		// Remove all whitespaces
			expression = expression.replaceAll(" ", "");
    		numbers.addAll(computeExpression(expression, profile, rollAttribute, inGame ? game.getAliasRegistry() : null, cachedAliases, RECURSION_LIMIT));
		} else {
			// Empty string
			numbers.add(rollDefaultDie());
		}
    		
		print(numbers, channel, sender);
	}

	/**
	 * Get the profile name from the expression.
	 * Profile name does not necessarily have a valid corresponding profile.
	 * Last arg of expression is assumed to be a profile name if:
	 *   - The expression has at least one space ' ' char
	 *   - The second last term is not an empty string
	 *   - The second last term does not end with a term delimiter character ('+' or '-')
	 * @param expression
	 * @return The profile name or null if conditions not met
	 */
	private String getExplicitProfileNameInExpression(String expression) {
		// Get roll command args
		String[] args = expression.trim().split(" ");

		// Test that args is long enough to contain a profile
		// Eg. expression "str Bob" is long enough, expressions "str" and "Bob" are too short
		if (args.length < 2) {
			return null;
		}

		// Test preceding arg to distinguish between "str + Bob" and "str Bob"
		// If a term delimiter character is at the end of the preceding term then assume endArg is actually a term in the expression
		String secondLastArg = args[args.length - 2];

		if (Util.isEmptyString(secondLastArg)) {
			return null;
		}

		String lastChar = Character.toString(secondLastArg.charAt(secondLastArg.length() - 1));
		if (termDelimiters.contains(lastChar)) {
			return null;
		}

		// Return last arg as profile name
		return args[args.length - 1];
	}

	/**
	 * Get the profile (either implied or explicit)
	 * @param explicitName Profile name to use. If set to null then return the implied profile (i.e. the profile of the message sender)
	 * @param game The game to find the profile in
	 * @param message The message the expression is sent from
	 * @return The profile used
	 */
	private CharProfile getProfileFromExpression(String explicitName, RPGame game, Message message) {
		ProfileRegistry profileRegistry = game.getProfileRegistry();

		if (explicitName == null) {
			// Default the profile to be the message sender's profile
			// Based on how JDA caches guild members, members need to be loaded directly from the message itself
			Member authorMember = message.getMember();
			return profileRegistry.getProfile(authorMember);
		} else {
			return profileRegistry.getProfile(explicitName);
		}
	}

	private List<Integer> computeExpression(String expression, CharProfile profile, boolean rollAttribute, AliasRegistry aliasRegistry, Map<String, String> cachedAliases, int recursionCount) {
		List<Integer> numbers = new LinkedList<>();

		// If recursion counter has hit limit, then return a zero to queue user
		if (recursionCount <= 0) {
			numbers.add(0);
			return numbers;
		}

		// Split everything by the term delimiter "+" character (will split by "-" later)
		String[] args = expression.split("\\+");

		for (int i = 0; i < args.length; i++) {
			String[] innerArgs = args[i].split("\\-"); // Split along term delimiter "-" character

			if (i == 0) {
				// Append default roll in front of first attr roll if applicable
				numbers.addAll(computeArg(innerArgs[0], false, profile, rollAttribute, true, true, aliasRegistry, cachedAliases));
			} else {
				// Not first roll, do not attach default roll
				numbers.addAll(computeArg(innerArgs[0], false, profile, rollAttribute, aliasRegistry, cachedAliases));
			}

			// Handle negative numbers
			for (int j = 1; j < innerArgs.length; j++) {
				numbers.addAll(computeArg(innerArgs[j], true, profile, rollAttribute, aliasRegistry, cachedAliases));
			}
		}

		return numbers;
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
			AliasRegistry aliasRegistry, Map<String, String> cachedAliases) {
		return computeArg(arg, isNegative, profile, rollAttribute, false, true, aliasRegistry, cachedAliases);
	}
	
	/**
	 * Parse a single term (arg) in a roll expression and get number values from rolled dice
	 * A term is defined as anything in between a + or a - sign in a roll expression.
	 *   Eg. in "&roll d6 - 2d2 + str Bob", the expression "d6 - 2d2 + str" has the terms "d6", "2d2" and "str"
	 * @param arg The term to parse.
	 * @param isNegative Whether to add or subtract the values from this term
	 * @param profile Profile to roll attributes for. If arg is not a simple dice expression, then this must not be null.
	 * @param rollAttribute Set to true to consider any attributes found as a dice with the number of faces equal to the attribute value
	 * @param padZero If the output cannot be computed, insert a 0 into the final expression
	 * @param prependDefault If true and arg is an attribute name, will insert new entry of default roll at the beginning of the list
	 * @return
	 *
	 * @author R Lee
	 */
	private List<Integer> computeArg(String arg, boolean isNegative, CharProfile profile, boolean rollAttribute, boolean prependDefault, boolean padZero,
			AliasRegistry aliasRegistry, Map<String, String> cachedAliases) {
		if (Util.isEmptyString(arg)) {
			return new LinkedList<>();
		}

		// Try interpreting arg as a simple dice or plain number expression (e.g. d20, 2d6, 3)
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

		if (profile != null) {
			if (cachedAliases.containsKey(name)) {
				attribute = profile.getAttribute(cachedAliases.get(name));
			} else if (aliasRegistry != null) {
				attribute = aliasRegistry.getAttribute(name, profile);

				if (attribute != null) {
					// Valid attribute name, cache the value
					cachedAliases.put(name, attribute.getName());
				}
			}
		}

		if (attribute != null) {
			if (attribute instanceof NumberAttribute) {
				numbers.addAll(computeNumberAttribute((NumberAttribute) attribute, isNegative, rollAttribute));
			}

		} else if (padZero) {
			numbers.add(0); // Add an empty value to serve as a queue to the end user
							// Only add zero if requested
		}
		
		return numbers;
	}

	private List<Integer> computeNumberAttribute(NumberAttribute attribute, boolean isNegative, boolean rollAttribute) {
		List<Integer> numbers = new LinkedList<>();

		// Valid attribute, apply roll and negative
		int attrVal = attribute.getValue();
		if (rollAttribute) {
			attrVal = getRandInt(attrVal);
		}

		if (isNegative) {
			attrVal *= -1;
		}

		numbers.add(attrVal);

		if (attribute.hasItemEffects()) { // Apply item effects
			numbers.add(attribute.getItemEffectsSum());
		}

		if (attribute.hasBuff()) { // Apply buffs
			numbers.add(attribute.getBuff());
			attribute.decrementBuffDuration(true);
		}

		return numbers;
	}

	/**
	 * Roll a single die with the default number of faces
	 * @return A number between 1 - DEFAULT_RANGE
	 *
	 * @author R Lee
	 */
	private int rollDefaultDie() {
		return getRandInt(DEFAULT_RANGE);
	}
	
	/**
	 * Roll a die/dice based on a given dice expression
	 * @param diceExpression Standard dice expression term (eg. d20, 3d5)
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
		
		List<Integer> values = new LinkedList<>();
		String[] args = diceExpression.split(DICE_CHAR);
		int diceCount = 1; // Number of dice to roll. Default to 1 if not given
		int range = 0;
		int output;
		
		if (args.length < 1 || args.length > 2) {
			return values;
		}
		
		// Try parsing the diceCount, if requested
		if (!Util.isEmptyString(args[COUNT_ARG])) {
    		try {
    			diceCount = Integer.parseInt(args[COUNT_ARG]);
    		} catch (NumberFormatException e) {
    			return values;
    		}
		}
		
		if (args.length <= 1) {
			output = diceCount; // Interpreting expression like &roll d20 + 3 or &roll 3, grabbing the 3
			values.add(isNegative ? output * -1 : output);
			return values;
		}

		// Try getting the range
		try {
			range = Integer.parseInt(args[RANGE_ARG]);
		} catch (NumberFormatException e) {
			return values;
		}

		// Reject dice rolls with 0 or less faces on dice
		if (range < 1) {
			return values;
		}
		
		for (int i = 0; i < diceCount; i++) {
			output = getRandInt(range);
			values.add(isNegative ? output * -1 : output);
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

	private int getSum(List<Integer> numbers) {
		return numbers.stream().reduce(0, Integer::sum);
	}
	
	private void print(List<Integer> numbers, MessageChannel channel, String sender) {
		 // Start message with sender name, or self mention
		String output = (!Util.isEmptyString(sender) ? sender : RPBot.selfUser().getAsMention());
		output += " rolled **" + getSum(numbers) + "**.";
		
		if (numbers.size() > 1) {
			output += " (";
			output += numbers.stream().map(Object::toString).collect(Collectors.joining(" + "));
    		output += ")";
		}
		
		channel.sendMessage(output).queue();
	}
}
