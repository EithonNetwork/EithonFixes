package net.eithon.plugin.fixes;

import java.util.ArrayList;
import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;
import net.eithon.library.plugin.PermissionBasedMultiplier;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.fixes.logic.CoolDownInfo;

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
		public static List<CoolDownInfo> coolDownInfos;
		public static List<String> buyWorlds;
		public static List<String> flyWorlds;
		public static List<String> penaltyOnDeathWorlds;
		public static List<String> chatChannelsToLeave;
		public static double costOfDeath;
		public static PermissionBasedMultiplier killerMoneyMultiplier;

		static void load(Configuration config, EithonPlugin plugin) {
			rewardCoolDownInSeconds = config.getSeconds("RewardCoolDownTimeSpan", 10);
			rewardReduction = config.getDouble("RewardReduction", 0.85);
			showEarlyWarningMessageTimeSpanList = config.getSecondsList("ShowEarlyWarningMessageTimeSpanList");
			showMiddleWarningMessageTimeSpanList = config.getSecondsList("ShowMiddleWarningMessageTimeSpanList");
			showFinalWarningMessageTimeSpanList = config.getSecondsList("ShowFinalWarningMessageTimeSpanList");
			ArrayList<String> coolDownCommands = new ArrayList<String>(config.getStringList("CoolDownCommands"));
			ArrayList<String> timeSpansAsStrings = new ArrayList<String>(config.getStringList("CoolDownTimeSpan"));
			if (coolDownCommands.size() != timeSpansAsStrings.size()) {
				plugin.getEithonLogger().error("%d CoolDownCommands, but %d CoolDownTimeInSeconds. Should be the same number.",
						coolDownCommands.size(), timeSpansAsStrings.size());
			}
			ArrayList<String> incidentsAsStrings = new ArrayList<String>(config.getStringList("CoolDownAllowedIncidents"));
			if (coolDownCommands.size() != incidentsAsStrings.size()) {
				plugin.getEithonLogger().error("%d CoolDownCommands, but %d CoolDownAllowedIncidents. Should be the same number.",
						coolDownCommands.size(), incidentsAsStrings.size());
			}
			coolDownInfos = new ArrayList<CoolDownInfo>();
			for (int i = 0; i < coolDownCommands.size(); i++) {
				String command = coolDownCommands.get(i);
				String timeSpansAsString = timeSpansAsStrings.get(i);
				long time = TimeMisc.stringToSeconds(timeSpansAsString);
				String incidentsAsString = incidentsAsStrings.get(i);
				int incidents = Integer.parseInt(incidentsAsString);
				coolDownInfos.add(new CoolDownInfo(command, time, incidents));
			}
			buyWorlds = config.getStringList("BuyWorlds");
			flyWorlds = config.getStringList("FlyWorlds");
			penaltyOnDeathWorlds = config.getStringList("PenaltyOnDeathWorlds");
			chatChannelsToLeave = config.getStringList("ChatChannelsToLeave");
			costOfDeath = config.getDouble("CostOfDeath", 30.0);
			killerMoneyMultiplier = PermissionBasedMultiplier.getFromConfig(config, "multipliers.donationboard.mobKill");
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
		public static ConfigurableMessage waitForCoolDown;
		public static ConfigurableMessage joinedServerFirstTime;
		public static ConfigurableMessage pleaseWelcomeNewPlayer;
		public static ConfigurableMessage earlyWarningMessage;
		public static ConfigurableMessage middleWarningMessage;
		public static ConfigurableMessage finalWarningMessage;
		public static ConfigurableMessage restartingServer;

		static void load(Configuration config) {
			penaltyOnDeath = config.getConfigurableMessage("messages.PenaltyOnDeath", 1,
					"Your death has resulted in a penalty of %.2f.");
			youNeedMoreMoney = config.getConfigurableMessage("messages.YouNeedMoreMoney", 4,
					"You need %.2f to buy %d %s. You have %.2f.");
			successfulPurchase = config.getConfigurableMessage("messages.SuccessfulPurchase", 2,
					"You successfully purchased %d item(s) of %s.");
			inventoryFull = config.getConfigurableMessage("messages.InventoryFull", 0,
					"Your inventory is full.");
			currentBalance = config.getConfigurableMessage("messages.CurrentBalance", 1,
					"Your balance is %.2f E-Coins.");
			joinedChat = config.getConfigurableMessage("messages.JoinedChat", 1,
					"You have joined chat channel %s.");
			waitForCoolDown = config.getConfigurableMessage("messages.WaitForCoolDown", 1,
					"In cool down. Remaining time: %s.");
			joinedServerFirstTime = config.getConfigurableMessage("messages.JoinedServerFirstTime", 1,
					"%s joined for the first time!");
			pleaseWelcomeNewPlayer = config.getConfigurableMessage("messages.PleaseWelcomeNewPlayer", 1,
					"Welcome %s to the server!");
			earlyWarningMessage = config.getConfigurableMessage("messages.EarlyWarningMessage", 1,
					"The server will be restarted in %d minutes.");
			middleWarningMessage = config.getConfigurableMessage("messages.MiddleWarningMessage", 1,
					"[subtitle/]Server restart in %d seconds.");
			finalWarningMessage = config.getConfigurableMessage("messages.FinalWarningMessage", 1,
					"[title/]%d");
			restartingServer = config.getConfigurableMessage("messages.RestartingServer", 0,
					"[title/]Server restarting");
		}		
	}

}
