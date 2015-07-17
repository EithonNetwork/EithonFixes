package net.eithon.plugin.fixes;

import java.util.Date;

import net.diecode.KillerMoney.CustomEvents.KillerMoneyMoneyRewardEvent;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

	private Controller _controller;
	private Logger _eithonLogger;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonLogger = eithonPlugin.getEithonLogger();
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		Player player = (Player) entity;
		this._controller.playerDied(player);
	}

	// Inform everyone that we have a new player on the server
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		verbose("onPlayerJoinEvent", "Enter Player %s", player.getName());
		long timeSinceFirstPlayedSeconds = 0;
		long firstPlayed = player.getFirstPlayed();
		if (firstPlayed > 0) {
			long now = System.currentTimeMillis();
			timeSinceFirstPlayedSeconds = (now-firstPlayed)/1000;
		}
		verbose("onPlayerJoinEvent", "Player %s was first spotted %d seconds ago", player.getName(), timeSinceFirstPlayedSeconds);
		if (timeSinceFirstPlayedSeconds > 10) {
			verbose("onPlayerJoinEvent", "%s is not a new player (%d > 10 s).", player.getName(), timeSinceFirstPlayedSeconds);
			verbose("onPlayerJoinEvent", "Leave");
			return;
		}
		verbose("onPlayerJoinEvent", "%s is a new player (%d <= 10 s).", player.getName(), timeSinceFirstPlayedSeconds);
		verbose("onPlayerJoinEvent", "Broadcast");
		event.setJoinMessage(Config.M.joinedServerFirstTime.getMessage(player.getName()));
		Config.M.pleaseWelcomeNewPlayer.broadcastMessage(player.getName());
		verbose("onPlayerJoinEvent", "Leave");
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
		double money = this._controller.getReductedMoney(event.getPlayer(), event.getMoney());
		event.setMoney(money);
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}
