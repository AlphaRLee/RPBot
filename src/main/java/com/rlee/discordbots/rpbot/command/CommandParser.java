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
	 * Print an error message to the text channel showing proper usage, with the error description following if set.
	 * @param errorMessage
	 */
	public void sendUsageError(String errorMessage) {
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
	  * @param requiredParameters parameters expected by inputed command for proper execution, excluding the command name itself
	  * @param optionalParameters parameters accepted by inputed command for execution
	  * @return True if valid, false if not
	  */
	public boolean validateParameterLength(String[] requiredParameters, String... optionalParameters) {
		boolean result = false;
		if (args.length > requiredParameters.length) {
			//More args than parameters (i.e. correct number of parameters following command name)
			result = true; //Happy path
		}

		lastUsageMessage = buildUsageErrorMessage(requiredParameters, optionalParameters);
		if (!result) {
			sendUsageError(lastUsageMessage);
		}
		return result;
	}

	private String buildUsageErrorMessage(String[] requiredParameters, String[] optionalParameters) {
		StringBuilder errorMessage = new StringBuilder("Usage: ").append(MessageListener.COMMAND_PREFIX).append(args[0]);
		for (String reqParam : requiredParameters) {
			errorMessage.append(formatParameter(reqParam, '<', '>'));
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
