package net.eithon.plugin.fixes.logic;

import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;

public class CoolDownInfo {
	private String _comparableString;
	private int _length;
	private String _command;
	private UUID _id;
	private int _coolDownInSeconds;
	private static EithonPlugin eithonPlugin;
	
	public CoolDownInfo(String command, int time) {
		this._command = command;
		this._coolDownInSeconds = time;
		this._id = UUID.randomUUID();
		this._comparableString = makeComparable(command);
		this._length = this._comparableString.length();
	}
	
	public static void initialize(EithonPlugin plugin) {
		eithonPlugin = plugin;
	}
	
	public String getName() { return this._command;	}
	public int getCoolDownPeriodInSeconds() { return this._coolDownInSeconds; }
	public UUID getId() { return this._id; }
	public String getComparableString() { return this._comparableString; }
	public boolean isSame(String command) {
		String comparable = makeComparable(command);
		eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "isSame: %s, %s", this._comparableString, comparable);
		if (comparable.length() < this._comparableString.length()) return false;
		return (comparable.substring(0, this._length).equals(this._comparableString));
	}
	
	private String makeComparable(String command) {
		return command.trim().replace(" ", "").toLowerCase();
	}
}
