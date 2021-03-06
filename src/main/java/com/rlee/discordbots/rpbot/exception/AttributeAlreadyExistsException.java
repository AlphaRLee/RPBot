package com.rlee.discordbots.rpbot.exception;

import javax.naming.NameAlreadyBoundException;

public class AttributeAlreadyExistsException extends NameAlreadyBoundException {
	private static final long serialVersionUID = -6393999356740797773L;

	public AttributeAlreadyExistsException(String attributeName) {
		super("An attribute named " + attributeName + " already exists for this profile");
	}
}
