package com.rlee.discordbots.rpbot.exception;

import com.rlee.discordbots.rpbot.profile.Attribute;

public class AttributeAlreadyExistsException extends Exception {
	private static final long serialVersionUID = -6393999356740797773L;

	public AttributeAlreadyExistsException(String attributeName) {
		super("An attribute named " + attributeName + " already exists for this profile");
	}
}
