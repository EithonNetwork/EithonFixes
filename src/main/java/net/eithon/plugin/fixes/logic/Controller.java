package net.eithon.plugin.fixes.logic;

import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.CoolDown;
import net.eithon.plugin.fixes.Config;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class Controller {
	private HashMap<UUID, CoolDown> _coolDownHashMap;
	private Logger _eithonLogger;

	public Controller(EithonPlugin plugin){
		CoolDownInfo.initialize(plugin);
		this._eithonLogger = plugin.getEithonLogger();
		Plugin ess = plugin.getServer().getPluginManager().getPlugin("Economy");
		if (ess != null && ess.isEnabled()) {
			plugin.getLogger().info("Succesfully hooked into Essentials economy!");
		}
		this._coolDownHashMap = new HashMap<UUID, CoolDown>();
		for (CoolDownInfo info : Config.V.coolDownInfos) {
			this._coolDownHashMap.put(info.getId(), new CoolDown(info.getName(), info.getCoolDownPeriodInSeconds()));
		}
	}

	void disable() {
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
			this._eithonLogger.warning("Could not find a material named \"%s\".", item);
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
		Config.M.currentBalance.sendMessage(sender, balance);
	}

	public void playerDied(Player player) {
		for (String penaltyWorld : Config.V.penaltyOnDeathWorlds) {
			if (penaltyWorld.equalsIgnoreCase(player.getWorld().getName())) {
				Config.C.take.execute(player.getName(), Config.V.costOfDeath);
				Config.M.penaltyOnDeath.sendMessage(player, Config.V.costOfDeath);
				break;
			}
		}
	}

	public void joinChannel(Player player, String channel) {
		for (String ch : Config.V.chatChannelsToLeave) {
			Config.C.leaveChat.execute(ch);
		}
		Config.C.joinChat.execute(channel);
		Config.M.joinedChat.sendMessage(player, channel);
	}

	public long secondsLeftOfCoolDown(Player player, String command) {
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: Enter");
		CoolDown coolDown = getCoolDown(command);
		if (coolDown == null) {
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: No cooldown found.");
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: return 0.");
			return 0;
		}
		
		if (player.hasPermission("eithonfixes.nocooldown")) {
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: Player \"%s\" has permission eithonfixes.nocooldown.", player.getName());
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: return 0.");
			return 0;			
		}
		
		long secondsLeft = coolDown.secondsLeft(player);
		if (secondsLeft > 0) {
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: Player \"%s\" is in cooldown.", player.getName());
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: return secondsLeft.");
			return secondsLeft;
		}
		if (secondsLeft > 0) {
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: Player \"%s\" is in cooldown.", player.getName());
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: return secondsLeft.");
			return secondsLeft;
		}
		coolDown.addPlayer(player);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: Player \"%s\" added to cooldown.", player.getName());
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "commandShouldBeCancelled: return 0.");
		return 0;
	}

	private CoolDown getCoolDown(String command) {
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "getCoolDown: Enter");
		CoolDownInfo info = getCoolDownInfo(command);
		if (info == null) {
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "getCoolDown: Command \"%s\" not found.", command);
			this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "getCoolDown: return null");
			return null;
		}
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "getCoolDown: Command \"%s\" found.", command);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "getCoolDown: return CoolDown object.");
		return this._coolDownHashMap.get(info.getId());
	}

	private CoolDownInfo getCoolDownInfo(String command) {
		for (CoolDownInfo info : Config.V.coolDownInfos) {
			if (info.isSame(command)) return info;
		}
		return null;
	}
}