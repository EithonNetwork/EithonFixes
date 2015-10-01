package net.eithon.plugin.fixes.logic;

import java.util.HashMap;

import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class IndividualConfigurableMessage {

	private Configuration _config;
	private String _configurationPath;
	private ConfigurableMessage _defaultMessage;
	private HashMap<String,ConfigurableMessage> _playerMessages;
	private HashMap<String,ConfigurableMessage> _rankMessages;

	public IndividualConfigurableMessage(Configuration config, String configurationPath) {
		this._config = config;
		this._configurationPath = configurationPath;
		this._defaultMessage = config.getConfigurableMessage(
				configurationPath + ".message", 0,
				"[color=green]+[/color] [color=yellow]%name[/color] [color=gray]has joined the server.[/color]");
		this._playerMessages = new HashMap<String, ConfigurableMessage>();
		this._rankMessages = new HashMap<String, ConfigurableMessage>();
	}

	public ConfigurableMessage getMessage(String playerName, String rankName) {
		ConfigurableMessage message = getPlayerMessage(playerName);
		if (message != null) return message;
		 message = getRankMessage(rankName);
		if (message != null) return message;
		return this._defaultMessage;
	}

	private ConfigurableMessage getPlayerMessage(String playerName) {
		if (playerName == null) return null;
		ConfigurableMessage message = this._playerMessages.get(playerName);
		if (message != null) return message;
		String playerPath = getPlayerPath(playerName);
		message = this._config.getConfigurableMessage(playerPath, 0, null);
		if (message == null) return null;
		this._playerMessages.put(playerName, message);
		return message;
	}

	private ConfigurableMessage getRankMessage(String rankName) {
		if (rankName == null) return null;
		ConfigurableMessage message = this._rankMessages.get(rankName);
		if (message != null) return message;
		String rankPath = getRankPath(rankName);
		message = this._config.getConfigurableMessage(rankPath, 0, null);
		if (message == null) return null;
		this._rankMessages.put(rankName, message);
		return message;
	}

	private String getPlayerPath(String playerName) {
		return String.format("%s.player.%s", this._configurationPath, playerName);
	}

	private String getRankPath(String rankName) {
		return String.format("%s.rank.%s", this._configurationPath, rankName);
	}
}
