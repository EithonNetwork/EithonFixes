package net.eithon.plugin.fixes;

import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	public static class V {
		public static List<String> penaltyOnDeathWorlds;
		public static double costOfDeath;

		static void load(Configuration config) {
			penaltyOnDeathWorlds = config.getStringList("PenaltyOnDeathWorlds");
			costOfDeath = config.getDouble("CostOfDeath", 30.0);
		}
	}
	public static class C {
		public static ConfigurableCommand _give;
		public static ConfigurableCommand _take;

		static void load(Configuration config) {
			_give = config.getConfigurableCommand("GiveCommand", 3,
					"give %s %s %d");
			_take = config.getConfigurableCommand("TakeCommand", 2,
					"eco take %s %f");
		}

	}
	public static class M {
		public static ConfigurableMessage youNeedMoreMoney;
		public static ConfigurableMessage successfulPurchase;
		public static ConfigurableMessage currentBalance;
		public static ConfigurableMessage penaltyOnDeath;

		static void load(Configuration config) {
			penaltyOnDeath = config.getConfigurableMessage("messages.PenaltyOnDeath", 1,
					"Your death has resulted in a penalty of %.2f.");
			youNeedMoreMoney = config.getConfigurableMessage("messages.YouNeedMoreMoney", 4,
					"You need %.2f to buy %d %s. You have %.2f.");
			successfulPurchase = config.getConfigurableMessage("messages.SuccessfulPurchase", 2,
					"You successfully purchased %d item(s) of %s.");
			currentBalance = config.getConfigurableMessage("messages.CurrentBalance", 1,
					"Your balance is %.2f E-Coins.");
		}		
	}

}
