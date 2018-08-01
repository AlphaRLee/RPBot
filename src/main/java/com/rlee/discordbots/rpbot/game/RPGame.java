package com.rlee.discordbots.rpbot.game;

import com.rlee.discordbots.rpbot.map.RPMap;
import com.rlee.discordbots.rpbot.profile.CharProfile;
import com.rlee.discordbots.rpbot.regitstry.AliasRegistry;
import com.rlee.discordbots.rpbot.regitstry.GameRegistry;
import com.rlee.discordbots.rpbot.regitstry.MapRegistry;
import com.rlee.discordbots.rpbot.regitstry.ProfileRegistry;

import net.dv8tion.jda.core.entities.Guild;

public class RPGame {

	private GameFileManager gameFileManager;
	
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
		this.profileRegistry = new ProfileRegistry(this);
		this.aliasRegistry = new AliasRegistry(this);
		this.mapRegistry = new MapRegistry(this);

		gameFileManager = new GameFileManager(this);
		gameFileManager.checkCreateFiles();
		gameFileManager.loadProfiles();
	}
	
	public GameRegistry getGameRegistry() {
		return gameRegistry;
	}
	
	public Guild getGuild() {
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

	public void saveProfile(CharProfile profile) {
		gameFileManager.saveProfile(profile);
	}
	
	public void deleteProfile(CharProfile profile) {
		gameFileManager.deleteProfile(profile);
	}
}
