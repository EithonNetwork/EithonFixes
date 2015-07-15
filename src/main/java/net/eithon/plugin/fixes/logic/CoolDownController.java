package net.eithon.plugin.fixes.logic;

import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.CoolDown;
import net.eithon.plugin.fixes.Config;

import org.bukkit.entity.Player;

class CoolDownController {
	private HashMap<UUID, CoolDown> _coolDownHashMap;
	private Logger _eithonLogger;

	public CoolDownController(EithonPlugin plugin){
		this._eithonLogger = plugin.getEithonLogger();
		CoolDownInfo.initialize(plugin);
		this._coolDownHashMap = new HashMap<UUID, CoolDown>();
		for (CoolDownInfo info : Config.V.coolDownInfos) {
			this._coolDownHashMap.put(info.getId(), new CoolDown(info.getName(), info.getCoolDownPeriodInSeconds()));
		}
	}

	public long secondsLeftOfCoolDown(Player player, String command) {
		verbose("secondsLeftOfCoolDown", "Enter");
		CoolDown coolDown = getCoolDown(command);
		if (coolDown == null) {
			verbose("secondsLeftOfCoolDown", "No cooldown found.");
			verbose("secondsLeftOfCoolDown", "return 0.");
			return 0;
		}
		if (player.hasPermission("eithonfixes.nocooldown")) {
			verbose("secondsLeftOfCoolDown", "Player \"%s\" has permission eithonfixes.nocooldown.", player.getName());
			verbose("secondsLeftOfCoolDown", "return 0.");
			return 0;			
		}
		
		long secondsLeft = coolDown.secondsLeft(player);
		if (secondsLeft > 0) {
			verbose("secondsLeftOfCoolDown", "Player \"%s\" is in cooldown.", player.getName());
			verbose("secondsLeftOfCoolDown", "return secondsLeft.");
			return secondsLeft;
		}
		coolDown.addPlayer(player);
		verbose("secondsLeftOfCoolDown", "Player \"%s\" added to cooldown.", player.getName());
		verbose("secondsLeftOfCoolDown", "return 0.");
		return 0;
	}

	private CoolDown getCoolDown(String command) {
		verbose("getCoolDown", "Enter");
		CoolDownInfo info = getCoolDownInfo(command);
		if (info == null) {
			verbose("getCoolDown", "Command \"%s\" not found.", command);
			verbose("getCoolDown", "return null");
			return null;
		}
		verbose("getCoolDown", "Command \"%s\" found.", command);
		verbose("getCoolDown", "return CoolDown object.");
		return this._coolDownHashMap.get(info.getId());
	}

	private CoolDownInfo getCoolDownInfo(String command) {
		for (CoolDownInfo info : Config.V.coolDownInfos) {
			if (info.isSame(command)) return info;
		}
		return null;
	}
	
	private void verbose(String method, String format, Object... args)
	{
		String message = CoreMisc.safeFormat(format, args);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}