package com.rlee.discordbots.rpbot.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rlee.discordbots.rpbot.RPBot;
import com.rlee.discordbots.rpbot.exception.AttributeAlreadyExistsException;
import com.rlee.discordbots.rpbot.profile.Attribute;
import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.regitstry.ProfileRegistry;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class ProfileReader {
	
	private static final String NEW_LINE = "\n";
	private static final String DELIMITER = ":";
	private static final String MAX_DELIMITER = "/";
	
	public void readProfilesFromChannel(ProfileRegistry profileRegistry, TextChannel channel, int maxMessages) {
		int messageCount = 0;

		// Iterate over message history from newest to oldest entry
		for (Message message : channel.getIterableHistory()) {

			CharProfile profile = readProfileFromMessage(message, profileRegistry);
			
			//TODO: Automatically assign name for unnamed entries
			if (!isValidProfile(profile)) {
				continue; //Reading invalid entry
			}
			
			profileRegistry.addProfile(profile);
			
			if (++messageCount > maxMessages) {
				break; // Prevent excessive memory overflow for large history
			}
		}
	}

	/**
	 * Create a character profile from the given message
	 * @param message Source message for the profile. Must not be null
	 * @param registry ProfileRegistry used to access this profile later. Must not be null
	 * @return New character profile with attributes set to those found inside the message, or null if creation failed
	 *
	 * @author R Lee
	 */
	public CharProfile readProfileFromMessage(Message message, ProfileRegistry registry) {
		if (message == null) {
			return null;
		}
		
		CharProfile profile = readProfile(message.getContent(), registry);
		
		if (!isValidProfile(profile)) {
			return null;
		}
		
		// TODO Implement message-related support into readProfile()
		if (!message.getMentionedUsers().isEmpty()) {
			Guild guild = message.getGuild();

			if (guild != null) {
				// Get the first mentioned user, validate them as a member
				Member member = guild.getMember(message.getMentionedUsers().get(0));

				if (member != null && member.getUser() != RPBot.selfUser() && registry.getProfile(member) == null) {
					profile.getProfileRegistry().claimProfile(member, profile);
				}
			}
		}
		
		profile.setSourceMessage(message);
		
		return profile;
	}
	
	public void readProfilesFromDirectory(ProfileRegistry registry) throws FileNotFoundException{
		if (registry == null) {
			// TODO Report exception
			return;
		}
		
		final String guildId = registry.getGame().getGuild().getId();
		final String directoryPath = "games/" + guildId + "/profiles";
		File directory = new File(directoryPath);
		
		if (!directory.exists()) {
			throw new FileNotFoundException("No directory for " + registry.getGame().getGuild().getName() + " game set up");
		}
		
		File[] files = directory.listFiles();
		CharProfile profile;
		
		for (File file : files) {
			if (!file.getName().endsWith(".txt")) {	// TODO Add support for other file paths or DB entries
				continue;
			}
			
			profile = readProfileFromFile(file, registry);
			if (!isValidProfile(profile)) {
				continue;
			}
			
			registry.addProfile(profile);
		}
	}
	
	/**
	 * Create a character profile from the given file path
	 * @param filePath
	 * @param registry
	 * @return
	 * @throws FileNotFoundException
	 */
	private CharProfile readProfileFromFile(File file, ProfileRegistry registry) throws FileNotFoundException {
		if (file == null) {
			return null;
		}
		
		if (!file.exists()) {
			throw new FileNotFoundException();
		}
		
		final String NEW_LINE = "\n";
		
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(NEW_LINE); // Needed to support existing infrastructure for readProfile command
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return readProfile(sb.toString(), registry);
	}
	
	private CharProfile readProfile(String source, ProfileRegistry registry) {
		if (RPBot.isEmptyString(source) || registry == null) {
			return null;
		}
		
		final String NEW_LINE = "\n";
		
		CharProfile profile = new CharProfile(registry);
		String[] args = source.split(NEW_LINE);
		
		//TODO: Change to iterator (allow for insertion)
		for (String arg : args) {
			try {
				Attribute attribute = readAttribute(arg, profile);
				if (attribute != null) {
					profile.setAttribute(attribute.getName(), attribute);
				}
			} catch (AttributeAlreadyExistsException e) { 
				continue; //Ignore repeated entry
			} catch (NumberFormatException e) {
				// TODO Add support for non-numeric entries (i.e. migrate name detection to non-numeric attribute)
				checkReadName(arg, profile);
				checkReadMemberName(arg, profile);
			}
		}

		if (!isValidProfile(profile) || registry.containsName(profile.getName())) {
			return null;
		}
		
		if (profile.getMember() != null) {
			registry.getGame().saveProfile(profile); //Save the entire profile (instead of one that is cut off)
		}
		
		return profile;
	}
	
	public Attribute readAttribute(String line, CharProfile profile) throws NumberFormatException, AttributeAlreadyExistsException {
		final int KEY = 0;
		final int VAL = 1;
		
		// Ignore lines that have no delimiter character
		List<String> args = new ArrayList<String>(Arrays.asList(line.split(DELIMITER)));
		
		if (args.size() < 2) {
			return null; // Skip lines that haven't been split
		}
		
		if (args.get(KEY).isEmpty() || RPBot.replaceWhitespaces(args.get(VAL)).isEmpty()) {
			return null; // Skip invalid entries (eg. "Key : : 3")
			// For more than 2 args, simply ignore trailing args
			// during operations
		}

		// Remove leading/tailing whitespace, replace spaces in middle with
		// underscore, and make all chars lowercase on the key
		args.set(KEY, RPBot.replaceWhitespaces(args.get(KEY), true));
		// Do NOT edit innerArgs.get(VAL) until innerArgs[MAX_VAL] is
		// extracted, if applicable
		
		if (profile.getAttributes().containsKey(args.get(KEY))) {
			throw new AttributeAlreadyExistsException(args.get(KEY));
		}
		
		//Remove white spaces
		args.set(VAL, RPBot.replaceWhitespaces(args.get(VAL)));
		
		// Remove leading '+' symbols if applicable (lenient conversion from
		// string to int)
		if (args.get(VAL).charAt(0) == '+') {
			args.set(VAL, args.get(VAL).substring(1));
		}

		Attribute attribute = new Attribute(args.get(KEY));
		attribute.setProfile(profile);
		readAttributeValue(args.get(VAL), attribute);
		
		return attribute;
	}
	
	/**
	 * Check if the attribute value has a max value written into it, and if so,
	 * @param attributeValue
	 * @param attribute
	 */
	private void readAttributeValue(String attributeValue, Attribute attribute) throws NumberFormatException {
		final int VAL = 0;
		final int MAX_VAL = 1;
		
		String[] fractionArr = attributeValue.split(MAX_DELIMITER);
		boolean hasMaxValue = (fractionArr.length >= 2);
		
		// Attempt to insert numerical entry
		attribute.setValue(Integer.valueOf(fractionArr[VAL]), true);
		
		if (hasMaxValue && !fractionArr[MAX_VAL].isEmpty()) {
			try {
				// Attempt to set numerical entry on max attribute
				attribute.setMaxValue(Integer.valueOf(fractionArr[MAX_VAL]));
				attribute.setMinValue(0);
			} catch (NumberFormatException e) {
				// Ignore bad entry TODO Return exception
			}
		}
	}
	
	/**
	 * Check the input string and profile to see if a name should be applied
	 * If the profile already has a name, then this will do nothing
	 * 
	 * @deprecated KLUDGE method substitute for non-numeric attributes for profile reading
	 * 			There exists no better alternative yet
	 */
	@Deprecated
	private void checkReadName(String line, CharProfile profile) {
		final int KEY = 0;
		final int VAL = 1;
		
		if (profile.getName().isEmpty()) {
			//Manually parse out "name" field if applicable
			String[] args = line.split(DELIMITER);
			
			if (args.length >= 2
					&& args[KEY].equalsIgnoreCase("name") 
					&& !RPBot.isEmptyString(args[VAL])) {
				// The arg can be split by the delimiter
				// AND the arg starts with the word "name"
				// AND the value for the name is not empty
				
				profile.setName(RPBot.replaceWhitespaces(args[VAL]));
			}
		}
	}
	
	/**
	 * Check the input string and profile to see if a member should be applied
	 * This will do nothing if any of the following are true: <br/>
	 * - The line is not in the format of "player: [USERNAME]"
	 * - The username is not provided
	 * - There is more than one user with the given username and the discriminator (numbers following # character) is not provided
	 * - No user can be found for the given username on the guild
	 * @param line
	 * @param profile
	 *
	 * @deprecated KLUDGE method substitute for non-numeric attributes for profile reading
	 * 			There exists no better alternative yet
	 */
	@Deprecated
	private void checkReadMemberName(String line, CharProfile profile) {
		final int KEY = 0;
		final int NAME = 1;
		
		if (profile.getMember() != null) {
			return;
		}
		
		//Manually parse out "name" field if applicable
		String[] args = line.split(DELIMITER);
		if (args.length < 2 || !args[KEY].equalsIgnoreCase("player")) {
			return;
		}
		
		args[NAME] = args[NAME].trim();
		if (RPBot.isEmptyString(args[NAME])) {
			return;
		}
		
		String[] usernameArgs = args[NAME].split("#");
		if (usernameArgs.length <= 0 || RPBot.isEmptyString(usernameArgs[0])) {
			return;
		}
		boolean discriminatorProvided = usernameArgs.length >= 2 && !RPBot.isEmptyString(usernameArgs[1]);
		
		Guild guild = profile.getProfileRegistry().getGame().getGuild();
		List<Member> members = guild.getMembersByName(usernameArgs[0], true);
		Member member = null;
		
		if (members.size() == 1) {
			member = members.get(0);	// Select the first member
		} else if (members.size() > 1 && discriminatorProvided) {
			String discriminator = usernameArgs[1];
			for (Member iteratedMember : members) {
				if (iteratedMember.getUser().getDiscriminator().equals(discriminator)) {
					member = iteratedMember;
					break;
				}
			}
		} else {
			return;
		}
						
		if (member == null || profile.getProfileRegistry().getProfile(member) != null) {
			return;
		}
		
		profile.getProfileRegistry().claimProfile(member, profile);
	}
	
	public boolean isValidProfile(CharProfile profile) {
		return profile != null && profile.hasName() && !profile.getAttributes().isEmpty();
	}
}
