package com.rlee.discordbots.rpbot.profile;

import java.util.HashSet;
import java.util.Set;

public class Item {
	private String name;
	private Set<NumberAttribute> attributeEffects;
	private CharProfile profile;
	
	public Item(String name) {
		this.setName(name);
		attributeEffects = new HashSet<NumberAttribute>();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the profile
	 */
	public CharProfile getProfile() {
		return profile;
	}

	/**
	 * @param profile the character profile that this item belongs to
	 */
	public void setProfile(CharProfile profile) {
		this.profile = profile;
	}
	
//	/**
//	 * @return the attributeEffects
//	 */
//	public Set<Attribute> getEffects() {
//		return attributeEffects;
//	}

//	/**
//	 * @param attributeEffects the attributeEffects to set
//	 */
//	public void setEffects(Map<Attribute, Integer> attributeEffects) {
//		this.effects = attributeEffects;
//	}
	
	/**
	 * Get the effect this item has on the specified attribute.
	 * More specifically, invokes {@link NumberAttribute#getItemEffect(Item)} with this item if attribute is valid
	 * and has been added to this effect list.
	 * @param attribute Attribute to get value of.
	 * @return Effect value of attribute.  Will return null if attribute is null or not added to attributeEffects
	 *
	 * @author R Lee
	 */
	public Integer getEffect(NumberAttribute attribute) {
		return (attribute != null && attributeEffects.contains(attribute)) ? attribute.getItemEffect(this) : null;
	}
	
	/**
	 * Set the effect value for a particular attribute.
	 * Invokes {@link NumberAttribute#setItemEffect(Item, Integer)} with this item and the effect value.
	 * Adds attribute to attributeEffects if absent or replaces last effect value if present.
	 * @param attribute Attribute to associate value for. If null, nothing will happen
	 * @param effectValue
	 *
	 * @author R Lee
	 */
	public void setEffect(NumberAttribute attribute, Integer effectValue) {
		if (attribute == null) {
			return;
		}
		
		attributeEffects.add(attribute);
		attribute.setItemEffect(this, effectValue);
	}
}
