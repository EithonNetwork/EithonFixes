package net.eithon.plugin.fixes;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.library.time.CountDown;
import net.eithon.library.time.ICountDownListener;
import net.eithon.library.title.Title;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String BUY_COMMAND = "/eithonfixes buy <player> <item> <price> <amount>";
	private static final String BALANCE_COMMAND = "/eithonfixes balance";
	private static final String JOIN_COMMAND = "/eithonfixes join <channel>";
	private static final String RCADD_COMMAND = "/eithonfixes rcadd <name> <command>";
	private static final String RCEDIT_COMMAND = "/eithonfixes rcedit <name> <command>";
	private static final String RCDELETE_COMMAND = "/eithonfixes rcdelete <name>";
	private static final String RCGOTO_COMMAND = "/eithonfixes rcgoto <name>";
	private static final String RCLIST_COMMAND = "/eithonfixes rclist";
	private Controller _controller;
	private EithonPlugin _eithonPlugin;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
	}

	public boolean onCommand(CommandParser commandParser) {
		String command = commandParser.getArgumentCommand();
		if (command == null) return false;

		if (command.equals("buy")) {
			buyCommand(commandParser);
		} else if (command.equals("balance")) {
			balanceCommand(commandParser);
		} else if (command.equals("join")) {
			joinCommand(commandParser);
		} else if (command.equals("rcadd")) {
			rcAddCommand(commandParser);
		} else if (command.equals("rcedit")) {
			rcEditCommand(commandParser);
		} else if (command.equals("rcdelete")) {
			rcDeleteCommand(commandParser);
		} else if (command.equals("rcgoto")) {
			rcGotoCommand(commandParser);
		} else if (command.equals("rclist")) {
			rcListCommand(commandParser);
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

	void joinCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.join")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;
		
		String channel = commandParser.getArgumentString();
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;
		this._controller.joinChannel(player, channel);
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

	void rcDeleteCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.rcdelete")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;
		
		String name = commandParser.getArgumentString();
		this._controller.rcDelete(commandParser.getSender(), name);
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

	void testCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithonfixes.test")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 3)) return;

		long counts = commandParser.getArgumentInteger(3);
		int intervalLengthInTicks = commandParser.getArgumentInteger(20);
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return;
		
		CountDown countDown = new CountDown(this._eithonPlugin, counts, intervalLengthInTicks*50, new ICountDownListener() {
			public boolean isCancelled(long remainingIntervals) {
				Title.get().sendActionbarMessage(player, String.format("%d", remainingIntervals));
				return false;
			}
			public void afterDoneTask() {
				Title.get().sendFloatingText(player, "Done", 0, 20, 20);
				Title.get().sendActionbarMessage(player, "");
			}
			@Override
			public void afterCancelTask() {
				Title.get().sendFloatingText(player, "Cancelled", 0, 20, 20);
				Title.get().sendActionbarMessage(player, "");
			}
		});
		
		countDown.start(Bukkit.getScheduler());
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("buy")) {
			sender.sendMessage(BUY_COMMAND);
		} else if (command.equals("balance")) {
			sender.sendMessage(BALANCE_COMMAND);
		} else if (command.equals("join")) {
			sender.sendMessage(JOIN_COMMAND);
		} else if (command.equals("rcadd")) {
			sender.sendMessage(RCADD_COMMAND);
		} else if (command.equals("rcedit")) {
			sender.sendMessage(RCEDIT_COMMAND);
		} else if (command.equals("rcdelete")) {
			sender.sendMessage(RCDELETE_COMMAND);
		} else if (command.equals("rcgoto")) {
			sender.sendMessage(RCGOTO_COMMAND);
		} else if (command.equals("rclist")) {
			sender.sendMessage(RCLIST_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}
	}
}