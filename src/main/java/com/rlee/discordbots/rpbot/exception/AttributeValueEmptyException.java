package com.rlee.discordbots.rpbot.exception;

public class AttributeValueEmptyException extends Exception {
	private static final long serialVersionUID = -2631253825112200117L;

	public AttributeValueEmptyException() {
		super("The value for the attribute is empty");
	}
}
