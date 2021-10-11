package com.rlee.discordbots.rpbot.map;

import com.rlee.discordbots.rpbot.MessageListener;
import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.command.CommandParser;
import com.rlee.discordbots.rpbot.exception.InvalidCoordinateException;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.regitstry.MapRegistry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.naming.NameAlreadyBoundException;
import java.util.StringJoiner;

public class MapCommandHandler {

	private RPGame game;
	private Member sender;
	private MessageChannel channel;

	private MapRegistry mapRegistry;
	private CommandParser cmdParser;

	public void handleCommand(String[] args, Member sender, RPGame game, MessageChannel channel) {
		setup(args, sender, game, channel);

		String errorDescription = "Type one of the following for the subcommand:\n"
				+  "\nshow, legend,\nadd, remove, move, setsymbol,\nusingmap, listmaps, usemap,\nnewmap, deletemap,\ndemo";
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
			case "setsymbol": case "symbol": case "ss":
				setEntitySymbolCmd(args);
				break;
			case "move":
				moveCmd(args);
				break;
			case "clear":
				clearMapCmd(args);
				break;
			case "listmaps": case "list":
				listMapsCmd(args);
				break;
			case "usingmap": case "using":
				usingMapCmd(args);
				break;
			case "usemap": case "use":
				useMapCmd(args);
				break;
			case "newmap": case "new": case "addmap":
				newMapCmd(args);
				break;
			case "deletemap": case "delete": case "delmap":
				deleteMapCmd(args);
				break;
			case "demo":
				demoCmd(args);
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
					+ "\nTry using `" + MessageListener.COMMAND_PREFIX + "map listmaps`");

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

	private String getActiveMapMessage() {
		RPMap activeMap = mapRegistry.getActiveMap();
		String activeMapMessage = "No map selected. Select a map with `" + MessageListener.COMMAND_PREFIX + "map usemap`.";
		if (activeMap != null) {
			activeMapMessage = "Current active map: **" + activeMap.getName() + "**";
		}

		return activeMapMessage;
	}

	private void showMapCmd(String[] args) {
		cmdParser.setErrorDescription("Show the map.\n"
				+ "[map_name]: The map to show. Defaults to active map.");
		if (!cmdParser.validateParameterLength("show", null, "map_name", "bottom_left_coordinate")) {
			return;
		}

		RPMap map = getTargetMap(args, 2);
		if (map == null) {
			return;
		}

		RPCoordinate bottomLeftCoordinate = new RPCoordinate(1, 1); // Default coordinate for bottom left
		if (args.length > 3) {
			bottomLeftCoordinate = parseCoordinates(args[3]);
			if (bottomLeftCoordinate == null) {
				return;
			}
		}

		// TODO Allow customization on row/col count
		channel.sendMessage("__" + map.getName() + "__:\n" + map.showMap(bottomLeftCoordinate)).queue();
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

	private void setEntitySymbolCmd(String[] args) {
		cmdParser.setErrorDescription("Set the symbol of an entity.\nCan specify character symbol, starting coordinate, or entity name.");
		if (!cmdParser.validateParameterLength("setsymbol", new String[] {"symbol | coordinate | entity", "symbol"}, "map_name")) {
			return;
		}

		RPMap map = getTargetMap(args, 4);
		if (map == null) {
			return;
		}

		RPMapEntity<?> mapEntity = parseMapEntity(map, args[2]);
		if (mapEntity == null) {
			return;
		}

		char oldSymbol = mapEntity.getSymbol();
		map.setEntitySymbol(mapEntity, args[3].charAt(0));
		channel.sendMessage("Entity " + mapEntity.getName() + " changed symbols from `" + oldSymbol + "` to `" + mapEntity.getSymbol() + "` at " + mapEntity.getCoordinate() + ".").queue();
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

		RPMap map = getTargetMap(args, 2);
		if (map == null) {
			return;
		}

		map.clearEntities();
		channel.sendMessage("The map **" + map.getName() + "** has been cleared.").queue();
	}

	private void listMapsCmd(String[] args) {
		cmdParser.setErrorDescription("List all maps available.");
		if (!cmdParser.validateParameterLength("listmaps", null)) {
			return;
		}

		StringJoiner sj = new StringJoiner("\n");
		sj.add("__Maps__:");

		if (mapRegistry.getMapNames().isEmpty()) {
			sj.add("(No maps found. Create a map with `" + MessageListener.COMMAND_PREFIX + "map newmap`.)");
		}

		for (String mapName : mapRegistry.getMapNames()) {
			sj.add(mapName);
		}

		channel.sendMessage(sj.toString()).queue();
	}

	/**
	 * Get a message telling which map the user currently has selected
	 * @param args
	 */
	private void usingMapCmd(String[] args) {
		cmdParser.setErrorDescription("Get the map currently being used.");
		if (!cmdParser.validateParameterLength("usingmap", null)) {
			return;
		}

		channel.sendMessage(getActiveMapMessage()).queue();
	}

	private void useMapCmd(String[] args) {
		cmdParser.setErrorDescription("Set the active map to use.\n" + getActiveMapMessage());
		if (!cmdParser.validateParameterLength("usemap", new String[] {"map_name"})) {
			return;
		}

		RPMap map = getTargetMap(args, 2);
		if (map == null) {
			return;
		}

		mapRegistry.setActiveMap(map);
		channel.sendMessage("Now using **" + map.getName() + "** as the active map.\nUse `" + MessageListener.COMMAND_PREFIX + "map show` to display the map.").queue();
	}

	private void newMapCmd(String[] args) {
		cmdParser.setErrorDescription("Create a new map.");
		if (!cmdParser.validateParameterLength("newmap", new String[] {"map_name"})) {
			return;
		}

		String mapName = args[2];
		RPMap map = new RPMap(mapName, game.getMapConfig());

		try {
			mapRegistry.addMap(map);
		} catch (NameAlreadyBoundException e) {
			cmdParser.setErrorDescription("The name **" + mapName + "** is already taken by another map." +
					"\nUse that map with `" + MessageListener.COMMAND_PREFIX + "map usemap " + mapName + "`" +
					"\nOr delete that map first with `" + MessageListener.COMMAND_PREFIX + "map deletemap " + mapName + "`.");
			cmdParser.sendUserError(cmdParser.getLastUsageMessage());
			return;
		}

		channel.sendMessage("Map **" + map.getName() + "** has been created." +
				"\nUse the map with `" + MessageListener.COMMAND_PREFIX + "map usemap " + map.getName() + "`.").queue();
	}

	private void deleteMapCmd(String[] args) {
		cmdParser.setErrorDescription("Delete a map.");
		if (!cmdParser.validateParameterLength("deletemap", new String[] {"map_name"})) {
			return;
		}

		RPMap map = getTargetMap(args, 2);
		if (map == null) {
			return;
		}

		map.clearEntities();
		mapRegistry.removeMap(map);
		mapRegistry.setActiveMap(null);

		channel.sendMessage("Map **" + map.getName() + "** has been deleted.\n" +
				"Select a new map with `" + MessageListener.COMMAND_PREFIX + "map usemap`").queue();
	}

	private boolean hasDemoRan = false;
	private void demoCmd(String args[]) {
		String[] originalArgs = cmdParser.getArgs();
		String[] cmdArgs;

		cmdParser.setErrorDescription("Demo for creating and using a demo map.");
		if (!cmdParser.validateParameterLength("demo", null)) {
			return;
		}

		if (!mapRegistry.containsName("DemoMap")) {
			String newMapCmdMsg = MessageListener.COMMAND_PREFIX + "map newmap DemoMap";
			String[] newMapCmdArgs = newMapCmdMsg.split(" ");
			channel.sendMessage("Creating a new demo map with: `" + newMapCmdMsg + "`").queue();
			cmdParser.setArgs(newMapCmdArgs);
			newMapCmd(newMapCmdArgs);
		}

		String useMapCmdMsg = MessageListener.COMMAND_PREFIX + "map usemap DemoMap";
		cmdArgs = useMapCmdMsg.split(" ");
		channel.sendMessage("Using the demo map with: `" + useMapCmdMsg + "`").queue();
		cmdParser.setArgs(cmdArgs);
		useMapCmd(cmdArgs);

		RPMap rpMap = mapRegistry.getActiveMap();

		if (!hasDemoRan) {
			String addToMapCmdMessage = MessageListener.COMMAND_PREFIX + "map add Camel C2";
			cmdArgs = addToMapCmdMessage.split(" ");
			channel.sendMessage("Adding entity to demo map with: `" + addToMapCmdMessage + "`").queue();
			cmdParser.setArgs(cmdArgs);
			addToMapCmd(cmdArgs);

			channel.sendMessage("Adding more entities to demo map...").queue();

			try {
				rpMap.addEntity(-1, -2, 'B', "Brontosaurus");
//				rpMap.addEntity(2, 3, 'C', "Camel");
				rpMap.addEntity(3, 4, 'D', "Dino");
				rpMap.addEntity(2, 5, 'E', "Elephant");
				rpMap.addEntity(2, 6, 'F', "Fish");
				rpMap.addEntity(1, 2, '/', "Wall");
				rpMap.addEntity(8, 5, '\u2588', "Wall");
				rpMap.addEntity(7, 5, '\u2588', "Wall");
				rpMap.addEntity(7, 6, '\u2588', "Wall");

				rpMap.addEntity(1, 1, 'Z', "Zebra");
				rpMap.addEntity(1, 8, 'Y', "Yak");
				rpMap.addEntity(8, 1, 'X', "Xerus");
				rpMap.addEntity(8, 2, 'W', "Walrus");
				rpMap.addEntity(8, 8, 'U', "Unicorn");
			} catch (NameAlreadyBoundException e) {
				e.printStackTrace();
			}
		}

		String showMapCmdMsg = MessageListener.COMMAND_PREFIX + "map show";
		cmdArgs = showMapCmdMsg.split(" ");
		channel.sendMessage("Showing the demo map with: `" + showMapCmdMsg + "`").queue();
		cmdParser.setArgs(cmdArgs);
		showMapCmd(cmdArgs);

		String legendCmdMsg = MessageListener.COMMAND_PREFIX + "map legend";
		cmdArgs = showMapCmdMsg.split(" ");
		channel.sendMessage("Finishing demo by showing legend with: `" + legendCmdMsg + "`").queue();
		cmdParser.setArgs(cmdArgs);
		legendCmd(cmdArgs);

		cmdParser.setArgs(originalArgs);
		hasDemoRan = true;
	}
}
