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
		setupRestartCommand(commandSyntax);

		this._commandSyntax = commandSyntax;
	}

	@Override
	public CommandSyntax getCommandSyntax()
	{
		return this._commandSyntax;
	}

	public void setupBuyCommand(CommandSyntax commandSyntax) {
		// buy <player> <item> <price> [<amount>]
		CommandSyntax buy = commandSyntax.addCommand(
				"buy", "eithonfixes.buy", p -> buyCommand(p));
		buy.addParameterPlayer("player");
		buy.addParameter(ParameterType.STRING, "item");
		buy.addParameter(ParameterType.REAL, "price");
		ParameterSyntax parameter = buy.addParameter(ParameterType.INTEGER, "amount");
		parameter.setDefault("1");
	}

	public void setupDebugCommand(CommandSyntax commandSyntax) {
		CommandSyntax debug = commandSyntax.addCommand(
				"debug", "eithonfixes.debug", p -> debugCommand(p));
		debug.addParameter(ParameterType.STRING, "plugin");
		ParameterSyntax parameter = debug.addParameter(ParameterType.INTEGER, "level");
		parameter.setValues("0", "1", "2", "3");
		parameter.setDefault("0");
	}

	public void setupBalanceCommand(CommandSyntax commandSyntax) {
		commandSyntax.addCommand("balance", p -> balanceCommand(p));
	}

	public void setupSpCommand(CommandSyntax commandSyntax) {
		CommandSyntax sp = commandSyntax.addCommand("sp");
		ParameterSyntax spExistingName = new ParameterSyntax(ParameterType.STRING, "name");
		spExistingName.SetValueGetter(() -> this._controller.getAllSpawnPointNames(), true);

		// sp add
		CommandSyntax spAdd = sp.addCommand(
				"add", "eithonfixes.spadd", p -> spAddCommand(p));
		spAdd.addParameter(ParameterType.STRING, "name");
		spAdd.setRestOfParametersAsOptional();
		ParameterSyntax parameter = spAdd.addParameter(ParameterType.INTEGER, "distance");
		parameter.setDefault("10");

		// sp edit
		CommandSyntax spEdit = sp.addCommand(
				"edit", "eithonfixes.spedit", p -> spEditCommand(p));
		spEdit.addParameter(spExistingName);
		spAdd.setRestOfParametersAsOptional();
		spAdd.addParameter(ParameterType.INTEGER, "distance");

		// sp delete
		CommandSyntax spDelete = sp.addCommand(
				"delete", "eithonfixes.spdelete", p -> spDeleteCommand(p));
		spDelete.addParameter(spExistingName);

		// sp goto
		CommandSyntax spGoto = sp.addCommand(
				"goto", "eithonfixes.spgoto", p -> spGotoCommand(p));
		spGoto.addParameter(spExistingName);

		sp.addCommand("list", "eithonfixes.splist", p -> spListCommand(p));
	}

	public void setupRcCommand(CommandSyntax commandSyntax) {

		CommandSyntax rc = commandSyntax.addCommand("rc");
		ParameterSyntax rcExistingName = new ParameterSyntax(ParameterType.STRING, "name");
		rcExistingName.SetValueGetter(() -> this._controller.getAllRegionCommands(), true);

		// rc add
		CommandSyntax rcAdd = rc.addCommand(
				"add", "eithonfixes.rcadd", p -> rcAddCommand(p));
		rcAdd.addParameter(ParameterType.STRING, "name");
		rcAdd.addParameter(ParameterType.REST, "command");

		// rc edit
		CommandSyntax rcEdit = rc.addCommand("edit", "eithonfixes.rcedit", p -> rcEditCommand(p));
		rcEdit.addParameter(rcExistingName);
		rcEdit.addParameter(ParameterType.REST, "command");

		// rc set
		CommandSyntax rcSet = rc.addCommand(
				"set", "eithonfixes.rcset", p -> rcSetCommand(p));
		rcSet.addParameter(rcExistingName);
		rcSet.addNamedParameter(ParameterType.BOOLEAN, "OnEnter");
		rcSet.addNamedParameter(ParameterType.BOOLEAN, "OnOtherWorld");

		// rc delete
		CommandSyntax rcDelete = rc.addCommand(
				"delete", "eithonfixes.rcdelete", p -> rcDeleteCommand(p));
		rcDelete.addParameter(rcExistingName);

		// rc goto
		CommandSyntax rcGoto = rc.addCommand(
				"goto", "eithonfixes.rcgoto", p -> rcGotoCommand(p));
		rcGoto.addParameter(rcExistingName);

		commandSyntax.addCommand("list", p -> rcListCommand(p));
	}

	public void setupServerCommand(CommandSyntax commandSyntax) {
		CommandSyntax server = commandSyntax.addCommand(
				"server", "eithonfixes.server", p -> serverCommand(p));
		server.addParameter(ParameterType.STRING, "name");
	}

	public void setupRestartCommand(CommandSyntax commandSyntax) {
		CommandSyntax restart = commandSyntax.addCommand(
				"restart", "eithonfixes.restart", p -> serverCommand(p));
		ParameterSyntax parameter = restart.addParameter(ParameterType.TIME_SPAN, "time");
		parameter.setDefault("10m");
	}

	void buyCommand(CommandParser commandParser)
	{
		EithonPlayer eithonPlayer = commandParser.getEithonPlayer();
		if ((eithonPlayer != null) && (!eithonPlayer.isInAcceptableWorldOrInformPlayer(Config.V.buyWorlds))) return;

		Player buyingPlayer = commandParser.getArgument("player").asPlayer();
		if (buyingPlayer == null) return;


		String item = commandParser.getArgument("item").asLowerCase();
		double pricePerItem = commandParser.getArgument("price").asDouble();
		int amount = commandParser.getArgument("amount").asInteger();

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
		String pluginName = commandParser.getArgument("plugin").asString();
		int debugLevel = commandParser.getArgument("level").asInteger();
		CommandSender sender = commandParser.getSender();
		boolean success = this._controller.setPluginDebugLevel(sender, pluginName, debugLevel);
		if (!success) return;
		sender.sendMessage(String.format("Plugin %s now has debug level %d", pluginName, debugLevel));
	}

	void rcAddCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").asString();
		String commands = commandParser.getArgument("command").asString();
		this._controller.rcAdd(player, name, commands);
	}

	void rcEditCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;
		
		String name = commandParser.getArgument("name").asString();
		String commands = commandParser.getArgument("command").asString();
		this._controller.rcEdit(player, name, commands);
	}

	void rcSetCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").asString();

		for (String argumentName : new String[]{"OnEnter", "OnOtherWorld"}) {
			ParameterValue parameterValue = commandParser.getArgument(argumentName);
			if (parameterValue.hasValue()) this._controller.rcSet(player, argumentName, parameterValue.asBoolean());
		}
	}

	void rcDeleteCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").asString();
		this._controller.rcDelete(player, name);
	}

	void rcGotoCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").asString();
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

		String name = commandParser.getArgument("name").asString();
		long distance = commandParser.getArgument("distance").asLong();
		this._controller.spAdd(player, name, distance);
	}

	void spEditCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").asString();
		long distance = commandParser.getArgument("distance").asLong();
		this._controller.spEdit(player, name, distance);
	}

	void spDeleteCommand(CommandParser commandParser)
	{
		String name = commandParser.getArgument("name").asString();
		this._controller.spDelete(commandParser.getSender(), name);
	}

	void spGotoCommand(CommandParser commandParser)
	{
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = commandParser.getArgument("name").asString();
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

		String cancel = commandParser.getArgument("cancel").asString();
		if ((cancel != null) && cancel.startsWith("ca")) {
			boolean success = this._controller.cancelRestart();
			if (success) sender.sendMessage("The server restart has been cancelled.");
			else sender.sendMessage("Too late to cancel server restart.");
			return;
		}

		long secondsToRestart = commandParser.getArgument("time").asSeconds();
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
		String serverName = commandParser.getArgument("serverName").asString();
		Player player = commandParser.getPlayer();
		boolean success = this._controller.connectPlayerToServer(player, serverName);
		if (!success) return;
		Config.M.connectedToServer.sendMessage(player, serverName);
	}
}