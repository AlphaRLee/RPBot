package com.rlee.discordbots.rpbot.exception;

public class InvalidCoordinateException extends Exception {
	private String formattedExceptionMessage;

	public InvalidCoordinateException() {
		super();
	}

	public InvalidCoordinateException(String message) {
		super(message);
	}

	public String getFormattedExceptionMessage() {
		return formattedExceptionMessage;
	}

	public void buildFormattedExceptionMessage(String userInput) {
		formattedExceptionMessage = "Coordinate cannot be parsed from **" + userInput + "**.\n" + getMessage();
	}
}
