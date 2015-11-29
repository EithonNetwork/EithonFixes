package net.eithon.plugin.fixes;

import java.time.LocalDateTime;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String BUY_COMMAND = "/eithonfixes buy <player> <item> <price> <amount>";
	private static final String BALANCE_COMMAND = "/eithonfixes balance";
	private static final String RCADD_COMMAND = "/eithonfixes rcadd <name> <command>";
	private static final String RCEDIT_COMMAND = "/eithonfixes rcedit <name> <command>";
	private static final String RCSET_COMMAND = "/eithonfixes rcset <name> <onEnter> <onOtherWorld>";
	private static final String RCDELETE_COMMAND = "/eithonfixes rcdelete <name>";
	private static final String RCGOTO_COMMAND = "/eithonfixes rcgoto <name>";
	private static final String RCLIST_COMMAND = "/eithonfixes rclist";
	private static final String SPADD_COMMAND = "/eithonfixes spadd <name> [<distance>]";
	private static final String SPEDIT_COMMAND = "/eithonfixes spedit <name> [<distance>]";
	private static final String SPDELETE_COMMAND = "/eithonfixes spdelete <name>";
	private static final String SPGOTO_COMMAND = "/eithonfixes spgoto <name>";
	private static final String SPLIST_COMMAND = "/eithonfixes splist";
	private static final String SERVER_COMMAND = "/eithonfixes server <server name>";
	private static final String RESTART_COMMAND = "/eithonfixes restart [cancel | [<time to restart>]]";
	private static final String DEBUG_COMMAND = "/eithonfixes debug <plugin> <level>";
	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	public boolean onCommand(CommandParser commandParser) {
		String command = commandParser.getArgumentCommand();
		if (command == null) return false;

		if (command.equals("buy")) {
			buyCommand(commandParser);
		} else if (command.equals("balance")) {
			balanceCommand(commandParser);
		} else if (command.equals("debug")) {
			debugCommand(commandParser);
		} else if (command.equals("rcadd")) {
			rcAddCommand(commandParser);
		} else if (command.equals("rcedit")) {
			rcEditCommand(commandParser);
		} else if (command.equals("rcset")) {
			rcSetCommand(commandParser);
		} else if (command.equals("rcdelete")) {
			rcDeleteCommand(commandParser);
		} else if (command.equals("rcgoto")) {
			rcGotoCommand(commandParser);
		} else if (command.equals("rclist")) {
			rcListCommand(commandParser);
		} else if (command.equals("restart")) {
			restartCommand(commandParser);
		} else if (command.equals("server")) {
			serverCommand(commandParser);
		} else if (command.equals("spadd")) {
			spAddCommand(commandParser);
		} else if (command.equals("spedit")) {
			spEditCommand(commandParser);
		} else if (command.equals("spdelete")) {
			spDeleteCommand(commandParser);
		} else if (command.equals("spgoto")) {
			spGotoCommand(commandParser);
		} else if (command.equals("splist")) {
			spListCommand(commandParser);
		} else if (command.equals("test")) {
			testCommand(commandParser);
		} else {
			commandParser.showCommandSyntax();
		}
		return true;
	}

	void buyCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.buy")) return;
		EithonPlayer eithonPlayer = commandParser.getEithonPlayer();
		if ((eithonPlayer != null) && (!eithonPlayer.isInAcceptableWorldOrInformPlayer(Config.V.buyWorlds))) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(4, 5)) return;

		Player buyingPlayer = commandParser.getArgumentPlayer(null);
		if (buyingPlayer == null) return;


		String item = commandParser.getArgumentStringAsLowercase();
		double pricePerItem = commandParser.getArgumentDouble(Double.MAX_VALUE);
		if (pricePerItem == Double.MAX_VALUE) return;
		int amount = commandParser.getArgumentInteger(1);

		this._controller.buy(buyingPlayer, item, pricePerItem, amount);
	}

	void balanceCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.balance")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return ;

		this._controller.displayBalance(player);
	}

	void debugCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.debug")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3)) return;

		String pluginName = commandParser.getArgumentString();
		int debugLevel = commandParser.getArgumentInteger(0);
		CommandSender sender = commandParser.getSender();
		boolean success = this._controller.setPluginDebugLevel(sender, pluginName, debugLevel);
		if (!success) return;
		sender.sendMessage(String.format("Plugin  %s now has debug level %d", pluginName, debugLevel));
	}

	void rcAddCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.rcadd")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgumentString();
		String commands = commandParser.getArgumentRest();
		this._controller.rcAdd(player, name, commands);
	}

	void rcEditCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.rcedit")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgumentString();
		String commands = commandParser.getArgumentRest();
		this._controller.rcEdit(player, name, commands);
	}

	void rcSetCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.rcedit")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(4)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgumentString();
		boolean onEnter = commandParser.getArgumentBoolean(true);
		boolean onOtherWorld = commandParser.getArgumentBoolean(true);
		this._controller.rcSet(player, name, onEnter, onOtherWorld);
	}

	void rcDeleteCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.rcdelete")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgumentString();
		this._controller.rcDelete(player, name);
	}

	void rcGotoCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.rcgoto")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgumentString();
		this._controller.rcGoto(player, name);
	}

	void rcListCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.rclist")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		this._controller.rcList(commandParser.getSender());
	}

	void spAddCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.spadd")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2,3)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgumentString();
		long distance = commandParser.getArgumentInteger(10);
		this._controller.spAdd(player, name, distance);
	}

	void spEditCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.spedit")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2,3)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgumentString();
		long distance = commandParser.getArgumentInteger(10);
		this._controller.spEdit(player, name, distance);
	}

	void spDeleteCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.spdelete")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		String name = commandParser.getArgumentString();
		this._controller.spDelete(commandParser.getSender(), name);
	}

	void spGotoCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.spgoto")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgumentString();
		this._controller.rcGoto(player, name);
	}

	void spListCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.rclist")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		this._controller.spList(commandParser.getSender());
	}

	void restartCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.restart")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1,2)) return;

		CommandSender sender = commandParser.getSender();
		if (sender == null) return;

		String cancel = commandParser.getArgumentString();
		if ((cancel != null) && cancel.startsWith("ca")) {
			boolean success = this._controller.cancelRestart();
			if (success) sender.sendMessage("The server restart has been cancelled.");
			else sender.sendMessage("Too late to cancel server restart.");
			return;
		}

		long secondsToRestart = commandParser.getArgumentTimeAsSeconds(1, 10*60);
		LocalDateTime when = this._controller.initiateRestart(secondsToRestart);
		if (when == null) sender.sendMessage("Could not initiate a restart.");
		else sender.sendMessage(String.format("The server will be restarted %s", when.toString()));
	}

	void testCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.test")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		Player player = commandParser.getPlayer();
		player.sendMessage(String.format("TEST by player %s", player.getName()));
	}

	void serverCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.server")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		String serverName = commandParser.getArgumentStringAsLowercase();
		Player player = commandParser.getPlayer();
		boolean success = this._controller.connectPlayerToServer(player, serverName);
		if (!success) return;
		Config.M.connectedToServer.sendMessage(player, serverName);
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("buy")) {
			sender.sendMessage(BUY_COMMAND);
		} else if (command.equals("balance")) {
			sender.sendMessage(BALANCE_COMMAND);
		} else if (command.equals("debug")) {
			sender.sendMessage(DEBUG_COMMAND);
		} else if (command.equals("rcadd")) {
			sender.sendMessage(RCADD_COMMAND);
		} else if (command.equals("rcedit")) {
			sender.sendMessage(RCEDIT_COMMAND);
		} else if (command.equals("rcset")) {
			sender.sendMessage(RCSET_COMMAND);
		} else if (command.equals("rcdelete")) {
			sender.sendMessage(RCDELETE_COMMAND);
		} else if (command.equals("rcgoto")) {
			sender.sendMessage(RCGOTO_COMMAND);
		} else if (command.equals("rclist")) {
			sender.sendMessage(RCLIST_COMMAND);
		} else if (command.equals("restart")) {
			sender.sendMessage(RESTART_COMMAND);
		} else if (command.equals("server")) {
			sender.sendMessage(SERVER_COMMAND);
		} else if (command.equals("spadd")) {
			sender.sendMessage(SPADD_COMMAND);
		} else if (command.equals("spedit")) {
			sender.sendMessage(SPEDIT_COMMAND);
		} else if (command.equals("spdelete")) {
			sender.sendMessage(SPDELETE_COMMAND);
		} else if (command.equals("spgoto")) {
			sender.sendMessage(SPGOTO_COMMAND);
		} else if (command.equals("splist")) {
			sender.sendMessage(SPLIST_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}
	}
}