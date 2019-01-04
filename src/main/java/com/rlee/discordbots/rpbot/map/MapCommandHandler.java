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

import javax.naming.NameAlreadyBoundException;

public class MapCommandHandler {

	private RPGame game;
	private Member sender;
	private MessageChannel channel;

	private MapRegistry mapRegistry;
	private CommandParser cmdParser;

	public void handleCommand(String[] args, Member sender, RPGame game, MessageChannel channel) {
		setup(args, sender, game, channel);

		String errorDescription = "Type one of the following for the subcommand:\n"
				+  "\tshow, legend, add, remove, move, listmaps, newmap, deletemap";
		cmdParser.setErrorDescription(errorDescription);
		if (!cmdParser.validateParameterLength(new String[] {"subcommand"})) {
			return;
		}
		cmdParser.setErrorDescription(null); // Wipe out the error description so that the subcommand can set it cleanly

		String subCommand = args[1].toLowerCase();
		switch (subCommand) {
			case "show":
				showMapCmd(args);
				break;
			case "legend":
				legendCmd(args);
				break;
			case "add":
				addToMapCmd(args);
				break;
			case "remove":
				removeFromMapCmd(args);
				break;
			case "move":
				moveCmd(args);
				break;
			case "clear":
				clearMapCmd(args);
				break;
			case "listmaps": case "list":
				break;
			case "newmap": case "new":
				break;
			case "deletemap": case "delete":
				break;
			case "help": default:
				cmdParser.setErrorDescription(errorDescription);
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
			try {
				rpMap.addEntity(-1, -2, 'b', "Brontosaurus");
				rpMap.addEntity(2, 3, 'c', "Camel");
				rpMap.addEntity(2, 4, 'd', "Dino");
				rpMap.addEntity(1, 5, 'e', "Elephant");
				rpMap.addEntity(1, 6, 'f', "Fish");
				rpMap.addEntity(1, 2, '/', "Wall");
				rpMap.addEntity(7, 4, '\u2588', "Wall");
				rpMap.addEntity(6, 4, '\u2588', "Wall");
				rpMap.addEntity(6, 5, '\u2588', "Wall");

				rpMap.addEntity(0, 0, 'z', "Zebra");
				rpMap.addEntity(0, 7, 'y', "Yak");
				rpMap.addEntity(7, 0, 'x', "Xerus");
				rpMap.addEntity(7, 1, 'w', "Walrus");
				rpMap.addEntity(7, 7, 'u', "Unicorn");
			} catch (NameAlreadyBoundException e) {
				e.printStackTrace();
			}
			mapRegistry.addMap(rpMap);
			mapRegistry.setActiveMap(rpMap);
		}

		RPMap map = getTargetMap(args, 2);
		if (map == null) {
			return;
		}

		channel.sendMessage(map.showMap(0, 0)).queue();
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

	private void addToMapCmd(String[] args) {
		if (!cmdParser.validateParameterLength("add", new String[] {"entity", "coordinate"}, "map_name")) {
			return;
		}

		RPMap map = getTargetMap(args, 4);
		if (map == null) {
			return;
		}

		RPCoordinate coord = parseCoordinates(args[3]);
		String inputName = args[2];
		String outputName = null;
		try {
			// TODO Parse out the "character" input
			// TODO add configurability for the autorename feature
			outputName = map.addEntity(coord, args[2].charAt(0), inputName, true);
		} catch (NameAlreadyBoundException e) {
			cmdParser.setErrorDescription(e.getMessage());
			cmdParser.sendUserError(cmdParser.getLastUsageMessage());
			return;
		}

		RPMapEntity<?> outputEntity = map.getEntity(outputName);
		String outputMessage = "**" + outputName + "** [`" + outputEntity.getSymbol() + "`] has been added at **" + outputEntity.getCoordinate() + "**.";

		if (!outputName.equals(inputName)) {
			outputMessage += "\n**" + inputName + "** has been renamed to **" + outputName + "** because another entity already has the requested name.";
		}

		channel.sendMessage(outputMessage).queue();
	}

	private void removeFromMapCmd(String[] args) {
		cmdParser.setErrorDescription("Remove an entity from the map.\nCan specify character symbol, coordinate, or entity name.");
		if (!cmdParser.validateParameterLength("remove", new String[] {"symbol | coordinate | entity"})) {
			return;
		}

		RPMap map = getTargetMap(args, 3); // TODO Add support for choosing map
		if (map == null) {
			return;
		}

		RPMapEntity<?> mapEntity = parseMapEntity(map, args[2]);
		if (mapEntity == null) {
			return;
		}

		if (map.removeEntity(mapEntity)) {
			channel.sendMessage("Entity **" + mapEntity.getName() + "** [`" + mapEntity.getSymbol() + "`] has been removed from the map " + map.getName() + " at **" + mapEntity.getCoordinate() + "**.").queue();
		} else {
			// Theoretically should never get here
			channel.sendMessage("Entity **" + mapEntity.getName() + "** could not be removed. Please contact a developer.").queue();
		}
	}

	private void moveCmd(String[] args) {
		cmdParser.setErrorDescription("Move an entity on the map to the provided coordinate.\nCan specify character symbol, starting coordinate, or entity name.");
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

	/**
	 * Clears all entities from a map.
	 * @param args
	 */
	private void clearMapCmd(String[] args) {
		cmdParser.setErrorDescription("Clear all entities from a map.");
		if (!cmdParser.validateParameterLength("clear", null, "map_name")) {
			return;
		}

		RPMap map = getTargetMap(args, );
		if (map == null) {
			return;
		}

		map.clearEntities();
		channel.sendMessage("The map **" + map.getName() + "** has been cleared.").queue();
	}
}
