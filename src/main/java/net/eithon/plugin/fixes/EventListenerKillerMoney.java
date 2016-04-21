package net.eithon.plugin.fixes;

import net.diecode.KillerMoney.CustomEvents.KillerMoneyMoneyRewardEvent;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EventListenerKillerMoney implements Listener {

	private Controller _controller;
	private Logger _eithonLogger;

	public EventListenerKillerMoney(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonLogger = eithonPlugin.getEithonLogger();
	}

	// Reduce money reward if killing in fast succession
	@EventHandler(ignoreCancelled=true)
	public void onMoneyRewardEvent(KillerMoneyMoneyRewardEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		double money = this._controller.getReductedMoney(player, event.getMoney());
		double factor = Config.V.killerMoneyMultiplier.getMultiplier(player);
		verbose("onMoneyRewardEvent", "Money before permission based multiplier: %.4f", money);
		money = money * factor;
		verbose("onMoneyRewardEvent", "Money after permission based multiplier: %.4f", money);
		money = Math.round(money*4)/4.0;
		verbose("onMoneyRewardEvent", "Money after round off: %.2f", money);
		event.setMoney(money);
	}


	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "EventListener.%s: %s", method, message);
	}
}
