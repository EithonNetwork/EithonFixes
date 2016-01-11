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
		setupRcCommand(commandSyntax);
		setupSpCommand(commandSyntax);
		setupBalanceCommand(commandSyntax);
		setupServerCommand(commandSyntax);
		setupDebugCommand(commandSyntax);
		
		this._commandSyntax = commandSyntax;
	}
	
	@Override
	public CommandSyntax getCommandSyntax()
	{
		return this._commandSyntax;
	}

	public void setupDebugCommand(CommandSyntax commandSyntax) {
		CommandSyntax debug = commandSyntax.addCommand("debug", p -> debugCommand(p));
		debug.setPermission("eithonfixes.debug");
		debug.addParameter(ParameterType.STRING, "plugin");
		ParameterSyntax argument = debug.addParameter(ParameterType.INTEGER, "level");
		argument.setValues(0, 1, 2, 3);
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
		spAdd.addParameter(ParameterType.INTEGER, "distance");
		
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

	public void setupBuyCommand(CommandSyntax commandSyntax) {
		// buy
		CommandSyntax buy = commandSyntax.addCommand("buy", p -> buyCommand(p));
		buy.setPermission("eithonfixes.buy");
		buy.addParameterPlayer("player");
		buy.addParameter(ParameterType.STRING, "item");
		buy.addParameter(ParameterType.REAL, "price");
		buy.addParameter(ParameterType.INTEGER, "amount");
	}

	void buyCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();
		EithonPlayer eithonPlayer = commandParser.getEithonPlayer();
		if ((eithonPlayer != null) && (!eithonPlayer.isInAcceptableWorldOrInformPlayer(Config.V.buyWorlds))) return;

		Player buyingPlayer = arguments.getPlayer(null);
		if (buyingPlayer == null) return;


		String item = arguments.getStringAsLowercase();
		double pricePerItem = arguments.getDouble(Double.MAX_VALUE);
		if (pricePerItem == Double.MAX_VALUE) return;
		int amount = arguments.getInteger(1);

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
		CommandArguments arguments = commandParser.getArguments();

		String pluginName = arguments.getString();
		int debugLevel = arguments.getInteger(0);
		CommandSender sender = commandParser.getSender();
		boolean success = this._controller.setPluginDebugLevel(sender, pluginName, debugLevel);
		if (!success) return;
		sender.sendMessage(String.format("Plugin  %s now has debug level %d", pluginName, debugLevel));
	}

	void rcAddCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = arguments.getString();
		String commands = arguments.getRest();
		this._controller.rcAdd(player, name, commands);
	}

	void rcEditCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = arguments.getString();
		String commands = arguments.getRest();
		this._controller.rcEdit(player, name, commands);
	}

	void rcSetCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = arguments.getString();
		boolean onEnter = arguments.getBoolean(true);
		boolean onOtherWorld = arguments.getBoolean(true);
		this._controller.rcSet(player, name, onEnter, onOtherWorld);
	}

	void rcDeleteCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = arguments.getString();
		this._controller.rcDelete(player, name);
	}

	void rcGotoCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = arguments.getString();
		this._controller.rcGoto(player, name);
	}

	void rcListCommand(CommandParser commandParser)
	{
		this._controller.rcList(commandParser.getSender());
	}

	void spAddCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = arguments.getString();
		long distance = arguments.getInteger(10);
		this._controller.spAdd(player, name, distance);
	}

	void spEditCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = arguments.getString();
		long distance = arguments.getInteger(10);
		this._controller.spEdit(player, name, distance);
	}

	void spDeleteCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		String name = arguments.getString();
		this._controller.spDelete(commandParser.getSender(), name);
	}

	void spGotoCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;

		String name = arguments.getString();
		this._controller.rcGoto(player, name);
	}

	void spListCommand(CommandParser commandParser)
	{
		this._controller.spList(commandParser.getSender());
	}

	void restartCommand(CommandParser commandParser)
	{
		CommandArguments arguments = commandParser.getArguments();

		CommandSender sender = commandParser.getSender();
		if (sender == null) return;

		String cancel = arguments.getString();
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
		CommandArguments arguments = commandParser.getArguments();
		String serverName = arguments.getStringAsLowercase();
		Player player = commandParser.getPlayer();
		boolean success = this._controller.connectPlayerToServer(player, serverName);
		if (!success) return;
		Config.M.connectedToServer.sendMessage(player, serverName);
	}
}