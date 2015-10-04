package net.eithon.plugin.fixes.logic;

import java.util.HashMap;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.ZPermissionsFacade;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.fixes.Config;

import org.bukkit.entity.Player;

public class IndividualMessageController {
	private EithonPlugin _eithonPlugin;

	public IndividualMessageController(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
	}
	
	public void playerJoined(Player player) {
		verbose("playerJoined", "Enter, Player = %s", player.getName());
		String playerName = player.getName();
		String highestGroup = getHighestGroup(player);
		ConfigurableMessage configurableMessage = Config.M.joinMessage.getMessage(playerName, highestGroup);
		if (configurableMessage == null) {
			verbose("playerJoined", "Leave, No configurable message", player.getName());
			return;
		}
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", playerName);
		namedArguments.put("SERVER_NAME", player.getServer().getName());
		String message = configurableMessage.getMessageWithColorCoding(namedArguments);
		if (message == null) {
			verbose("playerJoined", "Leave, No message", player.getName());
			return;
		}
		verbose("playerJoined", "Execute bungee broadcast with message = \"%s\"", message);
		Config.C.bungeeBroadcast.execute(message);
		verbose("playerJoined", "Leave");
	}

	private String getHighestGroup(Player player) {
		verbose("getHighestGroup", "Enter, Player = %s", player.getName());
		String[] currentGroups = ZPermissionsFacade.getPlayerPermissionGroups(player);
		for (String priorityGroup : Config.V.groupPriorities) {
			for (String playerGroup : currentGroups) {
				if (playerGroup.equalsIgnoreCase(priorityGroup)) {
					verbose("getHighestGroup", "Leave, priorityGroup = %s", priorityGroup);
					return priorityGroup;
				}
			}
		}
		verbose("getHighestGroup", "Leave, priorityGroup = null");
		return null;
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "IndividualMessageController.%s: %s", method, message);
	}
}
