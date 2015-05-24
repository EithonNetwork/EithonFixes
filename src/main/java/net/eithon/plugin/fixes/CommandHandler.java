package net.eithon.plugin.fixes;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String BUY_COMMAND = "/eithonfixes buy <player> <item> <price> <amount>";
	private static final String BALANCE_COMMAND = "/eithonfixes balance";
	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	public boolean onCommand(CommandParser commandParser) {
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1)) return true;

		String command = commandParser.getArgumentStringAsLowercase(0);
		commandParser.setCurrentCommand(command);

		if (command.equals("buy")) {
			buyCommand(commandParser);
		} else if (command.equals("balance")) {
			balanceCommand(commandParser);
		} else if (command.equals("join")) {
			joinCommand(commandParser);
		} else {
			commandParser.showCommandSyntax();
		}
		return true;
	}

	void buyCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.buy")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(4, 5)) return;

		Player buyingPlayer = commandParser.getArgumentPlayer(1, null);
		if (buyingPlayer == null) return;


		String item = commandParser.getArgumentStringAsLowercase(2);
		double pricePerItem = commandParser.getArgumentDouble(3, Double.MAX_VALUE);
		if (pricePerItem == Double.MAX_VALUE) return;
		int amount = commandParser.getArgumentInteger(4, 1);

		this._controller.buy(buyingPlayer, item, pricePerItem, amount);
	}

	void balanceCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.balance")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		this._controller.balance(commandParser.getSender());
	}

	void joinCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.join")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;
		
		String channel = commandParser.getArgumentString();
		this._controller.joinChannel(channel);
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("buy")) {
			sender.sendMessage(BUY_COMMAND);
		} else if (command.equals("balance")) {
			sender.sendMessage(BALANCE_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}
	}
}