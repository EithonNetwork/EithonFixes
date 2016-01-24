package net.eithon.plugin.fixes;

import java.time.LocalDateTime;

import net.eithon.library.command.CommandSyntaxException;
import net.eithon.library.command.EithonCommand;
import net.eithon.library.command.EithonCommandUtilities;
import net.eithon.library.command.ICommandSyntax;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.fixes.logic.Controller;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {
	private Controller _controller;
	private ICommandSyntax _commandSyntax;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;

		ICommandSyntax commandSyntax = EithonCommand.createRootCommand("eithonfixes");
		commandSyntax.setPermissionsAutomatically();

		try {
			commandSyntax
			.parseCommandSyntax("server <name>")
			.setCommandExecutor(p -> serverCommand(p));
			setupResetCommand(commandSyntax);
			setupBuyCommand(commandSyntax);
			setupDebugCommand(commandSyntax);
			setupRcCommand(commandSyntax);
			setupSpCommand(commandSyntax);
			setupBalanceCommand(commandSyntax);
		} catch (CommandSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this._commandSyntax = commandSyntax;
	}

	public void setupResetCommand(ICommandSyntax commandSyntax)
			throws CommandSyntaxException {
		commandSyntax.parseCommandSyntax("restart <time : TIME_SPAN {10m, ...}>")
		.setCommandExecutor(p -> restartCommand(p));
		commandSyntax.parseCommandSyntax("restart cancel")
		.setCommandExecutor(p -> restartCancelCommand(p));
	}

	public ICommandSyntax getCommandSyntax() { return this._commandSyntax;	}

	public void setupBalanceCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		ICommandSyntax balance = commandSyntax.parseCommandSyntax("balance <player>")
				.setCommandExecutor(p -> balanceCommand(p));

		balance
		.getParameterSyntax("player")
		.setExampleValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec))
		.setDefaultGetter(ec -> getSenderName(ec));
	}

	private String getSenderName(EithonCommand command) {
		return command.getSender().getName();
	}

	public void setupBuyCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		// buy <player> <item> <price> [<amount>]
		ICommandSyntax buy = commandSyntax.parseCommandSyntax("buy <player> <item> <price : REAL> <amount : INTEGER {_1_, ...}>")
				.setCommandExecutor(eithonCommand -> buyCommand(eithonCommand));
		buy
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec));
	}

	public void setupDebugCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		commandSyntax.parseCommandSyntax("debug <plugin> <level : INTEGER {0, 1, 2, _3_}>")
		.setCommandExecutor(p -> debugCommand(p));
	}

	public void setupSpCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		ICommandSyntax sp = commandSyntax.addKeyWord("sp");
		ICommandSyntax subCommand;

		// sp add
		sp
		.parseCommandSyntax("add <name> <distance : INTEGER {10,...}>")
		.setCommandExecutor(ec -> spAddCommand(ec));

		// sp edit
		subCommand = sp
				.parseCommandSyntax("edit <name> <distance : INTEGER {10,...}>")
				.setCommandExecutor(ec -> spEditCommand(ec));

		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllSpawnPointNames());

		// sp delete
		subCommand = sp.parseCommandSyntax("delete <name>")
				.setCommandExecutor(ec -> spDeleteCommand(ec));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(ec -> this._controller.getAllSpawnPointNames());

		// sp goto
		subCommand = sp
				.parseCommandSyntax("goto <name>")
				.setCommandExecutor(p -> spDeleteCommand(p));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(ec -> this._controller.getAllSpawnPointNames());
	}

	public void setupRcCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		ICommandSyntax rc = commandSyntax.addKeyWord("rc");
		ICommandSyntax subCommand;

		// rc add
		rc.parseCommandSyntax("add <name> <command ...>")
		.setCommandExecutor(ec -> rcAddCommand(ec));

		// rc edit
		subCommand = rc
				.parseCommandSyntax("edit <name> <command ...>")
				.setCommandExecutor(ec -> rcEditCommand(ec));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllRegionCommands());

		// rc set
		subCommand = rc
				.parseCommandSyntax("set <name> OnEnter=<on-enter : BOOLEAN> OnWorld=<on-world : BOOLEAN>")
				.setCommandExecutor(ec -> rcSetCommand(ec));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(ec -> this._controller.getAllRegionCommands());

		// rc delete
		subCommand = rc
				.parseCommandSyntax("delete <name>")
				.setCommandExecutor(ec -> rcDeleteCommand(ec));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(ec -> this._controller.getAllRegionCommands());

		// rc goto
		subCommand = rc
				.parseCommandSyntax("goto <name>")
				.setCommandExecutor(ec -> rcDeleteCommand(ec));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(ec -> this._controller.getAllRegionCommands());
	}

	void buyCommand(EithonCommand command)
	{
		EithonPlayer eithonPlayer = command.getEithonPlayer();
		if ((eithonPlayer != null) && (!eithonPlayer.isInAcceptableWorldOrInformPlayer(Config.V.buyWorlds))) return;

		Player buyingPlayer = command.getArgument("player").asPlayer();
		if (buyingPlayer == null) return;

		String item = command.getArgument("item").asString();
		double pricePerItem = command.getArgument("price").asDouble();
		int amount = command.getArgument("amount").asInteger();

		this._controller.buy(buyingPlayer, item, pricePerItem, amount);
	}

	void balanceCommand(EithonCommand command)
	{
		CommandSender sender = command.getSender();
		OfflinePlayer player = command.getArgument("player").asOfflinePlayer();
		if (player == null) return;

		this._controller.displayBalance(sender, player);
	}

	void debugCommand(EithonCommand command)
	{
		String pluginName = command.getArgument("plugin").asString();
		int debugLevel = command.getArgument("level").asInteger();
		CommandSender sender = command.getSender();
		boolean success = this._controller.setPluginDebugLevel(sender, pluginName, debugLevel);
		if (!success) return;
		sender.sendMessage(String.format("Plugin %s now has debug level %d", pluginName, debugLevel));
	}

	void rcAddCommand(EithonCommand command)
	{
		Player player = command.getPlayerOrInformSender();
		if (player == null) return;

		String name = command.getArgument("name").asString();
		String commands = command.getArgument("command").asString();
		this._controller.rcAdd(player, name, commands);
	}

	void rcEditCommand(EithonCommand command)
	{
		Player player = command.getPlayerOrInformSender();
		if (player == null) return;

		String name = command.getArgument("name").asString();
		String commands = command.getArgument("command").asString();
		this._controller.rcEdit(player, name, commands);
	}

	void rcSetCommand(EithonCommand command)
	{
		Player player = command.getPlayerOrInformSender();
		if (player == null) return;

		command.getArgument("name").asString();

		for (String argumentName : new String[]{"OnEnter", "OnOtherWorld"}) {
			command.getArgument(argumentName);
			throw new NotImplementedException();
			//if (parameterValue.hasValue()) this._controller.rcSet(player, name, argumentName, parameterValue.asBoolean());
		}
	}

	void rcDeleteCommand(EithonCommand command)
	{
		Player player = command.getPlayerOrInformSender();
		if (player == null) return;

		String name = command.getArgument("name").asString();
		this._controller.rcDelete(player, name);
	}

	void rcGotoCommand(EithonCommand command)
	{
		Player player = command.getPlayerOrInformSender();
		if (player == null) return;

		String name = command.getArgument("name").asString();
		this._controller.rcGoto(player, name);
	}

	void rcListCommand(EithonCommand command)
	{
		this._controller.rcList(command.getSender());
	}

	void spAddCommand(EithonCommand command)
	{
		Player player = command.getPlayerOrInformSender();
		if (player == null) return;

		String name = command.getArgument("name").asString();
		long distance = command.getArgument("distance").asLong();
		this._controller.spAdd(player, name, distance);
	}

	void spEditCommand(EithonCommand command)
	{
		Player player = command.getPlayerOrInformSender();
		if (player == null) return;

		String name = command.getArgument("name").asString();
		long distance = command.getArgument("distance").asLong();
		this._controller.spEdit(player, name, distance);
	}

	void spDeleteCommand(EithonCommand command)
	{
		String name = command.getArgument("name").asString();
		this._controller.spDelete(command.getSender(), name);
	}

	void spGotoCommand(EithonCommand command)
	{
		Player player = command.getPlayerOrInformSender();
		if (player == null) return;

		String name = command.getArgument("name").asString();
		this._controller.rcGoto(player, name);
	}

	void spListCommand(EithonCommand command)
	{
		this._controller.spList(command.getSender());
	}

	void restartCommand(EithonCommand command)
	{
		CommandSender sender = command.getSender();
		if (sender == null) return;

		long secondsToRestart = command.getArgument("time").asSeconds();
		LocalDateTime when = this._controller.initiateRestart(secondsToRestart);
		if (when == null) sender.sendMessage("Could not initiate a restart.");
		else sender.sendMessage(String.format("The server will be restarted %s", when.toString()));
	}

	void restartCancelCommand(EithonCommand command)
	{
		CommandSender sender = command.getSender();
		if (sender == null) return;

		boolean success = this._controller.cancelRestart();
		if (success) sender.sendMessage("The server restart has been cancelled.");
		else sender.sendMessage("Too late to cancel server restart.");
	}

	void testCommand(EithonCommand command)
	{
		Player player = command.getPlayer();
		player.sendMessage(String.format("TEST by player %s", player.getName()));
	}

	void serverCommand(EithonCommand command)
	{
		String serverName = command.getArgument("name").asString();
		Player player = command.getPlayer();
		boolean success = this._controller.connectPlayerToServer(player, serverName);
		if (!success) return;
		Config.M.connectedToServer.sendMessage(player, serverName);
	}
}