package com.rlee.discordbots.rpbot.game;

import java.io.File;
import java.io.FileNotFoundException;

import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.profile.ProfilePrinter;
import com.rlee.discordbots.rpbot.reader.ProfileReader;

public class GameFileManager {

	private RPGame game;
	private final String gameDirectoryPath;
	
	public GameFileManager(RPGame game) {
		this.game = game;
		gameDirectoryPath = "games/" + game.getGuild().getId();
	}
	
	void checkCreateFiles() {
		File gameDirectory = new File(gameDirectoryPath);
		if (!gameDirectory.exists() || !gameDirectory.isDirectory()) {
			gameDirectory.mkdirs();
		}
		
		File profilesDirectory = new File(gameDirectoryPath + "/profiles");
		if (!profilesDirectory.exists() || !profilesDirectory.isDirectory()) {
			profilesDirectory.mkdirs();
		}
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
	
	void saveProfile(CharProfile profile) throws IllegalArgumentException {
		if (profile.getName().contains(".exe") || profile.getName().contains(".bat") || profile.getName().contains("../")) {
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
		
		if (profile.getName().contains(".exe") || profile.getName().contains(".bat") || profile.getName().contains("../")) {
			throw new IllegalArgumentException("Invalid characters found inside profile name");
		}
		
		File file = new File(gameDirectoryPath + "/profiles/" + profile.getName() + ".txt");
		if (file.exists()) {
			file.delete();
		}
	}
}
