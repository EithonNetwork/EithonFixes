package net.eithon.plugin.fixes.logic;

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
		ConfigurableMessage message = Config.M.joinMessage.getMessage(playerName, highestGroup);
		if (message == null) return;
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
