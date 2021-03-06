package net.eithon.plugin.fixes.logic;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.CoolDown;
import net.eithon.plugin.fixes.Config;

import org.bukkit.entity.Player;

public class KillerMoneyController {
	private CoolDown _rewardCoolDown;
	private PlayerCollection<Double> _lastRewardFactor;
	private EithonPlugin _eithonPlugin;
	
	KillerMoneyController(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
		this._rewardCoolDown = new CoolDown("KillerMoneyReward", Config.V.rewardCoolDownInSeconds);
		this._lastRewardFactor = new PlayerCollection<Double>();
	}
	
	public double getReductedMoney(Player player, double money) {
		if (this._rewardCoolDown.isInCoolDownPeriod(player)) {
			Double rewardReduction = this._lastRewardFactor.get(player);
			if (rewardReduction == null) rewardReduction = new Double(1.0);
			double rewardFactor = rewardReduction*Config.V.rewardReduction;
			this._lastRewardFactor.put(player, new Double(rewardFactor));
			verbose("getReductedMoney", "Money before reduction: %.4f", money);
			money = rewardFactor*money;
			verbose("getReductedMoney", "Money after reduction: %.4f", money);
		} else {
			this._lastRewardFactor.remove(player);
		}
		this._rewardCoolDown.addIncident(player);
		return money;
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("KillerMoneyController", method, format, args);
	}
}
