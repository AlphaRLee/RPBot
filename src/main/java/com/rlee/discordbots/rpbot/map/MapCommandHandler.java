package com.rlee.discordbots.rpbot.map;

import com.rlee.discordbots.rpbot.MessageListener;
import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.command.CommandParser;
import com.rlee.discordbots.rpbot.exception.InvalidCoordinateException;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.regitstry.MapRegistry;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

public class MapCommandHandler {

	private RPGame game;
	private Member sender;
	private MessageChannel channel;

	private MapRegistry mapRegistry;
	private CommandParser cmdParser;

	public void handleCommand(String[] args, Member sender, RPGame game, MessageChannel channel) {
		setup(args, sender, game, channel);

		cmdParser.setErrorDescription("Type one of the following for the subcommand:\n"
				+  "\tshow, move, set, legend, list, new, delete");
		if (!cmdParser.validateParameterLength(new String[] {"subcommand"})) {
			return;
		}
		cmdParser.setErrorDescription(null);

		String subCommand = args[1].toLowerCase();
		switch (subCommand) {
			case "show":
				showMapCmd(args);
				break;
			case "move":
				break;
			case "set":
				setOnMapCmd(args);
				break;
			case "legend":
				break;
			case "list":
				break;
			case "new":
				break;
			case "delete":
				break;
			case "help": default:
				cmdParser.setErrorDescription("Type one of the following for the subcommand:\n"
						+  "\tshow, move, set, legend, list, new, delete");
				cmdParser.sendUsageError(cmdParser.getLastUsageMessage());
				break;
		}
	}

	private void setup(String[] args, Member sender, RPGame game, MessageChannel channel) {
		this.sender = sender;
		this.game = game;
		this.channel = channel;
		this.mapRegistry = game.getMapRegistry();
		cmdParser = new CommandParser(args, (TextChannel) channel);
	}

	private RPMap getTargetMap(String[] args, int index) {
		RPMap map = mapRegistry.getActiveMap();
		String errorExtension = "";
		if (args.length > index && !Util.isEmptyString(args[index])) {
			map = mapRegistry.getMap(args[index]);
			errorExtension = " with the name **" + args[index] + "**";
		}

		if (map == null) {
			cmdParser.setErrorDescription("No map found" + errorExtension + "."
					+ "\nTry using " + MessageListener.COMMAND_PREFIX + "map list");

			cmdParser.sendUsageError(cmdParser.getLastUsageMessage());
		}

		return map;
	}

	private RPCoordinate getTargetCoordinate(String arg) {
		RPCoordinate rc = null;
		try {
			rc = getTargetCoordinate(arg, false);
		} catch (InvalidCoordinateException e) {
			//Will never reach here
		}

		return rc;
	}

	private RPCoordinate getTargetCoordinate(String arg, boolean throwException) throws InvalidCoordinateException {
		RPCoordinate rc = null;
		try {
			rc = new CoordinateParser().parseCoordinates(arg);
		} catch (InvalidCoordinateException e) {
			e.buildFormattedExceptionMessage(arg);
			if (throwException) {
				throw e;
			} else {
				cmdParser.setErrorDescription(e.getFormattedExceptionMessage());
				cmdParser.sendUsageError(cmdParser.getLastUsageMessage());
			}
		}

		return rc;
	}

	private void showMapCmd(String[] args) {
		cmdParser.setErrorDescription("Show the map.\n"
				+ "[map_name]: The map to show. Defaults to active map.");
		if (!cmdParser.validateParameterLength(new String[] {"show"}, "map_name")) {
			return;
		}

		//FIXME Remove test sample
		if (mapRegistry.getActiveMap() == null) {
			RPMap rpMap = new RPMap("Test");
			rpMap.setEntity(-1, -2, 'b', "Brontosaurus");
			rpMap.setEntity(2, 3, 'c', "Camel");
			rpMap.setEntity(2, 4, 'd', "Dino");
			rpMap.setEntity(1, 5, 'e', "Elephant");
			rpMap.setEntity(1, 6, 'f', "Fish");
			rpMap.setEntity(1, 2, '/', "Wall");
			rpMap.setEntity(7, 4, '\u2588', "Wall");
			rpMap.setEntity(6, 4, '\u2588', "Wall");
			rpMap.setEntity(6, 5, '\u2588', "Wall");

			rpMap.setEntity(0, 0, 'z', "Zebra");
			rpMap.setEntity(0, 7, 'y', "Yak");
			rpMap.setEntity(7, 0, 'x', "Xerus");
			rpMap.setEntity(7, 1, 'w', "Walrus");
			rpMap.setEntity(7, 7, 'u', "Unicorn");
			mapRegistry.addMap(rpMap);
			mapRegistry.setActiveMap(rpMap);
		}

		RPMap map = getTargetMap(args, 2);
		if (map == null) {
			return;
		}

		channel.sendMessage(map.showMap(0, 0)).queue();
	}

	private void setOnMapCmd(String args[]) {
		if (!cmdParser.validateParameterLength(new String[] {"set", "entity", "coordinate"}, "map_name")) {
			return;
		}

		RPMap map = getTargetMap(args, 4);
		if (map == null) {
			return;
		}

		RPCoordinate rc = getTargetCoordinate(args[3]);

		//TODO Parse out the "character" input
		map.setEntity(rc.getRow(), rc.getCol(), args[2].charAt(0), args[2]);

	}

	private void moveCmd(String args[]) {
		cmdParser.setErrorDescription("Move an entity on the map to the provided coordinate.\nCan specify character symbol, starting coordinate, or entity.");
		if (!cmdParser.validateParameterLength(new String[] {"move", "symbol | source_coordinate | entity", "destination_coordinate"}, "map_name")) {
			return;
		}

		RPMap map = getTargetMap(args, 4);
		if (map == null) {
			return;
		}

		RPCoordinate destCoord = getTargetCoordinate(args[3]);
		if (destCoord == null) {
			return;
		}

		MapEntityRegistry.SearchType priorityCast = MapEntityRegistry.SearchType.ENTITY;
		if (args[2].length() == 1) {
			priorityCast = MapEntityRegistry.SearchType.SYMBOL;
		} else if (args[2].length() == 2) {
			priorityCast = MapEntityRegistry.SearchType.COORDINATE;
		}

	}
}
