package com.rlee.discordbots.rpbot.regitstry;

import java.util.HashMap;
import java.util.Map;

import com.rlee.discordbots.rpbot.game.RPGame;

import net.dv8tion.jda.core.entities.Guild;

public class GameRegistry {
	private Map<Guild, RPGame> gamesByGuild;
	
	public GameRegistry() {
		gamesByGuild = new HashMap<Guild, RPGame>();
	}
	
	public Map<Guild, RPGame> getGames() {
		return gamesByGuild;
	}
	
	/**
	 * Get the RPGame that a guild is playing
	 * @param guild
	 * @return RPGame or null if no game found
	 *
	 * @author R Lee
	 */
	public RPGame getGame(Guild guild) {
		return gamesByGuild.get(guild);
	}
	
	/**
	 * Sets the RPGame that this guild is playing. </br>
	 * WILL overwrite existing entries
	 * @param guild Guild to set game for. Must not be null
	 * @param game
	 *
	 * @author R Lee
	 */
	public void setGame(Guild guild, RPGame game) {
		if (guild == null) {
			return;
		}
		
		gamesByGuild.put(guild, game);
	}
}
