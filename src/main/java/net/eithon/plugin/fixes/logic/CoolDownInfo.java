package net.eithon.plugin.fixes.logic;

import java.util.UUID;

public class CoolDownInfo {
	private String _command;
	private UUID _id;
	private int _coolDownInSeconds;
	public CoolDownInfo(String command, int time) {
		this._command = command;
		this._coolDownInSeconds = time;
		this._id = UUID.randomUUID();
	}
	public String getName() { return this._command;	}
	public int getCoolDownPeriodInSeconds() { return this._coolDownInSeconds; }
	public UUID getId() { return this._id; }
}
