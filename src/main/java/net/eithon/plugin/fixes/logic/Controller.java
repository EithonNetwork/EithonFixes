package net.eithon.plugin.fixes.logic;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.fixes.Config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Controller {
	private KillerMoneyController _killerMoneyController;
	private BuyController _buyController;
	private RegionCommandController _regionCommandController;
	private CoolDownController _coolDownController;

	public Controller(EithonPlugin plugin){
		this._killerMoneyController = new KillerMoneyController();
		this._buyController = new BuyController(plugin);
		this._regionCommandController = new RegionCommandController(plugin);
		this._coolDownController = new CoolDownController(plugin);
	}

	void disable() {
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

	public void buy(Player buyingPlayer, String item, double price, int amount)
	{
		this._buyController.buy(buyingPlayer, item, price, amount);
	}

	public void displayBalance(Player player) {
		this._buyController.displayBalance(player);
	}
	
	public long secondsLeftOfCoolDown(Player player, String command) {
		return this._coolDownController.secondsLeftOfCoolDown(player, command);
	}

	public double getReductedMoney(Player player, double money) {
		return this._killerMoneyController.getReductedMoney(player, money);
	}

	public void rcAdd(Player player, String name, String command) {
		this._regionCommandController.updateOrCreateRegionCommand(player, name, command, true);
	}

	public void rcEdit(Player player, String name, String command) {
		this._regionCommandController.editRegionCommand(player, name, command, true);
	}

	public void rcDelete(CommandSender sender, String name) {
		this._regionCommandController.deleteRegionCommand(sender, name);
	}

	public void rcGoto(Player player, String name) {
		this._regionCommandController.gotoRegionCommand(player, name);
	}

	public void rcList(CommandSender sender) {
		this._regionCommandController.listRegionCommands(sender);
	}
	
}