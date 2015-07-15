package net.eithon.plugin.fixes.logic;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.time.CoolDown;
import net.eithon.plugin.fixes.Config;

import org.bukkit.entity.Player;

public class KillerMoneyController {
	private CoolDown _rewardCoolDown;
	private PlayerCollection<Double> _lastRewardFactor;
	
	KillerMoneyController()
	{
		this._rewardCoolDown = new CoolDown("KillerMoneyReward", Config.V.rewardCoolDownInSeconds);
		this._lastRewardFactor = new PlayerCollection<Double>();
	}
	
	public double getReductedMoney(Player player, double money) {
		if (this._rewardCoolDown.isInCoolDownPeriod(player)) {
			Double rewardReduction = this._lastRewardFactor.get(player);
			if (rewardReduction == null) rewardReduction = new Double(1.0);
			double rewardFactor = rewardReduction*Config.V.rewardReduction;
			this._lastRewardFactor.put(player, new Double(rewardFactor));
			money = rewardFactor*money;
			money = Math.round(money);
		} else {
			this._lastRewardFactor.remove(player);
		}
		this._rewardCoolDown.addPlayer(player);
		return money;
	}
}
