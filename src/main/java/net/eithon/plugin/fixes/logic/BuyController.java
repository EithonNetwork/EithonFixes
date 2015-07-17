package net.eithon.plugin.fixes.logic;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.PluginMisc;
import net.eithon.plugin.fixes.Config;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.UserDoesNotExistException;

class BuyController {
	private Logger _eithonLogger;

	public BuyController(EithonPlugin plugin){
		this._eithonLogger = plugin.getEithonLogger();
		if (PluginMisc.isPluginEnabled("Economy")) {
			this._eithonLogger.info("Succesfully hooked into Essentials economy!");
		}
	}

	public void buy(Player buyingPlayer, String item, double price, int amount)
	{
		double totalPrice = amount*price;
		if (!hasEnoughOrInformPlayer(buyingPlayer, item, amount, totalPrice)) return;
		if (!hasRoomInInventoryOrInformPlayer(buyingPlayer, item, amount)) return;

		Config.C.take.execute(buyingPlayer.getName(), totalPrice);
		Config.C.give.execute(buyingPlayer.getName(), item, amount);

		Config.M.successfulPurchase.sendMessage(buyingPlayer, amount, item);
	}

	@SuppressWarnings("deprecation")
	public void displayBalance(Player player) {
		String playerName = player.getName();
		double balance;
		try {
			balance = Economy.getMoney(playerName);
		} catch (UserDoesNotExistException e) {
			player.sendMessage(String.format("Could not find a user named \"%s\".", playerName));
			return;
		}
		Config.M.currentBalance.sendMessage(player, balance);
	}

	@SuppressWarnings("deprecation")
	private boolean hasEnoughOrInformPlayer(Player buyingPlayer, String item, int amount,
			double totalPrice) {
		double balance;
		boolean hasEnough;
		String playerName = buyingPlayer.getName();
		try {
			balance = Economy.getMoney(playerName);
			hasEnough = Economy.hasEnough(playerName, totalPrice);
		} catch (UserDoesNotExistException e) {
			buyingPlayer.sendMessage(String.format("Could not find a user named \"%s\".", playerName));
			return false;
		}
		if (!hasEnough) {
			buyingPlayer.sendMessage(String.format(
					Config.M.youNeedMoreMoney.getFormat(), totalPrice, amount, item, balance));
			return false;
		}
		
		return true;
	}

	private boolean hasRoomInInventoryOrInformPlayer(Player buyingPlayer, String item, int amount) {
		PlayerInventory playerInventory = buyingPlayer.getInventory();
		if (playerInventory.firstEmpty() != -1) return true;
		Material material = Material.getMaterial(item);
		if (material == null) {
			String message = String.format("Could not find a material named \"%s\".", item);
			this._eithonLogger.warning("%s", message);
			buyingPlayer.sendMessage(message);
			return false;
		}
		int remainingAmount = amount;
		for (int i=0; i<playerInventory.getSize(); i++) {
			ItemStack itemStack = playerInventory.getItem(i);
			if (itemStack.getType().equals(material)) {
				int remainingInStack = itemStack.getMaxStackSize()-itemStack.getAmount();
				if (remainingInStack > 0) {
					remainingAmount -= remainingInStack;
					if (remainingAmount <= 0) return true;
				}
			}
		}
		Config.M.inventoryFull.sendMessage(buyingPlayer);
		return false;
	}
}