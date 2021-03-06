package net.eithon.plugin.fixes.logic;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.fixes.Config;
import net.eithon.plugin.fixes.logic.cooldown.CoolDownCommandController;
import net.eithon.plugin.fixes.logic.cooldown.CoolDownWorldController;
import net.eithon.plugin.fixes.logic.regioncommand.RegionCommandController;
import net.eithon.plugin.fixes.logic.spawnpoint.SpawnPointController;
import net.eithon.plugin.stats.EithonStatsApi;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.earth2me.essentials.api.Economy;

public class Controller {
	private KillerMoneyController _killerMoneyController;
	private BuyController _buyController;
	private RegionCommandController _regionCommandController;
	private SpawnPointController _spawnPointController;
	private CoolDownCommandController _coolDownCommandController;
	private CoolDownWorldController _coolDownWorldController;
	UUID _restartAlarmIdentity;
	private LocalDateTime _whenRestart;
	private EithonPlugin _eithonPlugin;

	public Controller(EithonPlugin plugin) {
		this._eithonPlugin = plugin;
		this._killerMoneyController = new KillerMoneyController(plugin);
		this._buyController = new BuyController(plugin);
		this._regionCommandController = new RegionCommandController(plugin);
		this._spawnPointController = new SpawnPointController(plugin);
		this._coolDownCommandController = new CoolDownCommandController(plugin);
		this._coolDownWorldController = new CoolDownWorldController(plugin);
		Config.V.commandScheduler.start();
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

	public void displayBalance(CommandSender sender, OfflinePlayer player) {
		this._buyController.displayBalance(sender, player);
	}

	public long secondsLeftOfCommandCoolDown(Player player, String command) {
		return this._coolDownCommandController.secondsLeftOfCoolDown(player, command);
	}

	public long secondsLeftOfWorldCoolDown(Player player, String world) {
		return this._coolDownWorldController.secondsLeftOfCoolDown(player, world);
	}

	public double getReductedMoney(Player player, double money) {
		return this._killerMoneyController.getReductedMoney(player, money);
	}

	public void rcAdd(Player player, String name, String command) {
		this._regionCommandController.updateOrCreateRegionCommand(player, name, command, true, true);
	}

	public void rcEdit(Player player, String name, String command) {
		this._regionCommandController.editRegionCommand(player, name, command, true, true);
	}

	public void rcSet(Player player, String name, boolean onEnter, boolean triggerOnEnterFromOtherWorld) {
		this._regionCommandController.editRegionCommand(player, name, onEnter, triggerOnEnterFromOtherWorld);
	}

	public void rcDelete(Player player, String name) {
		this._regionCommandController.deleteRegionCommand(player, name);
	}

	public void rcGoto(Player player, String name) {
		this._regionCommandController.gotoRegionCommand(player, name);
	}

	public void rcList(CommandSender sender) {
		this._regionCommandController.listRegionCommands(sender);
	}

	public List<String> getAllRegionCommands() {
		return Arrays.asList(this._regionCommandController.getAllRegionCommands());
	}

	public void spAdd(Player player, String name, long distance) {
		this._spawnPointController.updateOrCreateSpawnPoint(player, name, distance);
	}

	public void spEdit(Player player, String name, long distance) {
		this._spawnPointController.editSpawnPoint(player, name, distance);
	}

	public void spDelete(CommandSender sender, String name) {
		this._spawnPointController.deleteSpawnPoint(sender, name);
	}

	public void spGoto(Player player, String name) {
		this._spawnPointController.gotoSpawnPoint(player, name);
	}

	public void spList(CommandSender sender) {
		this._spawnPointController.listSpawnPoints(sender);
	}

	public boolean maybeTeleportToSpawnPoint(Player player) {
		return this._spawnPointController.maybeTeleportToSpawnPoint(player);
	}

	public List<String> getAllSpawnPointNames() {
		return Arrays.asList(this._spawnPointController.getAllSpawnPointNames());
	}

	public LocalDateTime initiateRestart(long seconds) {
		UUID alarmId = initiateRestartInternally(seconds+1);
		if (alarmId == null) return null;
		setNextMessageAlarm(alarmId);
		return this._whenRestart;
	}

	private UUID initiateRestartInternally(long seconds) {
		LocalDateTime whenRestart = LocalDateTime.now().plusSeconds(seconds);
		if (this._restartAlarmIdentity != null) {
			AlarmTrigger.get().removeAlarm(this._restartAlarmIdentity);
			this._restartAlarmIdentity = null;
		}

		this._restartAlarmIdentity = AlarmTrigger.get().setAlarm("restart", whenRestart, new Runnable() {
			public void run() {
				Server server = Bukkit.getServer();
				server.dispatchCommand(server.getConsoleSender(), "restart");			
			}
		});
		this._whenRestart = whenRestart;
		return this._restartAlarmIdentity;
	}

	public boolean cancelRestart() {
		if (this._restartAlarmIdentity == null) return true;
		boolean success = AlarmTrigger.get().removeAlarm(this._restartAlarmIdentity);
		this._restartAlarmIdentity = null;
		return success;
	}

	private void setNextMessageAlarm(UUID alarmId) {
		if (!alarmId.equals(this._restartAlarmIdentity)) return;
		EithonPlugin plugin = this._eithonPlugin;
		Controller thisObject = this;
		verbose("setNextMessageAlarm", "Enter, whenRestart = %s", this._whenRestart);
		long secondsLeft = this._whenRestart.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		long minutesLeft = secondsLeft/60;
		verbose("setNextMessageAlarm", "Left = %d seconds (%d minutes)", secondsLeft, minutesLeft);
		for (long seconds : Config.V.showEarlyWarningMessageTimeSpanList) {
			long secondsRemainingToMessage = secondsLeft- seconds;
			if (secondsRemainingToMessage > 0) {
				verbose("setNextMessageAlarm", "Found early = %s, in %d seconds",
						TimeMisc.secondsToString(seconds), secondsRemainingToMessage);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (thisObject._restartAlarmIdentity == null) return;
						earlyWarningMessage(alarmId, seconds);
					}
				}, TimeMisc.secondsToTicks(secondsRemainingToMessage));
				return;
			}
		}
		for (long seconds : Config.V.showMiddleWarningMessageTimeSpanList) {
			long secondsRemainingToMessage = secondsLeft- seconds;
			if (secondsRemainingToMessage > 0) {
				verbose("setNextMessageAlarm", "Found middle = %s, in %d seconds", 
						TimeMisc.secondsToString(seconds), secondsRemainingToMessage);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (thisObject._restartAlarmIdentity == null) return;
						middleWarningMessage(alarmId, seconds);
					}
				}, TimeMisc.secondsToTicks(secondsRemainingToMessage));
				return;
			}
		}
		for (long seconds : Config.V.showFinalWarningMessageTimeSpanList) {
			long secondsRemainingToMessage = secondsLeft- seconds;
			if (secondsRemainingToMessage > 0) {
				verbose("setNextMessageAlarm", "Found final = %s, in %d seconds", 
						TimeMisc.secondsToString(seconds), secondsRemainingToMessage);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (thisObject._restartAlarmIdentity == null) return;
						finalWarningMessage(alarmId, seconds);
					}
				}, TimeMisc.secondsToTicks(secondsRemainingToMessage));
				return;
			}
		}
		verbose("setNextMessageAlarm", "No message found so scheduling \"Restarting server\" message in %d seconds", secondsLeft);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (thisObject._restartAlarmIdentity == null) return;
				Config.M.restartingServer.broadcastToThisServer();
			}
		}, TimeMisc.secondsToTicks(secondsLeft));
	}

	void earlyWarningMessage(UUID alarmId, long seconds) {
		if (!alarmId.equals(this._restartAlarmIdentity)) return;
		verbose("earlyWarningMessage", " %d seconds", seconds);
		Config.M.earlyWarningMessage.broadcastToThisServer(seconds/60);
		setNextMessageAlarm(alarmId);
	}

	void middleWarningMessage(UUID alarmId, long seconds) {
		if (!alarmId.equals(this._restartAlarmIdentity)) return;
		verbose("middleWarningMessage", "%d seconds", seconds);
		Config.M.middleWarningMessage.broadcastToThisServer(seconds);
		setNextMessageAlarm(alarmId);
	}

	void finalWarningMessage(UUID alarmId, long seconds) {
		if (!alarmId.equals(this._restartAlarmIdentity)) return;
		verbose("finalWarningMessage", "%d seconds", seconds);
		Config.M.finalWarningMessage.broadcastToThisServer(seconds);
		setNextMessageAlarm(alarmId);
	}

	public boolean isWorldWhereFlyIsAllowed(World world) {
		if (world == null) return false;
		return CoreMisc.isStringInCollectionIgnoreCase(world.getName(), Config.V.flyWorlds);
	}

	public boolean isInWorldWhereFlyIsAllowed(Player player) {
		if (player == null) return false;
		EithonPlayer eithonPlayer = new EithonPlayer(player);
		return eithonPlayer.isInAcceptableWorld(Config.V.flyWorlds);
	}

	public boolean setPluginDebugLevel(CommandSender sender, String pluginName, int debugLevel) {
		EithonPlugin eithonPlugin = EithonPlugin.getByName(pluginName);
		if (eithonPlugin == null) {
			sender.sendMessage(String.format("Could not find EithonPlugin by name %s", pluginName));
			return false;
		}
		eithonPlugin.setDebugLevel(debugLevel);
		return true;
	}

	public void playerMovedOneBlock(final Player player, final Block fromBlock,
			final Block toBlock) {
		final RegionCommandController regionCommandController = this._regionCommandController;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.runTaskAsynchronously(this._eithonPlugin, new Runnable() {
			public void run() {
				regionCommandController.playerMovedOneBlockAsync(player, fromBlock, toBlock);
			}
		});
	}

	public boolean removeCooldowns(CommandSender sender, Player player) {
		this._coolDownWorldController.removePlayer(player);
		this._coolDownCommandController.removePlayer(player);
		return true;
	}

	public void rewardPlayersOnFirstJoinToday(String playerName) {
		for (Player rewardedPlayer : Bukkit.getServer().getOnlinePlayers()) {
			try {
				verbose("rewardPlayersOnFirstJoinToday", "Will reward %s", rewardedPlayer.getName());
				double reward = 0.0;
				if (EithonStatsApi.isActive(rewardedPlayer)) reward = Config.V.firstJoinTodayRewardWhenOnline;
				else reward = Config.V.firstJoinTodayRewardWhenAfk;
				Economy.add(rewardedPlayer.getName(), new BigDecimal(reward));
				Config.M.firstJoinTodayReward.sendMessage(rewardedPlayer, reward, playerName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("Controller", method, format, args);
	}

}