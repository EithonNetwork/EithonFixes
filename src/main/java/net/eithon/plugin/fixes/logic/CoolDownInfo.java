package net.eithon.plugin.fixes.logic;

public class CoolDownInfo {
	public CoolDownInfo(String command, int time) {
		this._command = command;
		this._coolDownInSeconds = time;
	}
	private String _command;
	private int _coolDownInSeconds;
}
