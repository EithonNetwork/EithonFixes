package net.eithon.plugin.fixes;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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
}
