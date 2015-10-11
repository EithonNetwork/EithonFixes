package net.eithon.plugin.fixes;

import java.math.BigDecimal;

import net.diecode.KillerMoney.CustomEvents.KillerMoneyMoneyRewardEvent;
import net.eithon.library.bungee.EithonBungeeJoinEvent;
import net.eithon.library.bungee.EithonBungeeQuitEvent;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.fixes.logic.Controller;
import net.eithon.plugin.stats.logic.ConsecutiveDaysEvent;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

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

	// Inform everyone if we have a new player on this server
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		maybeTeleportToSpawnPoint(player);
		maybeBroadcast(event, player);
		String joinMessage = this._controller.getJoinMessage(player);
		if (joinMessage != null) event.setJoinMessage(joinMessage);
	}

	// Inform everyone that a player joined on another server
	@EventHandler
	public void onEithonBungeeJoinEvent(EithonBungeeJoinEvent event) {
		verbose("onEithonBungeeJoinEvent", "Enter");
		EithonPlayer player = event.getPlayer();
		if (player == null) return;
		this._controller.broadcastPlayerJoined(event.getServerName(), player, event.getMainGroup());
		verbose("onEithonBungeeJoinEvent", "Leave");
	}

	// Inform everyone that a player left this server
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		String quitMessage = this._controller.getQuitMessage(player);
		if (quitMessage != null) event.setQuitMessage(quitMessage);
	}

	// Inform everyone that a player left another server
	@EventHandler
	public void onEithonBungeeQuitEvent(EithonBungeeQuitEvent event) {
		verbose("onEithonBungeeQuitEvent", "Enter");
		EithonPlayer player = event.getPlayer();
		if (player == null) return;
		this._controller.broadcastPlayerQuitted(event.getServerName(), player, event.getMainGroup());
		verbose("onEithonBungeeQuitEvent", "Leave");
	}

	private boolean maybeTeleportToSpawnPoint(Player player) {
		return this._controller.maybeTeleportToSpawnPoint(player);
	}

	private boolean maybeBroadcast(PlayerJoinEvent event, Player player) {
		verbose("maybeBroadcast", "Enter Player %s", player.getName());
		if (player.hasPlayedBefore()) {
			verbose("maybeBroadcast", "%s is not a new player.", player.getName());
			verbose("maybeBroadcast", "Leave false");
			return false;
		}
		verbose("maybeBroadcast", "%s is a new player.", player.getName());
		verbose("maybeBroadcast", "Broadcast");
		event.setJoinMessage(Config.M.joinedServerFirstTime.getMessageWithColorCoding(player.getName()));
		Config.M.pleaseWelcomeNewPlayer.broadcastMessage(player.getName());
		verbose("maybeBroadcast", "Leave true");
		return true;
	}

	// CoolDown for commands
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Intercepted command \"%s\".", event.getMessage());
		long secondsLeftOfCoolDown = this._controller.secondsLeftOfCommandCoolDown(event.getPlayer(), event.getMessage());
		if (secondsLeftOfCoolDown > 0) {
			this._eithonLogger.debug(DebugPrintLevel.MAJOR, "Command \"%s\" will be cancelled.", event.getMessage());
			event.setCancelled(true);
			Config.M.waitForCommandCoolDown.sendMessage(event.getPlayer(), TimeMisc.secondsToString(secondsLeftOfCoolDown));
		}
	}

	// CoolDown for worlds
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerTeleportEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		String fromWorld = safeGetWorldName(event.getFrom());
		String toWorld = safeGetWorldName(event.getTo());
		if ((fromWorld == null) || (toWorld == null)) return;
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s started teleport from %s to %s", 
				player.getName(), fromWorld, toWorld);
		long secondsLeftOfCoolDown = this._controller.secondsLeftOfWorldCoolDown(player, toWorld);
		if (secondsLeftOfCoolDown > 0) {
			this._eithonLogger.debug(DebugPrintLevel.MAJOR, "Teleport for player %s will be cancelled.", player.getName());
			event.setCancelled(true);
			Config.M.waitForWorldCoolDown.sendMessage(player, TimeMisc.secondsToString(secondsLeftOfCoolDown));
			return;
		}
		this._controller.secondsLeftOfWorldCoolDown(player, toWorld);
	}

	private String safeGetWorldName(Location location) {
		if (location == null) return null;
		if (location.getWorld() == null) return null;
		return location.getWorld().getName();
	}

	// Reduce money reward if killing in fast succession
	@EventHandler
	public void onMoneyRewardEvent(KillerMoneyMoneyRewardEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		double money = this._controller.getReductedMoney(player, event.getMoney());
		double factor = Config.V.killerMoneyMultiplier.getMultiplier(player);
		verbose("getReductedMoney", "Money before permission based multiplier: %.4f", money);
		money = money * factor;
		verbose("getReductedMoney", "Money after permission based multiplier: %.4f", money);
		money = Math.round(money*4)/4.0;
		verbose("getReductedMoney", "Money after round off: %.2f", money);

		event.setMoney(money);
	}
	
	// Players should be encouraged to login on consecutive days
	@EventHandler
	public void onConsecutiveDaysEvent(ConsecutiveDaysEvent event) {
		Player player = event.getPlayer();
		long consecutiveDays = event.getConsecutiveDays();
		double amount = Config.V.consecutiveDaysBaseAmount + (consecutiveDays - 1) * Config.V.consecutiveDaysMultiplyAmount;
		try {
			Economy.add(player.getName(), new BigDecimal(amount));
			Config.M.consecutiveDaysReward.sendMessage(player, amount, consecutiveDays);
		} catch (NoLoanPermittedException | ArithmeticException
				| UserDoesNotExistException e) {
			e.printStackTrace();
		}
	}
	
	// Only allow fly in certain worlds
	@EventHandler
	public void onPlayerToggleFlightEvent(PlayerToggleFlightEvent event) {
		verbose("onPlayerToggleFlightEvent", "Enter");
		if (event.isCancelled()) {
			verbose("onPlayerToggleFlightEvent", "Event has already been cancelled. Return.");
			return;
		}
		if (!event.isFlying()) {
			verbose("onPlayerToggleFlightEvent", "Not flying, rather landing. Return.");
			return;
		}
		
		Player player = event.getPlayer();
		if (this._controller.isInWorldWhereFlyIsAllowed(player)) {
			verbose("onPlayerToggleFlightEvent", "Player %s is in world where flying is allowed. Return.", player.getName());
			return;
		}
		
		// Allow players with permission freebuild.canfly to fly
		if (player.hasPermission("eithonfixes.canfly")) {
			verbose("onPlayerToggleFlightEvent", "Player %s has eithonfixes.canfly permission. Return.", player.getName());
			return;
		}
		
		verbose("onPlayerToggleFlightEvent", "The player %s is not allowed to fly in world %s. Cancels the event and return."
				, player.getName(), player.getWorld().getName());
		player.sendMessage("You are currently not allowed to fly.");
		event.setCancelled(true);
		Config.C.stopFly.executeAs(event.getPlayer());
	}
	
	// Only allow fly in certain worlds
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		verbose("onPlayerTeleportEvent", "Enter");
		if (event.isCancelled()) {
			verbose("onPlayerTeleportEvent", "Event has already been cancelled. Return.");
			return;
		}
		
		Player player = event.getPlayer();
		
		if (!player.isFlying()) {
			verbose("onPlayerTeleportEvent", "Not flying. Return.");
			return;
		}
		
		if (this._controller.isWorldWhereFlyIsAllowed(event.getTo().getWorld())) {
			verbose("onPlayerTeleportEvent", "Player %s is in world where flying is allowed. Return.", player.getName());
			return;
		}
		
		// Allow players with permission eithonfixes.canfly to fly
		if (player.hasPermission("eithonfixes.canfly")) {
			verbose("onPlayerTeleportEvent", "Player %s has eithonfixes.canfly permission. Return.", player.getName());
			return;
		}
		
		verbose("onPlayerTeleportEvent", "The player %s is not allowed to fly in world %s. Stop fly."
				, player.getName(), player.getWorld().getName());
		player.sendMessage(String.format("You are currently not allowed to fly in world %s.", event.getTo().getWorld()));
		Config.C.stopFly.executeAs(event.getPlayer());
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "EventListener.%s: %s", method, message);
	}
}
