package com.rlee.discordbots.rpbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.regitstry.GameRegistry;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class RPBot {
	
	private static JDA jda;

	private static GameRegistry gameRegistry;

	private static String TOKEN;
	private static String commandPrefix;
	private static String activity = "A Roleplay Attempt";

	private static final String configDirectoryPath = "config";
	private static final String configFilePath = configDirectoryPath + "/config.yml";

	public static void main(String[] args) {
		readConfig();
		startJDA();
		setup();
	}

	/**
	 * Set the static vars TOKEN and commandPrefix based on config/config.yml file
	 */
	private static void readConfig() {
		File configDirectory = new File(configDirectoryPath);
		if (!configDirectory.exists() || !configDirectory.isDirectory()) {
			configDirectory.mkdirs();
		}
		
		File configFile = new File(configFilePath);
		ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
		try {	
			if (!configFile.exists()) {
				throw new FileNotFoundException("Please create a file named config/config.yml with the following lines within: "
						+ "token: \"YOUR DISCORD BOT TOKEN\""
						+ "prefix: \"YOUR COMMAND PREFIX HERE\""
						+ "activity: \"DISCORD BOT ACTIVITY HERE\"");
			}
			
			ObjectNode root = (ObjectNode) yamlMapper.readTree(configFile);
			TOKEN = yamlMapper.treeToValue(root.get("token"), String.class);
			commandPrefix = yamlMapper.treeToValue(root.get("prefix"), String.class);

			// Optional nodes
			JsonNode activityNode = root.get("activity");
			if (activityNode != null) {
				activity = yamlMapper.treeToValue(activityNode, String.class);
			}

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void startJDA() {
		try {
			//Instantiate JDA api
			JDABuilder builder = JDABuilder.createDefault(TOKEN);
			builder.setActivity(Activity.playing(activity));
			jda = builder.build().awaitReady();
			jda.addEventListener(new MessageListener(commandPrefix)); // Add event listener for messages
		} catch (LoginException | IllegalArgumentException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private static void setup() {
		gameRegistry = new GameRegistry();
		
		//Create game for every guild this bot is a member of
		List<Guild> guilds = jda.getGuilds();
		
		for (Guild guild : guilds) {
			gameRegistry.setGame(guild, new RPGame(gameRegistry, guild));
		}
	}
	
	/**
	 * @return the profileRegistry
	 */
	public static GameRegistry getGameRegistry() {
		return gameRegistry;
	}
	
	/**
	 * Convenience method to get a game from a guild
	 * @param guild
	 * @return Game by guild or null if no game set
	 *
	 * @author R Lee
	 */
	public static RPGame getGame(Guild guild) {
		return gameRegistry.getGame(guild);
	}
	
	/**
	 * Convenience method to get the user this bot is represented by
	 * @return
	 *
	 * @author R Lee
	 */
	public static User selfUser() {
		return jda.getSelfUser();
	}

}
