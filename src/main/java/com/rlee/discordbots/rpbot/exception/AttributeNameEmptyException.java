package com.rlee.discordbots.rpbot.exception;

public class AttributeNameEmptyException extends Exception{
	private static final long serialVersionUID = -1000135030277819531L;

	public AttributeNameEmptyException() {
		super("The name for the attribute is empty");
	}
}
