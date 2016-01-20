package net.eithon.plugin.fixes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import net.eithon.library.command.CommandSyntaxException;
import net.eithon.library.command.EithonCommand;
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
			commandSyntax.parseCommandSyntax("restart <time : TIME_SPAN {10m, ...}>")
			.setCommandExecutor(p -> serverCommand(p));
			setupFreezeCommand(commandSyntax);
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

	public void setupFreezeCommand(ICommandSyntax commandSyntax)
			throws CommandSyntaxException {
		
		ICommandSyntax freeze = commandSyntax.addKeyWord("freeze");
		
		// freeze on
		ICommandSyntax on = freeze.parseCommandSyntax("on <player>")
		.setCommandExecutor(p -> freezeOnCommand(p));
		
		on
		.getParameterSyntax("player")
		.setMandatoryValues(sender -> getOnlinePlayerNames(sender));
		
		// freeze off
		ICommandSyntax off = freeze.parseCommandSyntax("off <player>")
		.setCommandExecutor(p -> freezeOffCommand(p));
		
		off
		.getParameterSyntax("player")
		.setMandatoryValues(sender->getFrozenPlayerNames());
		
		// freeze restore
		ICommandSyntax restore = freeze.parseCommandSyntax(String.format(
				"restore <player> <walk-speed:REAL {%.1f,...}> <fly-speed:REAL {%.1f,...}>",
				Config.V.freezeRestoreWalkSpeed, Config.V.freezeRestoreFlySpeed))
		.setCommandExecutor(ec -> freezeRestoreCommand(ec));
		
		restore
		.getParameterSyntax("player")
		.setMandatoryValues(sender -> getOnlinePlayerNames(sender));
		
		// freeze list
		freeze.parseCommandSyntax("list")
		.setCommandExecutor(p -> freezeListCommand(p));
	}

	private List<String> getFrozenPlayerNames() {
		return this._controller.getFrozenPlayerNames();
	}

	public ICommandSyntax getCommandSyntax() { return this._commandSyntax;	}

	public void setupBalanceCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		ICommandSyntax balance = commandSyntax.parseCommandSyntax("balance <player>")
				.setCommandExecutor(p -> balanceCommand(p));

		balance
		.getParameterSyntax("player")
		.setExampleValues(sender -> getOnlinePlayerNames(sender))
		.setDefaultValue(sender -> getSenderAsOnlinePlayer(sender));
	}

	private String getSenderAsOnlinePlayer(EithonCommand command) {
		return command.getPlayer().getName();
	}

	private List<String> getOnlinePlayerNames(EithonCommand command) {
		return command
				.getSender()
				.getServer()
				.getOnlinePlayers()
				.stream()
				.map(p -> p.getName())
				.collect(Collectors.toList());
	}

	public void setupBuyCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		// buy <player> <item> <price> [<amount>]
		ICommandSyntax buy = commandSyntax.parseCommandSyntax("buy <player> <item> <price : REAL> <amount : INTEGER {1, ...}>")
				.setCommandExecutor(eithonCommand -> buyCommand(eithonCommand));
		buy
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> getOnlinePlayerNames(ec));
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

		String item = command.getArgument("item").asLowerCase();
		double pricePerItem = command.getArgument("price").asDouble();
		int amount = command.getArgument("amount").asInteger();

		this._controller.buy(buyingPlayer, item, pricePerItem, amount);
	}

	private void freezeOnCommand(EithonCommand command) {
		CommandSender sender = command.getSender();
		Player player = command.getArgument("player").asPlayer();
		if (player == null) return;

		if (!this._controller.freezePlayer(sender, player)) return;
		Config.M.playerFrozen.sendMessage(sender, player.getName());
		
	}

	private void freezeOffCommand(EithonCommand command) {
		CommandSender sender = command.getSender();
		OfflinePlayer player = command.getArgument("player").asOfflinePlayer();
		if (player == null) return;

		if (!this._controller.thawPlayer(sender, player)) return;
		Config.M.playerThawn.sendMessage(sender, player.getName());
		
	}

	private void freezeRestoreCommand(EithonCommand command) {
		CommandSender sender = command.getSender();
		Player player = command.getArgument("player").asPlayer();
		if (player == null) return;
		float walkSpeed = command.getArgument("walk-speed").asFloat();
		float flySpeed = command.getArgument("fly-speed").asFloat();

		if (!this._controller.restorePlayer(sender, player, walkSpeed, flySpeed)) return;
		Config.M.playerRestored.sendMessage(sender, player.getName());
	}

	private void freezeListCommand(EithonCommand command) {
		CommandSender sender = command.getSender();

		this._controller.freezeList(sender);
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