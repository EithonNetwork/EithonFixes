package net.eithon.plugin.eithonfixes;

import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class Fixes {
	private static Fixes singleton = null;
	private ConfigurableCommand _giveCommand;
	private ConfigurableCommand _takeCommand;
	private ConfigurableMessage _youNeedMoreMoneyMessage;
	private ConfigurableMessage _successfulPurchaseMessage;
	private ConfigurableMessage _currentBalanceMessage;
	private ConfigurableMessage _penaltyOnDeathMessage;
	private List<String> _penaltyOnDeathWorlds;
	private double _costOfDeath;

	private Fixes() {
	}

	static Fixes get()
	{
		if (singleton == null) {
			singleton = new Fixes();
		}
		return singleton;
	}

	void enable(EithonPlugin plugin){
		Configuration config = plugin.getConfiguration();
		this._penaltyOnDeathWorlds = config.getStringList("PenaltyOnDeathWorlds");
		this._costOfDeath = config.getDouble("CostOfDeath", 30.0);
		this._giveCommand = plugin.getConfigurableCommand("GiveCommand", 3,
				"give %s %s %d");
		this._takeCommand = plugin.getConfigurableCommand("TakeCommand", 2,
				"eco take %s %f");
		this._penaltyOnDeathMessage = plugin.getConfigurableMessage("messages.PenaltyOnDeath", 1,
				"Your death has resulted in a penalty of %.2f.");
		this._youNeedMoreMoneyMessage = plugin.getConfigurableMessage("messages.YouNeedMoreMoney", 4,
				"You need %.2f to buy %d %s. You have %.2f.");
		this._successfulPurchaseMessage = plugin.getConfigurableMessage("messages.SuccessfulPurchase", 2,
				"You successfully purchased %d item(s) of %s.");
		this._currentBalanceMessage = plugin.getConfigurableMessage("messages.CurrentBalance", 1,
				"Your balance is %.2f E-Coins.");
		Plugin ess = plugin.getJavaPlugin().getServer().getPluginManager().getPlugin("Economy");
		if (ess != null && ess.isEnabled()) {
			plugin.getDebug().info("Succesfully hooked into Essentials economy!");
		}	
	}

	void disable() {
	}

	@SuppressWarnings("deprecation")
	public void buy(Player buyingPlayer, String item, double price, int amount)
	{
		double totalPrice = amount*price;
		double balance;
		boolean hasEnough;
		String playerName = buyingPlayer.getName();
		try {
			balance = Economy.getMoney(playerName);
			hasEnough = Economy.hasEnough(playerName, totalPrice);
		} catch (UserDoesNotExistException e) {
			buyingPlayer.sendMessage(String.format("Could not find a user named \"%s\".", playerName));
			return;
		}
		if (!hasEnough) {
			try {
				buyingPlayer.sendMessage(String.format(
						this._youNeedMoreMoneyMessage.getFormat(), totalPrice, amount, item, balance));
			} catch (Exception e) {
				this._youNeedMoreMoneyMessage.reportFailure(buyingPlayer, e);
			}
			return;
		}

		this._takeCommand.execute(buyingPlayer.getName(), totalPrice);
		this._giveCommand.execute(buyingPlayer.getName(), item, amount);

		try {
			buyingPlayer.sendMessage(String.format(this._successfulPurchaseMessage.getFormat(), amount, item));
		} catch (Exception e) {
			this._successfulPurchaseMessage.reportFailure(buyingPlayer, e);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void balance(CommandSender sender) {
		Player player = (Player)sender;
		String playerName = player.getName();
		double balance;
		try {
			balance = Economy.getMoney(playerName);
		} catch (UserDoesNotExistException e) {
			sender.sendMessage(String.format("Could not find a user named \"%s\".", playerName));
			return;
		}
		try {
			sender.sendMessage(String.format(this._currentBalanceMessage.getFormat(), balance));
		} catch (Exception e) {
			this._currentBalanceMessage.reportFailure(sender, e);
		}
	}

	public void playerDied(Player player) {
		for (String penaltyWorld : this._penaltyOnDeathWorlds) {
			if (penaltyWorld.equalsIgnoreCase(player.getWorld().getName())) {
				this._takeCommand.execute(player.getName(), this._costOfDeath);
				this._penaltyOnDeathMessage.sendMessage(player, this._costOfDeath);
				break;
			}
		}
	}
}