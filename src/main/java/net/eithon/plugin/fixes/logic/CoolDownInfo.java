package net.eithon.plugin.fixes.logic;

import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.EithonLogger.DebugPrintLevel;

public class CoolDownInfo {
	private String _comparableString;
	private int _length;
	private String _stringToCompare;
	private UUID _id;
	private long _coolDownInSeconds;
	private int _allowedIncidents;
	private static EithonPlugin eithonPlugin;
	
	public CoolDownInfo(String string, long time, int incidents) {
		this._stringToCompare = string;
		this._coolDownInSeconds = time;
		this._id = UUID.randomUUID();
		this._comparableString = makeComparable(string);
		this._length = this._comparableString.length();
		this._allowedIncidents = incidents;
	}
	
	public static void initialize(EithonPlugin plugin) {
		eithonPlugin = plugin;
	}
	
	public String getName() { return this._stringToCompare;	}
	public long getCoolDownPeriodInSeconds() { return this._coolDownInSeconds; }
	public int getAllowedIncidents() { return this._allowedIncidents; }
	public UUID getId() { return this._id; }
	public String getComparableString() { return this._comparableString; }
	public boolean isSame(String string) {
		String comparable = makeComparable(string);
		eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "isSame: %s, %s", this._comparableString, comparable);
		if (comparable.length() < this._comparableString.length()) return false;
		return (comparable.substring(0, this._length).equals(this._comparableString));
	}
	
	private String makeComparable(String string) {
		return string.trim().replace(" ", "").toLowerCase();
	}
}
