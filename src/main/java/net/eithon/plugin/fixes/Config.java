package net.eithon.plugin.fixes;

import java.util.ArrayList;
import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;
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
		public static List<CoolDownInfo> coolDownInfos;
		public static List<String> buyWorlds;
		public static List<String> penaltyOnDeathWorlds;
		public static List<String> chatChannelsToLeave;
		public static double costOfDeath;

		static void load(Configuration config, EithonPlugin plugin) {
			ArrayList<String> coolDownCommands = new ArrayList<String>(config.getStringList("CoolDownCommands"));
			ArrayList<String> strings = new ArrayList<String>(config.getStringList("CoolDownTimeInSeconds"));
			if (coolDownCommands.size() != strings.size()) {
				plugin.getEithonLogger().error("%d CoolDownCommands, but %d CoolDownTimeInSeconds. Should be the same number.",
						coolDownCommands.size(), strings.size());
			}
			coolDownInfos = new ArrayList<CoolDownInfo>();
			for (int i = 0; i < coolDownCommands.size(); i++) {
				String command = coolDownCommands.get(i);
				int time = Integer.parseInt(strings.get(i));
				coolDownInfos.add(new CoolDownInfo(command, time));
			}
			buyWorlds = config.getStringList("BuyWorlds");
			penaltyOnDeathWorlds = config.getStringList("PenaltyOnDeathWorlds");
			chatChannelsToLeave = config.getStringList("ChatChannelsToLeave");
			costOfDeath = config.getDouble("CostOfDeath", 30.0);
		}
	}
	public static class C {
		public static ConfigurableCommand give;
		public static ConfigurableCommand take;
		public static ConfigurableCommand joinChat;
		public static ConfigurableCommand leaveChat;

		static void load(Configuration config) {
			give = config.getConfigurableCommand("GiveCommand", 3,
					"give %s %s %d");
			take = config.getConfigurableCommand("TakeCommand", 2,
					"eco take %s %f");
			joinChat = config.getConfigurableCommand("JoinChannel", 1,
					"/ch enter %s");
			leaveChat = config.getConfigurableCommand("LeaveChannel", 1,
					"/ch leave %s");
		}

	}
	public static class M {
		public static ConfigurableMessage youNeedMoreMoney;
		public static ConfigurableMessage successfulPurchase;
		public static ConfigurableMessage currentBalance;
		public static ConfigurableMessage penaltyOnDeath;
		public static ConfigurableMessage joinedChat;

		static void load(Configuration config) {
			penaltyOnDeath = config.getConfigurableMessage("messages.PenaltyOnDeath", 1,
					"Your death has resulted in a penalty of %.2f.");
			youNeedMoreMoney = config.getConfigurableMessage("messages.YouNeedMoreMoney", 4,
					"You need %.2f to buy %d %s. You have %.2f.");
			successfulPurchase = config.getConfigurableMessage("messages.SuccessfulPurchase", 2,
					"You successfully purchased %d item(s) of %s.");
			currentBalance = config.getConfigurableMessage("messages.CurrentBalance", 1,
					"Your balance is %.2f E-Coins.");
			joinedChat = config.getConfigurableMessage("messages.JoinedChat", 1,
					"You have joined chat channel %s.");
		}		
	}

}
