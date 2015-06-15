package net.eithon.plugin.fixes.logic;

import java.util.UUID;

public class CoolDownInfo {
	private String _comparableString;
	private int _length;
	private String _command;
	private UUID _id;
	private int _coolDownInSeconds;
	public CoolDownInfo(String command, int time) {
		this._command = command;
		this._coolDownInSeconds = time;
		this._id = UUID.randomUUID();
		this._comparableString = makeComparable(command);
		this._length = this._comparableString.length();
	}
	public String getName() { return this._command;	}
	public int getCoolDownPeriodInSeconds() { return this._coolDownInSeconds; }
	public UUID getId() { return this._id; }
	public String getComparableString() { return this._comparableString; }
	public boolean isSame(String command) {
		String comparable = makeComparable(command);
		if (comparable.length() < this._comparableString.length()) return false;
		return (comparable.substring(0, this._length).equalsIgnoreCase(this._comparableString));
	}
	
	private String makeComparable(String command) {
		return command.trim().replace(" ", "");
	}
}
