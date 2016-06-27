package net.eithon.plugin.fixes;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.bungee.logic.joinleave.EithonBungeeJoinEvent;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EventListenerEithonBungee implements Listener {
	private Controller _controller;
	private EithonPlugin _eithonPlugin;
	
	public EventListenerEithonBungee(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
	}

	// Reward players that are on the server when another player joins Eithon for the first time this day
	@EventHandler
	public void onEithonBungeeJoinEvent(EithonBungeeJoinEvent event) {
		this._eithonPlugin.dbgMinor("EithonBungeeJoinEvent: %s", event.toString());
		if (!event.getIsFirstJoinToday()) {
			this._eithonPlugin.dbgMinor("EithonBungeeJoinEvent: Not first join today");
			return;
		}
		this._eithonPlugin.dbgMajor("First join today: %s", event.toString());
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(event.getPlayerId());
		if (player == null) {
			this._eithonPlugin.logWarn("Did not expect player to be null for %s", event.toString());
			return;
		}
		this._controller.rewardPlayersOnFirstJoinToday(event.getPlayerName());
	}
}
