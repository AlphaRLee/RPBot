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
				+  "\tshow, move, add, legend, list, new, delete");
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
				moveCmd(args);
				break;
			case "add":
				addToMapCmd(args);
				break;
			case "legend":
				legendCmd(args);
				break;
			case "list":
				break;
			case "new":
				break;
			case "delete":
				break;
			case "help": default:
				cmdParser.setErrorDescription("Type one of the following for the subcommand:\n"
						+  "\tshow, move, add, legend, list, new, delete");
				cmdParser.sendUserError(cmdParser.getLastUsageMessage());
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

			cmdParser.sendUserError(cmdParser.getLastUsageMessage());
		}

		return map;
	}

	private RPCoordinate parseCoordinates(String arg) {
		RPCoordinate rc = null;
		try {
			rc = RPMap.parseCoordinates(arg);
		} catch (InvalidCoordinateException e) {
			handleInvalidCoordinateException(e, arg);
		}

		return rc;
	}

	private void handleInvalidCoordinateException(InvalidCoordinateException exception, String coordinateArg) {
		exception.buildFormattedExceptionMessage(coordinateArg);
		cmdParser.setErrorDescription(exception.getFormattedExceptionMessage());
		cmdParser.sendUserError(cmdParser.getLastUsageMessage());
	}

	private RPMapEntity<?> parseMapEntity(RPMap map, String arg) {
		RPMapEntity<?> mapEntity = null;

		try {
			mapEntity = map.parseMapEntity(arg);
		} catch (AmbiguousSelectionException e) {
			String ambiguousSelectionLegend = getLegendForSelection(map, arg);
			cmdParser.setErrorDescription(ambiguousSelectionLegend + "\nTry searching by the entity's name instead.");
			cmdParser.sendUserError(e.getMessage());

			return null;
		}

		if (mapEntity == null) {
			cmdParser.setErrorDescription("No entity was found at/by the name **" + arg + "** on the map " + map.getName() + ".");
			cmdParser.sendUserError(cmdParser.getLastUsageMessage());
			return null;
		}

		return mapEntity;
	}

	private String getLegendForSelection(RPMap map, String arg) {
		try {
			return map.showLegendByParsedArg(arg);
		} catch (InvalidSearchTypeException e) {
			cmdParser.setErrorDescription(e.getMessage());
			cmdParser.sendUserError(cmdParser.getLastUsageMessage());
			return null;
		}
	}

	private void showMapCmd(String[] args) {
		cmdParser.setErrorDescription("Show the map.\n"
				+ "[map_name]: The map to show. Defaults to active map.");
		if (!cmdParser.validateParameterLength("show", null, "map_name")) {
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

	private void addToMapCmd(String args[]) {
		if (!cmdParser.validateParameterLength("add", new String[] {"entity", "coordinate"}, "map_name")) {
			return;
		}

		RPMap map = getTargetMap(args, 4);
		if (map == null) {
			return;
		}

		RPCoordinate rc = parseCoordinates(args[3]);

		//TODO Parse out the "character" input
		map.setEntity(rc, args[2].charAt(0), args[2]);
	}

	private void moveCmd(String args[]) {
		cmdParser.setErrorDescription("Move an entity on the map to the provided coordinate.\nCan specify character symbol, starting coordinate, or entity.");
		if (!cmdParser.validateParameterLength("move", new String[] {"symbol | source_coordinate | entity", "destination_coordinate"}, "map_name")) {
			return;
		}

		RPMap map = getTargetMap(args, 4);
		if (map == null) {
			return;
		}

		RPCoordinate destCoord = parseCoordinates(args[3]);
		if (destCoord == null) {
			return;
		}

		RPMapEntity<?> mapEntity = parseMapEntity(map, args[2]);
		if (mapEntity == null) {
			return;
		}

		RPCoordinate oldCoordinate = mapEntity.getCoordinate();
		map.moveEntityToCoordinate(mapEntity, destCoord);
		channel.sendMessage("**" + mapEntity.getName() + "** was moved from **" + oldCoordinate + "** to **" + mapEntity.getCoordinate() + "**.").queue();
	}

	private void legendCmd(String[] args) {
		cmdParser.setErrorDescription("Show the map legend.");
		if (!cmdParser.validateParameterLength("legend", null, "symbol | coordinate")) {
			return;
		}

		RPMap map = mapRegistry.getActiveMap();
		if (args.length > 2) {
			// Show the legend entry for the particular arg (symbol or coordinate)
			String legendOutput = getLegendForSelection(map, args[2]);
			if (legendOutput == null) {
				return;
			}

			channel.sendMessage(legendOutput).queue();
		} else {
			// Show all legend entries
			channel.sendMessage(map.showLegendBySymbols()).queue(); // TODO Handle maps with >20 entities
		}
	}
}
