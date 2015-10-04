package net.eithon.plugin.fixes.logic;

import java.util.HashMap;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.ZPermissionsFacade;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.plugin.fixes.Config;

import org.bukkit.entity.Player;

public class IndividualMessageController {
	private EithonPlugin _eithonPlugin;

	public IndividualMessageController(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
	}
	
	public void playerJoined(Player player) {
		String playerName = player.getName();
		String highestGroup = getHighestGroup(player);
		ConfigurableMessage configurableMessage = Config.M.joinMessage.getMessage(playerName, highestGroup);
		if (configurableMessage == null) return;
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", playerName);
		namedArguments.put("SERVER_NAME", player.getServer().getName());
		String message = configurableMessage.getMessageWithColorCoding(namedArguments);
		Config.C.bungeeBroadcast.execute(message);
	}

	private String getHighestGroup(Player player) {
		String[] currentGroups = ZPermissionsFacade.getPlayerPermissionGroups(player);
		for (String priorityGroup : Config.V.groupPriorities) {
			for (String playerGroup : currentGroups) {
				if (playerGroup.equalsIgnoreCase(priorityGroup)) return priorityGroup;
			}
		}
		return null;
	}
}
