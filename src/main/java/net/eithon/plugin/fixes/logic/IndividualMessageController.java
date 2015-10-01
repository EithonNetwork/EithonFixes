package net.eithon.plugin.fixes.logic;

import java.util.HashMap;

import org.bukkit.entity.Player;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.plugin.fixes.Config;

public class IndividualMessageController {
	private EithonPlugin _eithonPlugin;

	public IndividualMessageController(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
	}
	
	public void playerJoined(Player player) {
		ConfigurableMessage message = Config.M.joinMessage.getMessage(player.getName(), null);
		if (message == null) return;
		message.sendMessage(player);
	}
}
