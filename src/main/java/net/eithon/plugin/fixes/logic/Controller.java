package net.eithon.plugin.fixes.logic;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.fixes.Config;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Controller {
	private KillerMoneyController _killerMoneyController;
	private BuyController _buyController;
	private RegionCommandController _regionCommandController;
	private CoolDownController _coolDownController;
	UUID _restartAlarmIdentity;
	private Logger _eithonLogger;
	private EithonPlugin _eithonPlugin;

	public Controller(EithonPlugin plugin) {
		this._eithonPlugin = plugin;
		this._eithonLogger = plugin.getEithonLogger();
		this._killerMoneyController = new KillerMoneyController(plugin);
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

	public LocalDateTime initiateRestart(Player player, long minutes) {
		LocalDateTime whenRestart = initiateRestartInternally(player, minutes*60+5);
		if (whenRestart == null) return null;
		setNextMessageAlarm(this._eithonPlugin, whenRestart);
		return whenRestart;
	}

	private LocalDateTime initiateRestartInternally(Player player, long seconds) {
		LocalDateTime whenRestart = LocalDateTime.now().plusSeconds(seconds);
		if (this._restartAlarmIdentity != null) {
			boolean done = AlarmTrigger.get().resetAlarm(this._restartAlarmIdentity, whenRestart);
			if (done) return whenRestart;
			this._restartAlarmIdentity = null;
			return null;
		}

		this._restartAlarmIdentity = AlarmTrigger.get().setAlarm("restart", whenRestart, new Runnable() {
			public void run() {
				Server server = Bukkit.getServer();
				server.dispatchCommand(server.getConsoleSender(), "restart");			
			}
		});
		return whenRestart;
	}

	public boolean cancelRestart(Player player) {
		if (this._restartAlarmIdentity == null) return true;
		boolean success = AlarmTrigger.get().removeAlarm(this._restartAlarmIdentity);
		this._restartAlarmIdentity = null;
		return success;
	}

	private void setNextMessageAlarm(EithonPlugin plugin, LocalDateTime whenRestart) {
		Controller thisObject = this;
		verbose("setNextMessageAlarm", "Enter, whenRestart = %s", whenRestart);
		long secondsLeft = whenRestart.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		long minutesLeft = secondsLeft/60;
		verbose("setNextMessageAlarm", "Left = %d seconds (%d minutes)", secondsLeft, minutesLeft);
		for (long minutes : Config.V.showEarlyWarningMessageMinutesBeforeRestart) {
			if (minutes < minutesLeft) {
				verbose("setNextMessageAlarm", "Found early = %d minutes, in %d seconds", minutes, (secondsLeft- minutes*60));
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (thisObject._restartAlarmIdentity == null) return;
						earlyWarningMessage(plugin, whenRestart, minutes);
					}
				}, TimeMisc.secondsToTicks((secondsLeft- minutes*60)));
				return;
			}
		}
		for (long seconds : Config.V.showMiddleWarningMessageSecondsBeforeRestart) {
			if (seconds < secondsLeft) {
				verbose("setNextMessageAlarm", "Found middle = %d seconds, in %d seconds", seconds, (secondsLeft-seconds));
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (thisObject._restartAlarmIdentity == null) return;
						middleWarningMessage(plugin, whenRestart, seconds);
					}
				}, TimeMisc.secondsToTicks(secondsLeft-seconds));
				return;
			}
		}
		for (long seconds : Config.V.showFinalWarningMessageSecondsBeforeRestart) {
			if (seconds < secondsLeft) {
				verbose("setNextMessageAlarm", "Found final = %d seconds, in %d seconds", seconds, (secondsLeft-seconds));
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (thisObject._restartAlarmIdentity == null) return;
						finalWarningMessage(plugin, whenRestart, seconds);
					}
				}, TimeMisc.secondsToTicks(secondsLeft-seconds));
				return;
			}
		}
		verbose("setNextMessageAlarm", "No message found so scheduling \"Restarting server\" message in %d seconds", secondsLeft);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (thisObject._restartAlarmIdentity == null) return;
				Config.M.restartingServer.broadcastMessage();
			}
		}, TimeMisc.secondsToTicks(secondsLeft));
	}

	void earlyWarningMessage(EithonPlugin plugin, LocalDateTime whenRestart, long minutes) {
		verbose("earlyWarningMessage", " %d minutes", minutes);
		Config.M.earlyWarningMessage.broadcastMessage(minutes);
		setNextMessageAlarm(plugin, whenRestart);
	}

	void middleWarningMessage(EithonPlugin plugin, LocalDateTime whenRestart, long seconds) {
		verbose("middleWarningMessage", "%d seconds", seconds);
		Config.M.middleWarningMessage.broadcastMessage(seconds);
		setNextMessageAlarm(plugin, whenRestart);
	}

	void finalWarningMessage(EithonPlugin plugin, LocalDateTime whenRestart, long seconds) {
		verbose("finalWarningMessage", "%d seconds", seconds);
		Config.M.finalWarningMessage.broadcastMessage(seconds);
		setNextMessageAlarm(plugin, whenRestart);
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "EventListener.%s: %s", method, message);
	}
}