package com.rlee.discordbots.rpbot.regitstry;

import java.util.LinkedHashMap;
import java.util.Map;

import com.rlee.discordbots.rpbot.RPBot;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.profile.CharProfile;

import net.dv8tion.jda.core.entities.Member;

public class ProfileRegistry implements Registry {
	private RPGame game;
	
	private Map<String, CharProfile> profilesByName;
	private Map<Member, CharProfile> profilesByMember;

	public ProfileRegistry(RPGame game) {
		this.game = game;
		
		profilesByName = new LinkedHashMap<String, CharProfile>();
		profilesByMember = new LinkedHashMap<Member, CharProfile>();
	}
	
	public RPGame getGame() {
		return game;
	}
	
	/**
	 * @return the profilesByName
	 */
	public Map<String, CharProfile> getProfilesByName() {
		return profilesByName;
	}

	/**
	 * @param profilesByName
	 *            the profilesByName to set
	 */
	public void setProfilesByName(Map<String, CharProfile> profilesByName) {
		this.profilesByName = profilesByName;
	}

	/**
	 * @return the profilesByMember
	 */
	public Map<Member, CharProfile> getProfilesByMember() {
		return profilesByMember;
	}

	/**
	 * @param profilesByMember
	 *            the profilesByMember to set
	 */
	public void setProfilesByMember(Map<Member, CharProfile> profilesByMember) {
		this.profilesByMember = profilesByMember;
	}
	
	/**
	 * Get character profile by name. Case insensitive
	 * @param name
	 * @return
	 *
	 * @author R Lee
	 */
	public CharProfile getProfile(String name) {
		return profilesByName.get(name.toLowerCase());
	}

	/**
	 * @param member
	 * @return the character profile associated to this member or null if not
	 *         found
	 *
	 * @author R Lee
	 */
	public CharProfile getProfile(Member member) {
		return profilesByMember.get(member);
	}

	/**
	 * Add a profile indexed by its name. Name must not be null and not already
	 * within this registry If a member is associated to the profile, profile is
	 * indexed by member as well. Member must not be already within this
	 * registry Names are stored in lower case
	 * 
	 * @param profile
	 *
	 * @author R Lee
	 */
	public void addProfile(CharProfile profile) {
		if (RPBot.isEmptyString(profile.getName()) || containsName(profile.getName().toLowerCase())) {
			return; // Do not allow null name entry
					// Do not override existing entry
		}

		profilesByName.put(profile.getName().toLowerCase(), profile);

		if (profile.getMember() == null || containsMember(profile.getMember())) {
			return; // Do not use null member entry
			// Do not override existing entry
		}

		profilesByMember.put(profile.getMember(), profile);
	}
	
	public boolean removeProfile(String name) {
		CharProfile profile = getProfile(name);
		
		if (profile == null) {
			return false;
		}
		
		if (profile.getMember() != null) {
			//TODO: Add confirmation prompt
		}
		
		return removeProfile(profile);
	}
	
	public boolean removeProfile(Member member) {
		CharProfile profile = getProfile(member);
		
		if (profile == null) {
			return false;
		}
		
		//TODO: Add confirmation prompt
		
		return removeProfile(profile);
	}
	
	public boolean removeProfile(CharProfile profile) {
		CharProfile removedProfile = profilesByName.remove(profile.getName().toLowerCase());
		profilesByMember.remove(profile.getMember());
		game.deleteProfile(removedProfile);
		return removedProfile != null;
	}
	
	public boolean containsName(String name) {
		return profilesByName.containsKey(name);
	}

	public boolean containsMember(Member member) {
		return profilesByMember.containsKey(member);
	}
	
	/**
	 * Claim the profile for the specified member (and override previous claims related to member and profile),
	 * and save changes to file
	 * @param member
	 * @param profile
	 * @return True if profile was successfully claimed, false if not (e.g. null inputs)
	 */
	public boolean claimProfile(Member member, CharProfile profile) {
		if (member == null || profile == null) {
			return false;
		}
		
		profilesByMember.put(member, profile);
		profile.setMember(member);
		game.saveProfile(profile);
		return true;
	}
	
	/**
	 * Unclaim the profile held by the given member and save changes to file
	 * @param member
	 * @return The profile that was unclaimed, or null if no profile was found for the member
	 */
	public CharProfile unclaimProfile(Member member) {
		if (!containsMember(member)) {
			return null;
		}
		
		CharProfile profile = profilesByMember.remove(member);
		profile.setMember(null);
		game.saveProfile(profile);
		return profile;
	}
}
