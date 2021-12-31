package com.rlee.discordbots.rpbot.game;

import com.rlee.discordbots.rpbot.dice.RollConfig;
import com.rlee.discordbots.rpbot.map.RPMapConfig;
import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.regitstry.AliasRegistry;
import com.rlee.discordbots.rpbot.regitstry.GameRegistry;
import com.rlee.discordbots.rpbot.regitstry.MapRegistry;
import com.rlee.discordbots.rpbot.regitstry.ProfileRegistry;
import net.dv8tion.jda.api.entities.Guild;

public class RPGame {

	private GameFileManager gameFileManager;
	private RollConfig rollConfig;
	private RPMapConfig mapConfig;

	private GameRegistry gameRegistry;
	private ProfileRegistry profileRegistry;
	private AliasRegistry aliasRegistry;
	private MapRegistry mapRegistry;

	private Guild guild;

	public RPGame(GameRegistry gameRegistry, Guild guild) {
		setup(gameRegistry, guild);
	}
	
	private void setup(GameRegistry gameRegistry, Guild guild) {
		this.gameRegistry = gameRegistry;
		this.guild = guild;
		profileRegistry = new ProfileRegistry(this);
		aliasRegistry = new AliasRegistry(this);
		mapRegistry = new MapRegistry(this);
		gameFileManager = new GameFileManager(this);
		rollConfig = gameFileManager.createRollConfig();
		mapConfig = gameFileManager.createMapConfig();

		gameFileManager.checkCreateFiles();
		gameFileManager.loadProfiles();
		gameFileManager.loadConfig();
	}
	
	public GameRegistry getGameRegistry() {
		return gameRegistry;
	}
	
	public Guild getGuild() {
		// TODO: Refactor to get guild object from ID (https://github.com/DV8FromTheWorld/JDA#entity-lifetimes)
		//		https://github.com/DV8FromTheWorld/JDA/releases/tag/v4.2.0
		return guild;
	}
	
	public ProfileRegistry getProfileRegistry() {
		return profileRegistry;
	}
	
	public AliasRegistry getAliasRegistry() {
		return aliasRegistry;
	}

	public MapRegistry getMapRegistry() {
		return mapRegistry;
	}

	public RollConfig getRollConfig() {
		return rollConfig;
	}

	public RPMapConfig getMapConfig() {
		return mapConfig;
	}

	public void saveProfile(CharProfile profile) {
		gameFileManager.saveProfile(profile);
	}
	
	public void deleteProfile(CharProfile profile) {
		gameFileManager.deleteProfile(profile);
	}
}
