package net.eithon.plugin.fixes;

import java.util.ArrayList;
import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;
import net.eithon.library.plugin.PermissionBasedMultiplier;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.fixes.logic.CommandScheduler;
import net.eithon.plugin.fixes.logic.cooldown.CoolDownInfo;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config, plugin);
		C.load(config);
		M.load(config);

	}
	public static class V {
		public static List<Long> showEarlyWarningMessageTimeSpanList;
		public static List<Long> showMiddleWarningMessageTimeSpanList;
		public static List<Long> showFinalWarningMessageTimeSpanList;
		public static long rewardCoolDownInSeconds;
		public static double rewardReduction;
		public static List<CoolDownInfo> coolDownCommandInfos;
		public static List<CoolDownInfo> coolDownWorldInfos;
		public static List<String> buyWorlds;
		public static List<String> flyWorlds;
		public static List<String> penaltyOnDeathWorlds;
		public static List<String> chatChannelsToLeave;
		public static double costOfDeath;
		public static PermissionBasedMultiplier killerMoneyMultiplier;
		public static double consecutiveDaysBaseAmount;
		public static double consecutiveDaysMultiplyAmount;
		public static double consecutiveDaysMaxAmount;
		public static CommandScheduler commandScheduler;
		public static double freezeRestoreWalkSpeed;
		public static double freezeRestoreFlySpeed;
		public static double firstJoinTodayRewardWhenAfk;
		public static double firstJoinTodayRewardWhenOnline;
		
		static void load(Configuration config, EithonPlugin plugin) {
			rewardCoolDownInSeconds = config.getSeconds("RewardCoolDownTimeSpan", 10);
			rewardReduction = config.getDouble("RewardReduction", 0.85);
			showEarlyWarningMessageTimeSpanList = config.getSecondsList("ShowEarlyWarningMessageTimeSpanList");
			showMiddleWarningMessageTimeSpanList = config.getSecondsList("ShowMiddleWarningMessageTimeSpanList");
			showFinalWarningMessageTimeSpanList = config.getSecondsList("ShowFinalWarningMessageTimeSpanList");
			coolDownCommandInfos = loadCoolDownCommandsConfig(config, plugin);
			coolDownWorldInfos = loadCoolDownWorldsConfig(config, plugin);
			buyWorlds = config.getStringList("BuyWorlds");
			flyWorlds = config.getStringList("FlyWorlds");
			penaltyOnDeathWorlds = config.getStringList("PenaltyOnDeathWorlds");
			chatChannelsToLeave = config.getStringList("ChatChannelsToLeave");
			costOfDeath = config.getDouble("CostOfDeath", 30.0);
			killerMoneyMultiplier = PermissionBasedMultiplier.getFromConfig(config, "multipliers.donationboard.mobKill");
			consecutiveDaysBaseAmount = config.getDouble("ConsecutiveDaysBaseAmount", 50.0);
			consecutiveDaysMultiplyAmount = config.getDouble("ConsecutiveDaysMultiplyAmount", 25.0);
			consecutiveDaysMaxAmount = config.getDouble("ConsecutiveDaysMaxAmount", 700.0);
			commandScheduler = CommandScheduler.getFromConfig(config, "schedule");
			firstJoinTodayRewardWhenAfk = config.getDouble("FirstJoinTodayRewardWhenAfk", 5.0);
			firstJoinTodayRewardWhenOnline = config.getDouble("FirstJoinTodayRewardWhenOnline", 20.0);
		}

		private static List<CoolDownInfo> loadCoolDownCommandsConfig(Configuration config,
				EithonPlugin plugin) {
			List<CoolDownInfo> coolDownInfos = new ArrayList<CoolDownInfo>();
			ArrayList<String> coolDownCommands = new ArrayList<String>(config.getStringList("CoolDownCommands"));
			ArrayList<String> timeSpansAsStrings = new ArrayList<String>(config.getStringList("CoolDownCommandTimeSpans"));
			if (coolDownCommands.size() != timeSpansAsStrings.size()) {
				plugin.logError("%d CoolDownCommands, but %d CoolDownCommandTimeSpans. Should be the same number.",
						coolDownCommands.size(), timeSpansAsStrings.size());
				return coolDownInfos;
			}
			ArrayList<String> incidentsAsStrings = new ArrayList<String>(config.getStringList("CoolDownCommandAllowedIncidents"));
			if (coolDownCommands.size() != incidentsAsStrings.size()) {
				plugin.logError("%d CoolDownCommands, but %d CoolDownCommandAllowedIncidents. Should be the same number.",
						coolDownCommands.size(), incidentsAsStrings.size());
				return coolDownInfos;
			}
			for (int i = 0; i < coolDownCommands.size(); i++) {
				String command = coolDownCommands.get(i);
				String timeSpansAsString = timeSpansAsStrings.get(i);
				long time = TimeMisc.stringToSeconds(timeSpansAsString);
				String incidentsAsString = incidentsAsStrings.get(i);
				int incidents = Integer.parseInt(incidentsAsString);
				coolDownInfos.add(new CoolDownInfo(command, time, incidents));
			}
			
			return coolDownInfos;
		}

		private static List<CoolDownInfo> loadCoolDownWorldsConfig(Configuration config, EithonPlugin plugin) {
			List<CoolDownInfo> coolDownInfos = new ArrayList<CoolDownInfo>();
			ArrayList<String> coolDownWorlds = new ArrayList<String>(config.getStringList("CoolDownWorlds"));
			ArrayList<String> timeSpansAsStrings = new ArrayList<String>(config.getStringList("CoolDownWorldTimeSpans"));
			if (coolDownWorlds.size() != timeSpansAsStrings.size()) {
				plugin.logError("%d CoolDownWorlds, but %d CoolDownWorldTimeSpans. Should be the same number.",
						coolDownWorlds.size(), timeSpansAsStrings.size());
				return coolDownInfos;
			}
			for (int i = 0; i < coolDownWorlds.size(); i++) {
				String command = coolDownWorlds.get(i);
				String timeSpansAsString = timeSpansAsStrings.get(i);
				long time = TimeMisc.stringToSeconds(timeSpansAsString);
				coolDownInfos.add(new CoolDownInfo(command, time, 1));
			}
			
			return coolDownInfos;
		}
	}
	public static class C {
		public static ConfigurableCommand give;
		public static ConfigurableCommand take;
		public static ConfigurableCommand joinChat;
		public static ConfigurableCommand leaveChat;
		public static ConfigurableCommand stopFly;

		static void load(Configuration config) {
			give = config.getConfigurableCommand("commands.GiveCommand", 3,
					"give %s %s %d");
			take = config.getConfigurableCommand("commands.TakeCommand", 2,
					"eco take %s %f");
			joinChat = config.getConfigurableCommand("commands.JoinChannel", 1,
					"ch enter %s");
			leaveChat = config.getConfigurableCommand("commands.LeaveChannel", 1,
					"ch leave %s");
			stopFly = config.getConfigurableCommand("commands.StopFly", 0,
					"fly");
		}

	}
	public static class M {
		public static ConfigurableMessage youNeedMoreMoney;
		public static ConfigurableMessage successfulPurchase;
		public static ConfigurableMessage inventoryFull;
		public static ConfigurableMessage currentBalance;
		public static ConfigurableMessage penaltyOnDeath;
		public static ConfigurableMessage joinedChat;
		public static ConfigurableMessage waitForCommandCoolDown;
		public static ConfigurableMessage waitForWorldCoolDown;
		public static ConfigurableMessage earlyWarningMessage;
		public static ConfigurableMessage middleWarningMessage;
		public static ConfigurableMessage finalWarningMessage;
		public static ConfigurableMessage restartingServer;
		public static ConfigurableMessage consecutiveDaysReward;
		public static ConfigurableMessage firstJoinTodayReward;

		static void load(Configuration config) {
			penaltyOnDeath = config.getConfigurableMessage("messages.PenaltyOnDeath", 1,
					"Your death has resulted in a penalty of %.2f.");
			youNeedMoreMoney = config.getConfigurableMessage("messages.YouNeedMoreMoney", 4,
					"You need %.2f to buy %d %s. You have %.2f.");
			successfulPurchase = config.getConfigurableMessage("messages.SuccessfulPurchase", 2,
					"You successfully purchased %d item(s) of %s.");
			inventoryFull = config.getConfigurableMessage("messages.InventoryFull", 0,
					"Your inventory is full.");
			currentBalance = config.getConfigurableMessage("messages.CurrentBalance", 2,
					"The balance for %s is %.2f E-Coins.");
			joinedChat = config.getConfigurableMessage("messages.JoinedChat", 1,
					"You have joined chat channel %s.");
			waitForCommandCoolDown = config.getConfigurableMessage("messages.WaitForCoolDown", 1,
					"In cool down. Remaining time: %s.");
			waitForWorldCoolDown = config.getConfigurableMessage("messages.WaitForWorldDown", 1,
					"In cool down. Remaining time: %s.");
			earlyWarningMessage = config.getConfigurableMessage("messages.EarlyWarningMessage", 1,
					"The server will be restarted in %d minutes.");
			middleWarningMessage = config.getConfigurableMessage("messages.MiddleWarningMessage", 1,
					"[subtitle/]Server restart in %d seconds.");
			finalWarningMessage = config.getConfigurableMessage("messages.FinalWarningMessage", 1,
					"[title/]%d");
			restartingServer = config.getConfigurableMessage("messages.RestartingServer", 0,
					"[title/]Server restarting");
			consecutiveDaysReward = config.getConfigurableMessage("messages.ConsecutiveDaysReward", 2,
					"You were awarded %.2f E-Coins for using the server for %d consecutive days.");
			firstJoinTodayReward = config.getConfigurableMessage("messages.FirstJoinTodayReward", 2,
					"You were awarded %.2f E-Coins for witnessing %s's first login today!");
		}		
	}

}
