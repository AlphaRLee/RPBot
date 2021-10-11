package com.rlee.discordbots.rpbot.regitstry;

import java.util.HashMap;
import java.util.Map;

import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.profile.Attribute;
import com.rlee.discordbots.rpbot.profile.CharProfile;

public class AliasRegistry {
	
	private RPGame game;
	
	private Map<String, String> aliases;
	
	public AliasRegistry(RPGame game) {
		this.game = game;
		this.aliases = new HashMap<String, String>();
	}
	
	public RPGame getGame() {
		return game;
	}
	
	public Map<String, String> getAliases() {
		return aliases;
	}
	
	public void setAliases(Map<String, String> aliases) {
		this.aliases = aliases;
	}
	
	/**
	 * Gets the full, "real" name associated to an alias
	 * @param alias
	 * @return Full name alias points to or null if none set
	 *
	 * @author R Lee
	 */
	public String getFull(String alias) {
		return aliases.get(alias);
	}
	
	/**
	 * Sets the alias name to the full name.<br/>
	 * Aliases and full names will be stored in lowercase and all spaces are replaced with underscores
	 * @param alias Must not be empty nor null
	 * @param full Must not be empty nor null
	 *
	 * @author R Lee
	 */
	public void setAlias(String alias, String full) {
		if (alias == null || alias.isEmpty() || full == null || full.isEmpty()) {
			return;
		}
		
		aliases.put(Util.replaceWhitespaces(alias, true), Util.replaceWhitespaces(full, true));
	}
	
	/**
	 * Convenience method: Tries to find an attribute value associated to the given alias
	 * @param attributeName Attribute name, or attribute alias
	 * @return
	 *
	 * @author R Lee
	 */
	public Attribute getAttribute(String attributeName, CharProfile profile) {
		String fullAttr = getAttributeName(attributeName, profile);
		if (Util.isEmptyString(fullAttr)) {
			return null;
		}
		
		return profile.getAttribute(fullAttr);
	}
	
	/**
	 * Convenience method: Try to find the full exact attribute name associated with the given alias and profile
	 * @param alias
	 * @param profile
	 * @return Full, exact attribute name on the profile in lowercase letters, or null if inputs are null, empty or no alias found
	 *
	 * @author R Lee
	 */
	public String getAttributeName(String alias, CharProfile profile) {
		if (Util.isEmptyString(alias) || profile == null) {
			return null;
		}
		
		boolean hasAttr = profile.hasAttribute(alias);
		String nameAttempt = alias;
		
		if (!hasAttr) {
			int count = 0;
			final int MAX_COUNT = 50;
			
			nameAttempt = getFull(alias);
			
			//Chain-traverse down list of names (eg. comm -> com -> communication)
			while (!profile.hasAttribute(nameAttempt) && count < MAX_COUNT) {
				nameAttempt = getFull(nameAttempt);
				count++;
			}
		}
		
		return nameAttempt != null ? nameAttempt.toLowerCase() : null;
	}
}
