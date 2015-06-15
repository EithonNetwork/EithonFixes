package net.eithon.plugin.fixes;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class EventListener implements Listener {

	private Controller _controller;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
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
		if (this._controller.commandShouldBeCancelled(event.getPlayer(), event.getMessage())) {
			event.setCancelled(true);
			Config.M.waitForCoolDown.sendMessage(event.getPlayer());
		}
	}
}
