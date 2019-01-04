package com.rlee.discordbots.rpbot.command;

import com.rlee.discordbots.rpbot.MessageListener;

import com.rlee.discordbots.rpbot.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.Random;

public class CommandParser {

	private String[] args;
	private TextChannel channel;	

	private String lastUsageMessage;
	private String errorDescription;

	public CommandParser(String[] args, TextChannel channel) {
		this.args = args;
		this.channel = channel;

		lastUsageMessage = "";
		errorDescription = null;
	}

	public String getLastUsageMessage() {
		return lastUsageMessage;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * Set the description message that will accompany the error.
	 * Set to null or empty string to omit
	 * @param errorDescription
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	/**
	 * Print an error message to the text channel indicating the user has made an error.
	 * Commonly used to indicate proper usage, with the error description following if set.
	 * @param errorMessage
	 */
	public void sendUserError(String errorMessage) {
		String errMsg = Util.isEmptyString(errorMessage) ? "" : errorMessage;
		String errDesc = Util.isEmptyString(errorDescription) ? "" : errorDescription;

		String[] authorDescription = {
				"the wise",
				"the helpful",
				"did you try turning it off and on again?",
				"please try again",
				"the confused",
				"Sorry, my English isn't very good. Do you speak binary?",
				"Don't blame it on me, blame it on the RNG!"
		};

		EmbedBuilder msgBuilder = new EmbedBuilder();
		msgBuilder.setColor(Color.RED);
		msgBuilder.setFooter("RP Bot - " + authorDescription[new Random().nextInt(authorDescription.length)], null);
		msgBuilder.setTitle(errMsg, null);
		msgBuilder.setDescription(errDesc);
		channel.sendMessage(msgBuilder.build()).queue();
	}

	/**
	 * Validate that the number or args matches the requested length.
	 * If arg length is not met, then an error message is sent with the parameters demanded.
	 * If the first character in a parameter is the '\b' backspace escape character, then it is omitted and the
	 * whitespace character immediately proceeding it is removed.<br/>
	 * E.g. an optionalParameters array looking like: ["name", "gender, "\bage"] would look like: <br/>
	 * [name] [gender][age]
	 * The subCommands will always be set to null.
	 * @param requiredParameters parameters expected by inputted command for proper execution, excluding the command name itself. Set to null to exclude
	 * @param optionalParameters parameters accepted by inputted command for execution
	 * @return True if valid, false if not
	 */
	public boolean validateParameterLength(String[] requiredParameters, String... optionalParameters) {
		return validateParameterLength(
				(String[]) null,
				requiredParameters,
				optionalParameters);
	}

	/**
	 * Validate that the number or args matches the requested length.
	 * If arg length is not met, then an error message is sent with the parameters demanded.
	 * If the first character in a parameter is the '\b' backspace escape character, then it is omitted and the
	 * whitespace character immediately proceeding it is removed.<br/>
	 * E.g. an optionalParameters array looking like: ["name", "gender, "\bage"] would look like: <br/>
	 * [name] [gender][age]
	 * @param subCommand The first parameter expected by the inputted command, must be typed exactly as read. Set to null to exclude
	 * @param requiredParameters parameters expected by inputted command for proper execution, excluding the command name itself. Set to null to exclude
	 * @param optionalParameters parameters accepted by inputted command for execution
	 * @return True if valid, false if not
	 */
	public boolean validateParameterLength(String subCommand, String[] requiredParameters, String... optionalParameters) {
		return validateParameterLength(
				subCommand == null ? null : new String[] {subCommand},
				requiredParameters,
				optionalParameters);
	}

	 /**
	  * Validate that the number or args matches the requested length.
	  * If arg length is not met, then an error message is sent with the parameters demanded.
	  * If the first character in a parameter is the '\b' backspace escape character, then it is omitted and the 
	  * whitespace character immediately proceeding it is removed.<br/>
	  * E.g. an optionalParameters array looking like: ["name", "gender, "\bage"] would look like: <br/>
	  * [name] [gender][age]
	  * @param subCommands parameters expected by the inputted command, must be typed exactly as read. Set to null to exclude
	  * @param requiredParameters parameters expected by inputted command for proper execution, excluding the command name itself. Set to null to exclude
	  * @param optionalParameters parameters accepted by inputted command for execution
	  * @return True if valid, false if not
	  */
	 public boolean validateParameterLength(String[] subCommands, String[] requiredParameters, String... optionalParameters) {
		boolean result = false;

		boolean hasSubCommands = subCommands != null;
		boolean hasRequiredParams = requiredParameters != null;
		int requiredLength = (hasSubCommands ? subCommands.length : 0) + (hasRequiredParams ? requiredParameters.length : 0);
		if (args.length > requiredLength) {
			//More args than parameters (i.e. correct number of parameters following command name)
			result = true; //Happy path

			if (hasSubCommands && result) {
				for (String subCommand : subCommands) {
					if (Util.isEmptyString(subCommand)) {
						result = false; //Whoops, unhappy path found again
						break;
					}
				}
			}

			// Repeat check for subcommands
			if (hasRequiredParams && result) {
				for (String arg : requiredParameters) {
					if (Util.isEmptyString(arg)) {
						result = false; //Whoops, unhappy path found again
						break;
					}
				}
			}
		}

		lastUsageMessage = buildUsageErrorMessage(subCommands, requiredParameters, optionalParameters);
		if (!result) {
			sendUserError(lastUsageMessage);
		}
		return result;
	}

	private String buildUsageErrorMessage(String[] subCommands, String[] requiredParameters, String[] optionalParameters) {
		StringBuilder errorMessage = new StringBuilder("Usage: ").append(MessageListener.COMMAND_PREFIX).append(args[0]);
		if (subCommands != null) {
			for (String subCommand : subCommands) {
				errorMessage.append(" " + subCommand);
			}
		}

		if (requiredParameters != null) {
			for (String reqParam : requiredParameters) {
				errorMessage.append(formatParameter(reqParam, '<', '>'));
			}
		}

		for (String opParam: optionalParameters) {
			errorMessage.append(formatParameter(opParam, '[', ']'));
		}

		return errorMessage.toString();
	}

	/**
	 * Format the inputed parameter to either look like
	 * " (parameter)"
	 * or
	 * "(parameter)"
	 * The former option is the default, the latter option is used if the first character in param is '\b'.
	 * The round braces ( ) can be replaced with any characters, specified by the openBrace and closeBrace
	 * @param parameter The parameter to format
	 * @param openBrace The open brace character to use
	 * @param closeBrace The close brace character to use
	 * @return the parameter after formatting
	 */
	private String formatParameter(String parameter, char openBrace, char closeBrace) {
		if (Util.isEmptyString(parameter)) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder(parameter);
		final char backCharacter = '\b';
			
		sb = (sb.charAt(0) == backCharacter) 
				? sb.replace(0, 1, "").insert(0, openBrace) //sb without backspace character
				: sb.insert(0, " ").insert(1, openBrace);	//sb with " (" in the front
				
		return sb.append(closeBrace).toString();
	}
}
