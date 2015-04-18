package net.eithon.plugin.fixes;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Events implements Listener {
	private static Events singleton = null;

	private Events() {
	}

	static Events get()
	{
		if (singleton == null) {
			singleton = new Events();
		}
		return singleton;
	}

	void enable(EithonPlugin eithonPlugin){
	}

	void disable() {
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		Player player = (Player) entity;
		Fixes.get().playerDied(player);
	}
}
