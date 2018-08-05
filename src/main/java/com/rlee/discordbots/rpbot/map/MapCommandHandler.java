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
				test(args[2]);
				break;
			case "set":
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

	private void test(String arg2) {
		//FIXME Delete this test function!
		CoordinateParser cp = new CoordinateParser();
		try {
			RPCoordinate coord = cp.parseCoordinates(arg2);
			channel.sendMessage("Coord received! Row: " + coord.getRow() + ", Col: " + coord.getCol()).queue();
		} catch (InvalidCoordinateException e) {
			e.buildFormattedExceptionMessage(arg2);
			cmdParser.setErrorDescription(e.getFormattedExceptionMessage());
			cmdParser.sendUsageError(cmdParser.getLastUsageMessage());
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

	private void showMapCmd(String[] args) {
		cmdParser.setErrorDescription("Show the map.\n"
				+ "[map_name]: The map to show. Defaults to active map.");
		if (!cmdParser.validateParameterLength(new String[] {"show"}, new String[] {"map_name", "test_case"})) {
			return;
		}

		//FIXME Remove test sample
		RPMap rpMap = new RPMap("Test");
		rpMap.setAt(-1, -2, 'b', "Brontosaurus");
		rpMap.setAt(2, 3, 'c', "Camel");
		rpMap.setAt(2, 4, 'd', "Dino");
		rpMap.setAt(1, 5, 'e', "Elephant");
		rpMap.setAt(1, 6, 'f', "Fish");
		rpMap.setAt(1, 2, '/', "Wall");
		rpMap.setAt(7, 4, '\u2588', "Wall");
		rpMap.setAt(6, 4, '\u2588', "Wall");
		rpMap.setAt(6, 5, '\u2588', "Wall");
		mapRegistry.addMap(rpMap);
		mapRegistry.setActiveMap(rpMap);

		RPMap map = getTargetMap(args, 2);
		if (map == null) {
			return;
		}

		channel.sendMessage(map.showMap(0, 0)).queue();
	}
}
