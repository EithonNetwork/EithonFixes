package net.eithon.plugin.fixes;

import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands {
	private static Commands singleton = null;
	private static final String BUY_COMMAND = "/eithonfixes buy <player> <item> <price> <amount>";
	private static final String BALANCE_COMMAND = "/eithonfixes balance";

	private Commands() {
	}

	static Commands get()
	{
		if (singleton == null) {
			singleton = new Commands();
		}
		return singleton;
	}

	void enable(EithonPlugin eithonPlugin){
	}

	void disable() {
	}

	public boolean onCommand(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Incomplete command...");
			return false;
		}

		String command = args[0].toLowerCase();
		if (command.equals("buy")) {
			Commands.get().buyCommand(sender, args);
		} else if (command.equals("balance")) {
				Commands.get().balanceCommand(sender, args);
		} else {
			sender.sendMessage("Could not understand command.");
			return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	void buyCommand(CommandSender sender, String[] args)
	{
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "eithonfixes.buy")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 4, 5)) {
			sender.sendMessage(BUY_COMMAND);
			return;
		}

		Player buyingPlayer = null;
		try {
			UUID id = UUID.fromString(args[1]);
			buyingPlayer = Bukkit.getPlayer(id);
		} catch (Exception e) {
		}
		if (buyingPlayer == null) buyingPlayer = Bukkit.getPlayer(args[1]);
		if (buyingPlayer == null) {
			sender.sendMessage(String.format("Unknown player: %s", args[1]));
			return;
		}

		String item = args[2];
		double pricePerItem;
		try {
			pricePerItem = Float.parseFloat(args[3]);
		} catch (Exception e) {
			sender.sendMessage(BUY_COMMAND);
			return;			
		}
		int amount = 1;
		if (args.length > 4) {
			try {
				amount = Integer.parseInt(args[4]);
			} catch (Exception e) {
				sender.sendMessage(BUY_COMMAND);
				return;			
			}
		}

		Fixes.get().buy(buyingPlayer, item, pricePerItem, amount);
	}

	void balanceCommand(CommandSender sender, String[] args)
	{
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "eithonfixes.balance")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			sender.sendMessage(BALANCE_COMMAND);
			return;
		}		

		Fixes.get().balance(sender);
	}


	private boolean verifyPermission(Player player, String permission)
	{
		if (player.hasPermission(permission)) return true;
		player.sendMessage("You must have permission " + permission);
		return false;
	}

	private boolean arrayLengthIsWithinInterval(Object[] args, int min, int max) {
		return (args.length >= min) && (args.length <= max);
	}
}
