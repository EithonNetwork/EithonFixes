package net.eithon.plugin.fixes;

import java.time.LocalDateTime;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.command.syntax.ParameterSyntax;
import net.eithon.library.command.syntax.CommandSyntax;
import net.eithon.library.command.syntax.ParameterSyntax.ParameterType;
import net.eithon.library.command.CommandArguments;
import net.eithon.library.command.CommandParser;
import net.eithon.library.command.ICommandHandler;
import net.eithon.library.command.ParameterValue;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private Controller _controller;
	private CommandSyntax _commandSyntax;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;

		CommandSyntax commandSyntax = new CommandSyntax("eithonfixes");

		setupBuyCommand(commandSyntax);
		setupDebugCommand(commandSyntax);
		setupRcCommand(commandSyntax);
		setupSpCommand(commandSyntax);
		setupBalanceCommand(commandSyntax);
		setupServerCommand(commandSyntax);

		this._commandSyntax = commandSyntax;
	}

	@Override
	public CommandSyntax getCommandSyntax()
	{
		return this._commandSyntax;
	}

	public void setupBuyCommand(CommandSyntax commandSyntax) {
		// buy <player> <item> <price> [<amount>]
		CommandSyntax buy = commandSyntax.addCommand("buy", p -> buyCommand(p));
		buy.setPermission("eithonfixes.buy");
		buy.addParameterPlayer("player");
		buy.addParameter(ParameterType.STRING, "item");
		buy.addParameter(ParameterType.REAL, "price");
		ParameterSyntax parameter = buy.addParameter(ParameterType.INTEGER, "amount");
		parameter.setDefault("1");
	}

	public void setupDebugCommand(CommandSyntax commandSyntax) {
		CommandSyntax debug = commandSyntax.addCommand("debug", p -> debugCommand(p));
		debug.setPermission("eithonfixes.debug");
		debug.addParameter(ParameterType.STRING, "plugin");
		ParameterSyntax parameter = debug.addParameter(ParameterType.INTEGER, "level");
		parameter.setValues("0", "1", "2", "3");
		parameter.setDefault("0");
	}

	public void setupServerCommand(CommandSyntax commandSyntax) {
		CommandSyntax server = commandSyntax.addCommand("server", p -> serverCommand(p));
		server.setPermission("eithonfixes.server");
		server.addParameter(ParameterType.STRING, "name");
	}

	public void setupBalanceCommand(CommandSyntax commandSyntax) {
		commandSyntax.addCommand("balance", p -> balanceCommand(p));
	}

	public void setupSpCommand(CommandSyntax commandSyntax) {
		CommandSyntax sp = commandSyntax.addCommand("sp");
		ParameterSyntax spExistingName = new ParameterSyntax(ParameterType.STRING, "name");
		spExistingName.SetValueGetter(() -> this._controller.getAllSpawnPointNames(), true);

		// sp add
		CommandSyntax spAdd = sp.addCommand("add", p -> spAddCommand(p));
		spAdd.setPermission("eithonfixes.spadd");
		spAdd.addParameter(ParameterType.STRING, "name");
		spAdd.setRestOfParametersAsOptional();
		ParameterSyntax parameter = spAdd.addParameter(ParameterType.INTEGER, "distance");
		parameter.setDefault("10");

		// sp edit
		CommandSyntax spEdit = sp.addCommand("edit", p -> spEditCommand(p));
		spEdit.setPermission("eithonfixes.spedit");
		spEdit.addParameter(spExistingName);
		spAdd.setRestOfParametersAsOptional();
		spAdd.addParameter(ParameterType.INTEGER, "distance");

		// sp delete
		CommandSyntax spDelete = sp.addCommand("delete", p -> spDeleteCommand(p));
		spDelete.setPermission("eithonfixes.spdelete");
		spDelete.addParameter(spExistingName);

		// sp goto
		CommandSyntax spGoto = sp.addCommand("goto", p -> spGotoCommand(p));
		spGoto.setPermission("eithonfixes.spgoto");
		spGoto.addParameter(spExistingName);

		sp.addCommand("list", p -> spListCommand(p));
		spGoto.setPermission("eithonfixes.splist");
	}

	public void setupRcCommand(CommandSyntax commandSyntax) {

		CommandSyntax rc = commandSyntax.addCommand("rc");
		ParameterSyntax rcExistingName = new ParameterSyntax(ParameterType.STRING, "name");
		rcExistingName.SetValueGetter(() -> this._controller.getAllRegionCommands(), true);

		// rc add
		CommandSyntax rcAdd = rc.addCommand("add", p -> rcAddCommand(p));
		rcAdd.setPermission("eithonfixes.rcadd");
		rcAdd.addParameter(ParameterType.STRING, "name");
		rcAdd.addParameter(ParameterType.REST, "command");

		// rc edit
		CommandSyntax rcEdit = rc.addCommand("edit", p -> rcEditCommand(p));
		rcEdit.setPermission("eithonfixes.rcedit");
		rcEdit.addParameter(rcExistingName);
		rcEdit.addParameter(ParameterType.REST, "command");

		// rc set
		CommandSyntax rcSet = rc.addCommand("set", p -> rcSetCommand(p));
		rcSet.setPermission("eithonfixes.rcset");
		rcSet.addParameter(rcExistingName);
		rcSet.addNamedParameter(ParameterType.BOOLEAN, "OnEnter");
		rcSet.addNamedParameter(ParameterType.BOOLEAN, "OnOtherWorld");

		// rc delete
		CommandSyntax rcDelete = rc.addCommand("delete", p -> rcDeleteCommand(p));
		rcDelete.setPermission("eithonfixes.rcdelete");
		rcDelete.addParameter(rcExistingName);

		// rc goto
		CommandSyntax rcGoto = rc.addCommand("goto", p -> rcGotoCommand(p));
		rcGoto.setPermission("eithonfixes.rcgoto");
		rcGoto.addParameter(rcExistingName);

		commandSyntax.addCommand("list", p -> rcListCommand(p));
	}

	void buyCommand(CommandParser commandParser)
	{
		EithonPlayer eithonPlayer = commandParser.getEithonPlayer();
		if ((eithonPlayer != null) && (!eithonPlayer.isInAcceptableWorldOrInformPlayer(Config.V.buyWorlds))) return;

		Player buyingPlayer = commandParser.getArgument("player").asPlayer();
		if (buyingPlayer == null) return;


		String item = commandParser.getArgument("item").getStringAsLowerCase();
		double pricePerItem = commandParser.getArgument("price").getDouble();
		int amount = commandParser.getArgument("amount").getInteger();

		this._controller.buy(buyingPlayer, item, pricePerItem, amount);
	}

	void balanceCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return ;

		this._controller.displayBalance(player);
	}

	void debugCommand(CommandParser commandParser)
	{
		String pluginName = commandParser.getArgument("plugin").getString();
		int debugLevel = commandParser.getArgument("level").getInteger();
		CommandSender sender = commandParser.getSender();
		boolean success = this._controller.setPluginDebugLevel(sender, pluginName, debugLevel);
		if (!success) return;
		sender.sendMessage(String.format("Plugin  %s now has debug level %d", pluginName, debugLevel));
	}

	void rcAddCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").getString();
		String commands = commandParser.getArgument("command").getString();
		this._controller.rcAdd(player, name, commands);
	}

	void rcEditCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").getString();
		String commands = commandParser.getArgument("command").getString();
		this._controller.rcEdit(player, name, commands);
	}

	void rcSetCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").getString();

		for (String argumentName : new String[]{"OnEnter", "OnOtherWorld"}) {
			ParameterValue parameterValue = commandParser.getArgument(argumentName);
			if (parameterValue.hasValue()) this._controller.rcSet(argumentName, parameterValue.getBoolean());
		}
	}

	void rcDeleteCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").getString();
		this._controller.rcDelete(player, name);
	}

	void rcGotoCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").getString();
		this._controller.rcGoto(player, name);
	}

	void rcListCommand(CommandParser commandParser)
	{
		this._controller.rcList(commandParser.getSender());
	}

	void spAddCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").getString();
		long distance = commandParser.getArgument("distance").getLong();
		this._controller.spAdd(player, name, distance);
	}

	void spEditCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").getString();
		long distance = commandParser.getArgument("distance").getLong();
		this._controller.spEdit(player, name, distance);
	}

	void spDeleteCommand(CommandParser commandParser)
	{
		String name = commandParser.getArgument("name").getString();
		this._controller.spDelete(commandParser.getSender(), name);
	}

	void spGotoCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").getString();
		this._controller.rcGoto(player, name);
	}

	void spListCommand(CommandParser commandParser)
	{
		this._controller.spList(commandParser.getSender());
	}

	void restartCommand(CommandParser commandParser)
	{
		CommandSender sender = commandParser.getSender();
		if (sender == null) return;

		String cancel = commandParser.getArgument("cancel").getString();
		if ((cancel != null) && cancel.startsWith("ca")) {
			boolean success = this._controller.cancelRestart();
			if (success) sender.sendMessage("The server restart has been cancelled.");
			else sender.sendMessage("Too late to cancel server restart.");
			return;
		}

		long secondsToRestart = arguments.getTimeSpanAsSeconds(10*60);
		LocalDateTime when = this._controller.initiateRestart(secondsToRestart);
		if (when == null) sender.sendMessage("Could not initiate a restart.");
		else sender.sendMessage(String.format("The server will be restarted %s", when.toString()));
	}

	void testCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayer();
		player.sendMessage(String.format("TEST by player %s", player.getName()));
	}

	void serverCommand(CommandParser commandParser)
	{
		String serverName = commandParser.getArgument("serverName").getString();
		Player player = commandParser.getPlayer();
		boolean success = this._controller.connectPlayerToServer(player, serverName);
		if (!success) return;
		Config.M.connectedToServer.sendMessage(player, serverName);
	}
}