package com.rlee.discordbots.rpbot.command;

import com.rlee.discordbots.rpbot.MessageListener;
import com.rlee.discordbots.rpbot.RPBot;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.map.RPMap;
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

	public MapCommandHandler(String[] args, Member sender, RPGame game, MessageChannel channel) {
		this.sender = sender;
		this.game = game;
		this.channel = channel;
		this.mapRegistry = game.getMapRegistry();

		cmdParser = new CommandParser(args, (TextChannel) channel);

		handleCommand(args);
	}

	private void handleCommand(String[] args) {
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

	private RPMap getTargetMap(String[] args, int index) {
		RPMap map = mapRegistry.getActiveMap();
		String errorExtension = "";
		if (args.length > index && !RPBot.isEmptyString(args[index])) {
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
				+ "[map\\_name]: The map to show. Defaults to active map.");
		if (!cmdParser.validateParameterLength(new String[] {"show"}, new String[] {"map_name", "test_case"})) {
			return;
		}

		//FIXME Remove test sample
		RPMap rpMap = new RPMap("Test");
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

		channel.sendMessage(map.showMap()).queue();
	}
}
