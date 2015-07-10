package net.eithon.plugin.fixes;

import net.diecode.KillerMoney.CustomEvents.KillerMoneyMoneyRewardEvent;
import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.CoolDown;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class EventListener implements Listener {

	private Controller _controller;
	private Logger _eithonLogger;
	private CoolDown _rewardCoolDown;
	private PlayerCollection<Double> _lastRewardFactor;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonLogger = eithonPlugin.getEithonLogger();
		this._rewardCoolDown = new CoolDown("KillerMoneyReward", Config.V.rewardCoolDownInSeconds);
		this._lastRewardFactor = new PlayerCollection<Double>();
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		Player player = (Player) entity;
		this._controller.playerDied(player);
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Intercepted command \"%s\".", event.getMessage());
		long secondsLeftOfCoolDown = this._controller.secondsLeftOfCoolDown(event.getPlayer(), event.getMessage());
		if (secondsLeftOfCoolDown > 0) {
			this._eithonLogger.debug(DebugPrintLevel.MAJOR, "Command \"%s\" will be cancelled.", event.getMessage());
			event.setCancelled(true);
			Config.M.waitForCoolDown.sendMessage(event.getPlayer(), TimeMisc.secondsToString(secondsLeftOfCoolDown));
		}
	}

	@EventHandler
	public void onMoneyRewardEvent(KillerMoneyMoneyRewardEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		if (this._rewardCoolDown.isInCoolDownPeriod(player)) {
			Double rewardReduction = this._lastRewardFactor.get(player);
			if (rewardReduction == null) rewardReduction = new Double(1.0);
			double rewardFactor = rewardReduction*Config.V.rewardReduction;
			this._lastRewardFactor.put(player, new Double(rewardFactor));
			event.setMoney(rewardFactor*event.getMoney());
		} else {
			this._lastRewardFactor.remove(player);
		}
		this._rewardCoolDown.addPlayer(player);
	}
}
