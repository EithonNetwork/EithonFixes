package net.eithon.plugin.fixes;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.bungee.logic.joinleave.EithonBungeeJoinEvent;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;

public class EventListenerEithonBungee implements Listener {
	private Controller _controller;
	private EithonPlugin _eithonPlugin;
	
	public EventListenerEithonBungee(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
		eithonPlugin.getEithonLogger();
	}

	// Reward players that are on the server when another player joins Eithon for the first time this day
	public void onEithonBungeeJoinEvent(EithonBungeeJoinEvent event) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR,
				"EithonFixes.onEithonBungeeJoinEvent: %s",
				event.toString());
		if (!event.getIsFirstJoinToday()) return;
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(event.getPlayerId());
		if (player == null) return;
		this._controller.rewardPlayersOnFirstJoinToday(event.getPlayerName());
	}
}
