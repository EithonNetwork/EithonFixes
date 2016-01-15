package net.eithon.plugin.fixes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import net.eithon.library.command.Argument;
import net.eithon.library.command.EithonCommand;
import net.eithon.library.command.syntax.CommandSyntax;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.fixes.logic.Controller;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {
	private Controller _controller;
	private CommandSyntax _commandSyntax;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;

		CommandSyntax commandSyntax = new CommandSyntax("eithonfixes");
		commandSyntax.setPermissionsAutomatically();

		setupBuyCommand(commandSyntax);
		setupDebugCommand(commandSyntax);
		setupRcCommand(commandSyntax);
		setupSpCommand(commandSyntax);
		setupBalanceCommand(commandSyntax);
		commandSyntax.addCommand("server <name>").setCommandExecutor(p -> serverCommand(p));
		commandSyntax.addCommand("restart <time : TIME_SPAN {10m, ...}>").setCommandExecutor(p -> serverCommand(p));
		this._commandSyntax = commandSyntax;
	}

	public CommandSyntax getCommandSyntax() { return this._commandSyntax;	}

	public void setupBalanceCommand(CommandSyntax commandSyntax) {
		CommandSyntax balance = commandSyntax.addCommand("balance <player>").setCommandExecutor(p -> balanceCommand(p));
		balance
		.getParameterSyntax("player")
		.setMandatoryValues(sender -> getOnlinePlayerNames(sender))
		.setDefaultValue(sender -> getSenderAsOnlinePlayer(sender));
	}


	private String getSenderAsOnlinePlayer(CommandSender sender) {
		return (sender instanceof Player) ? ((Player) sender).getName() : null;
	}

	private List<String> getOnlinePlayerNames(CommandSender sender) {
		return sender.getServer().getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toList());
	}

	public void setupBuyCommand(CommandSyntax commandSyntax) {
		// buy <player> <item> <price> [<amount>]
		CommandSyntax buy = commandSyntax.addCommand("buy <player> <item : REAL> <price : FLOAT> <amount : INTEGER {1, ...}>")
				.setCommandExecutor(eithonCommand -> buyCommand(eithonCommand));
		buy
		.getParameterSyntax("player")
		.setMandatoryValues(sender -> getOnlinePlayerNames(sender));
	}

	public void setupDebugCommand(CommandSyntax commandSyntax) {
		CommandSyntax debug = commandSyntax.addCommand("debug <plugin> <level : INTEGER {0, 1, 2, _3_}");
		debug.setCommandExecutor(p -> debugCommand(p));
	}

	public void setupSpCommand(CommandSyntax commandSyntax) {
		CommandSyntax sp = commandSyntax.addCommand("sp");
		CommandSyntax subCommand;

		// sp add
		sp.addCommand("add <name> <distance : INTEGER {10,...}>")
		.setCommandExecutor(p -> spAddCommand(p));

		// sp edit
		subCommand = sp.addCommand("edit <name> <distance : INTEGER {10,...}>")
				.setCommandExecutor(p -> spEditCommand(p));
		
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllSpawnPointNames());

		// sp delete
		subCommand = sp.addCommand("delete <name>")
				.setCommandExecutor(p -> spDeleteCommand(p));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllSpawnPointNames());

		// sp goto
		subCommand = sp.addCommand("goto <name>")
				.setCommandExecutor(p -> spDeleteCommand(p));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllSpawnPointNames());
	}

	public void setupRcCommand(CommandSyntax commandSyntax) {
		CommandSyntax rc = commandSyntax.addCommand("rc");
		CommandSyntax subCommand;

		// rc add
		rc.addCommand("add <name> <command ...>")
		.setCommandExecutor(p -> rcAddCommand(p));

		// rc edit
		subCommand = rc.addCommand("edit <name> <command ...>")
				.setCommandExecutor(p -> rcEditCommand(p));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllRegionCommands());

		// rc set
		subCommand = rc.addCommand("edit set <name> OnEnter=<on-enter : BOOLEAN> OnWorld=<on-world : BOOLEAN>")
				.setCommandExecutor(p -> rcSetCommand(p));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllRegionCommands());

		// rc delete
		subCommand = rc.addCommand("delete <name>")
				.setCommandExecutor(p -> rcDeleteCommand(p));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllRegionCommands());

		// rc goto
		subCommand = rc.addCommand("goto <name>")
				.setCommandExecutor(p -> rcDeleteCommand(p));
		subCommand
		.getParameterSyntax("name")
		.setMandatoryValues(sender -> this._controller.getAllRegionCommands());
	}

	void buyCommand(EithonCommand command)
	{
		EithonPlayer eithonPlayer = command.getEithonPlayer();
		if ((eithonPlayer != null) && (!eithonPlayer.isInAcceptableWorldOrInformPlayer(Config.V.buyWorlds))) return;

		Player buyingPlayer = command.getArgument("player").asPlayer();
		if (buyingPlayer == null) return;

		String item = command.getArgument("item").asLowerCase();
		double pricePerItem = command.getArgument("price").asDouble();
		int amount = command.getArgument("amount").asInteger();

		this._controller.buy(buyingPlayer, item, pricePerItem, amount);
	}

	void balanceCommand(EithonCommand command)
	{
		CommandSender sender = command.getSender();
		Player player = command.getArgument("player").asPlayer();

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

		String name = command.getArgument("name").asString();

		for (String argumentName : new String[]{"OnEnter", "OnOtherWorld"}) {
			Argument parameterValue = command.getArgument(argumentName);
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

		String cancel = command.getArgument("cancel").asString();
		if ((cancel != null) && cancel.startsWith("ca")) {
			boolean success = this._controller.cancelRestart();
			if (success) sender.sendMessage("The server restart has been cancelled.");
			else sender.sendMessage("Too late to cancel server restart.");
			return;
		}

		long secondsToRestart = command.getArgument("time").asSeconds();
		LocalDateTime when = this._controller.initiateRestart(secondsToRestart);
		if (when == null) sender.sendMessage("Could not initiate a restart.");
		else sender.sendMessage(String.format("The server will be restarted %s", when.toString()));
	}

	void testCommand(EithonCommand command)
	{
		Player player = command.getPlayer();
		player.sendMessage(String.format("TEST by player %s", player.getName()));
	}

	void serverCommand(EithonCommand command)
	{
		String serverName = command.getArgument("serverName").asString();
		Player player = command.getPlayer();
		boolean success = this._controller.connectPlayerToServer(player, serverName);
		if (!success) return;
		Config.M.connectedToServer.sendMessage(player, serverName);
	}
}