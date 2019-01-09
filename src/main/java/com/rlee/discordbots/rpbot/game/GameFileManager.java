package com.rlee.discordbots.rpbot.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.profile.ProfilePrinter;
import com.rlee.discordbots.rpbot.reader.ProfileReader;

public class GameFileManager {

	private RPGame game;
	private final String gameDirectoryPath;
	private final String configFilePath;
	
	public GameFileManager(RPGame game) {
		this.game = game;
		gameDirectoryPath = "games/" + game.getGuild().getId();
		configFilePath = gameDirectoryPath + "/config.yml";
	}

	/**
	 * Check if the required files exist.
	 * If not, create them.
	 */
	void checkCreateFiles() {
		File gameDirectory = new File(gameDirectoryPath);
		if (!gameDirectory.exists() || !gameDirectory.isDirectory()) {
			gameDirectory.mkdirs();
		}

		File profilesDirectory = new File(gameDirectoryPath + "/profiles");
		if (!profilesDirectory.exists() || !profilesDirectory.isDirectory()) {
			profilesDirectory.mkdirs();
		}

		checkCreateConfigFile();
	}
	
	void loadProfiles() {
		ProfileReader profileReader = new ProfileReader();
		try {
			profileReader.readProfilesFromDirectory(game.getProfileRegistry());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Load configuration settings for the particular game
	 */
	void loadConfig() {
		// Presently only maps have config
		try {
			System.out.println("Now reading: " + configFilePath); // FIXME delete test code
			game.getMapConfig().readMapConfigFromFile(configFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void saveProfile(CharProfile profile) throws IllegalArgumentException {
		if (hasIllegalCharacters(profile.getName())) {
			throw new IllegalArgumentException("Invalid characters found inside profile name");
		}
		
		ProfilePrinter profilePrinter = new ProfilePrinter();
		File file = new File(gameDirectoryPath + "/profiles/" + profile.getName() + ".txt");
		profilePrinter.writeProfileToFile(profile, file);
	}
	
	void deleteProfile(CharProfile profile) throws IllegalArgumentException {
		if (profile == null) {
			return;
		}
		
		if (hasIllegalCharacters(profile.getName())) {
			throw new IllegalArgumentException("Invalid characters found inside profile name");
		}
		
		File file = new File(gameDirectoryPath + "/profiles/" + profile.getName() + ".txt");
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * Check if the required config file exists.
	 * If not, create a default config file for the game
	 */
	private void checkCreateConfigFile() {
		File configFile = new File(configFilePath);
		if (!configFile.exists()) {
			try {
				Files.copy(new File("config/defaultGameConfig.yml").toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks for illegal character names embedded within a file name
	 * @param fileName
	 * @return
	 */
	private boolean hasIllegalCharacters(String fileName) {
		String[] illegalStrings = {
				".exe", ".bat", ".command", ".start", ".jpg", ".jpeg", ".png", ".gif", ".sh", "/", "../", "~"
		};

		String lowercaseName = fileName.toLowerCase();
		for (String illegalString : illegalStrings) {
			if (lowercaseName.contains(illegalString)) {
				return true;
			}
		}

		return false;
	}
}
