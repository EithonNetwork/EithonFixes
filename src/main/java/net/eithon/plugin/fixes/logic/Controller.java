package net.eithon.plugin.fixes.logic;

import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.CoolDown;
import net.eithon.plugin.fixes.Config;
import net.eithon.plugin.fixes.Config.C;
import net.eithon.plugin.fixes.Config.M;
import net.eithon.plugin.fixes.Config.V;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class Controller {
	private HashMap<UUID, CoolDown> _coolDownHashMap;

	public Controller(EithonPlugin plugin){
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
			buyingPlayer.sendMessage(String.format(
					Config.M.youNeedMoreMoney.getFormat(), totalPrice, amount, item, balance));
			return;
		}

		Config.C.take.execute(buyingPlayer.getName(), totalPrice);
		Config.C.give.execute(buyingPlayer.getName(), item, amount);

		Config.M.successfulPurchase.sendMessage(buyingPlayer, amount, item);
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

	public boolean commandShouldBeCancelled(Player player, String command) {
		CoolDown coolDown = getCoolDown(command);
		if (coolDown == null) return false;
		if (coolDown.isInCoolDownPeriod(player)) return true;
		coolDown.addPlayer(player);
		return false;
	}

	private CoolDown getCoolDown(String command) {
		CoolDownInfo info = getCoolDownInfo(command);
		if (info == null) return null;
		return this._coolDownHashMap.get(info.getId());
	}

	private CoolDownInfo getCoolDownInfo(String command) {
		return null;
	}
}