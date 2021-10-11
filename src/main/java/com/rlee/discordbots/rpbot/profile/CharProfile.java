package com.rlee.discordbots.rpbot.profile;

import java.util.LinkedHashMap;
import java.util.Map;

import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.regitstry.ProfileRegistry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

/**
 * Object carrying profille information about a character
 * 
 * @author R Lee
 */
public class CharProfile {
	private ProfileRegistry profileRegistry;

	private String name = "";
	private Member member;
	private Message sourceMessage;
	
	private Map<String, Attribute> attributes;
	// Attributes carrying max values, eg: health
	//private Map<String, Integer> attributeMaxes;
	
	public CharProfile(ProfileRegistry profileRegistry) {
		// Default constructor. No name given, designed for temporary value only
		this.profileRegistry = profileRegistry;
		attributes = new LinkedHashMap<String, Attribute>();
		//attributeMaxes = new HashMap<String, Integer>();	
	}

	public CharProfile(ProfileRegistry profileRegistry, String name) {
		this(profileRegistry);
		this.setName(name);
	}

	public ProfileRegistry getProfileRegistry() {
		return this.profileRegistry;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean hasName() {
		return name != null && name.length() > 0;
	}

	/**
	 * @return the member that this character belongs to
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * @param member
	 *            the member that this character belongs to
	 */
	public void setMember(Member member) {
		this.member = member;
	}

	public Message getSourceMessage() {
		return sourceMessage;
	}
	
	/**
	 * Set the source message that this profile had read from and can read from during updates
	 * @param message
	 *
	 * @author R Lee
	 */
	public void setSourceMessage(Message message) {
		this.sourceMessage = message;
	}
	
	/**
	 * @return the attributes of this character
	 */
	public Map<String, Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map<String, Attribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @param attributeName Case insensitive name
	 * @return Attribute associated to given name or null if no name found
	 *
	 * @author R Lee
	 */
	public Attribute getAttribute(String attributeName) {
		return attributes.get(attributeName.toLowerCase());
	}

	/**
	 * Set the particular attribute to the given attribute name, stored in lower case.
	 * @param attributeName Attribute name. If null or empty, does nothing.
	 * @param attribute
	 *
	 * @author R Lee
	 */
	public void setAttribute(String attributeName, Attribute attribute) {
		if (Util.isEmptyString(attributeName)) {
			return;
		}
		
		attributes.put(attributeName.toLowerCase(), attribute);
	}
	
	/**
	 * 
	 * @param attributeName Case insensitive name
	 * @return Whether this profile has the requested attribute or not or false for empty strings
	 *
	 * @author R Lee
	 */
	public boolean hasAttribute(String attributeName) {
		if (Util.isEmptyString(attributeName)) {
			return false;
		}
		
		return attributes.containsKey(attributeName.toLowerCase());
	}
	
	public Attribute removeAttribute(String attributeName) {
		if (Util.isEmptyString(attributeName) || !hasAttribute(attributeName)) {
			return null;
		}
		
		return attributes.remove(attributeName.toLowerCase());
	}
	
	public Attribute removeAttribute(Attribute attribute) {
		if (attribute == null) {
			return null;
		}
		
		return removeAttribute(attribute.getName());
	}
	
	public boolean hasMember() {
		return member != null;
	}
}
