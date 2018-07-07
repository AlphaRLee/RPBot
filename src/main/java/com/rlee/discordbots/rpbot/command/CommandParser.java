package com.rlee.discordbots.rpbot.command;

import com.rlee.discordbots.rpbot.MessageListener;
import com.rlee.discordbots.rpbot.RPBot;

import net.dv8tion.jda.core.entities.TextChannel;

public class CommandParser {

	private String[] args;
	private TextChannel channel;	
	
	public CommandParser(String[] args, TextChannel channel) {
		this.args = args;
		this.channel = channel;
	}
	
	/**
	 * Print an error message to the text channel
	 * @param errorMessage
	 */
	private void sendError(String errorMessage) {
		channel.sendMessage(errorMessage).queue();
	}
	
	 /**
	  * Validate that the number or args matches the requested length.
	  * If arg length is not met, then an error message is sent with the parameters demanded.
	  * If the first character in a parameter is the '\b' backspace escape character, then it is omitted and the 
	  * whitespace character immediately proceeding it is removed.<br/>
	  * E.g. an optionalParameters array looking like: ["name", "gender, "\bage"] would look like: <br/>
	  * [name] [gender][age]
	  * @param length Minimum number of args
	  * @param requiredParameters parameters expected by inputed command for proper execution, excluding the command name itself
	  * @param optionalParameters parameters accepted by inputed command for execution
	  * @return True if valid, false if not
	  */
	public boolean validateParameterLength(String[] requiredParameters, String... optionalParameters) {
		if (args.length > requiredParameters.length) {
			//More args than parameters (i.e. correct number of parameters following command name)
			return true; //Happy path
		}
		
		StringBuilder errorMessage = new StringBuilder("Format: ").append(MessageListener.COMMAND_PREFIX).append(args[0]);
		for (String reqParam : requiredParameters) {
			errorMessage.append(formatParameter(reqParam, '<', '>'));
		}
		
		for (String opParam: optionalParameters) {
			errorMessage.append(formatParameter(opParam, '[', ']'));
		}
		
		sendError(errorMessage.toString());	
		return false;
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
		if (RPBot.isEmptyString(parameter)) {
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
