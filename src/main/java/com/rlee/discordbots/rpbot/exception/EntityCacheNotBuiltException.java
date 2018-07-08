package com.rlee.discordbots.rpbot.exception;

public class EntityCacheNotBuiltException extends Exception {
	public EntityCacheNotBuiltException() {
		super("The EntityCache has not been built and populated yet");
	}
}
