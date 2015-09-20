package net.eithon.plugin.fixes.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.eithon.library.plugin.Configuration;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("CommandScheduler")
public class CommandScheduler implements ConfigurationSerializable  {
	private HashMap<String, CommandSchedule> _commandScheduleList;
	
	public static void initialize() {
		ConfigurationSerialization.registerClass(CommandSchedule.class, "CommandSchedule");
		ConfigurationSerialization.registerClass(CommandScheduler.class, "CommandScheduler");
	}

	public CommandScheduler() {
		this._commandScheduleList = new HashMap<String, CommandSchedule>();
	}
	
	public void start() {
		for (CommandSchedule schedule : this._commandScheduleList.values()) {
			schedule.start();
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		this._commandScheduleList.forEach((key, value) -> map.put(key, value));
		return map;
	}

	public static CommandScheduler deserialize(Map<String, Object> map) {
		CommandScheduler pbm = new CommandScheduler();
		pbm._commandScheduleList = toCommandScheduleMap(map);
		Bukkit.getLogger().info(String.format("CommandScheduler.deserialize() = \"%s\"", pbm.toString()));
		return pbm;
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, CommandSchedule> toCommandScheduleMap(Map<String, Object> map) {
		HashMap<String, CommandSchedule> newMap = new HashMap<String, CommandSchedule>();		
		if (map == null) return newMap;
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof CommandSchedule) {
				newMap.put(entry.getKey(), (CommandSchedule) value);
			} else if (value instanceof Map<?,?>) {
				newMap.put(entry.getKey(), CommandSchedule.deserialize((Map<String, Object>) value));
			}
		}
		return newMap;
	}

	public String toString()
	{
		final StringBuilder result = new StringBuilder("");
		if (this._commandScheduleList == null) return result.toString();
		this._commandScheduleList.forEach((key, value) -> result.append(String.format("\n  %s = %s", key, value == null ? "null" : value.toString())));
		return result.toString();
	}

	public static CommandScheduler getFromConfig(Configuration config,
			String path) {
		CommandScheduler defaultValue = new CommandScheduler();
		Object object = config.getObject(path, defaultValue);
		if (object == null) return defaultValue;
		if (object instanceof CommandScheduler) return (CommandScheduler) object;
		Map<String, Object> map = config.getMap(path, true);
		return deserialize(map);
	}
}
