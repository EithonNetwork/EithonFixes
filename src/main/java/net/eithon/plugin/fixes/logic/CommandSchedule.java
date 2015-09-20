package net.eithon.plugin.fixes.logic;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.IRepeatable;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("CommandSchedule")
public class CommandSchedule implements ConfigurationSerializable  {
	private DayOfWeek _day;
	private int _hour;
	private int _minute;
	Collection<String> _commands;
	
	private CommandSchedule() {}
	
	public CommandSchedule(DayOfWeek day, int hour, int minute, Collection<String> commands) {
		this._day = day;
		this._hour = hour;
		this._minute = minute;
		this._commands = new ArrayList<String>();
		this._commands.addAll(commands);
	}
	
	public CommandSchedule(int day, int hour, int minute, Collection<String> commands) {
		this(DayOfWeek.of(day), hour, minute, commands);
	}
	
	public void start() {
		CommandSchedule thisObject = this;
		AlarmTrigger.get().repeatEveryWeek(toString(), this._day, LocalTime.of(this._hour, this._minute),
				new IRepeatable() {
			@Override
			public boolean repeat() {
				for (String command : thisObject._commands) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
				}
				return true;
			}
		});
	}

	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("day", new Integer(getDayAsInteger()));
		map.put("hour", new Integer(this._hour));
		map.put("minute", new Integer(this._minute));
		map.put("command-list", this._commands);
		return map;
	}

	@SuppressWarnings("unchecked")
	public static CommandSchedule deserialize(Map<String, Object> map) {
		CommandSchedule schedule = new CommandSchedule();
		final int day = (int) map.get("day");
		schedule._day = DayOfWeek.of(day);
		final Object hour = map.get("hour");
		schedule._hour = hour == null ? 0 : (int) hour;
		final Object minute = map.get("minute");
		schedule._minute = minute == null ? 0 : (int) minute;
		schedule._commands = (Collection<String>) map.get("command-list");
		return schedule;		
	}
	
	private int getDayAsInteger() {
		return this._day.get(ChronoField.DAY_OF_WEEK);
	}
	
	public String toString() {
		return String.format("%s %02d:%02d %s",
				this._day.getDisplayName(TextStyle.FULL, Locale.ENGLISH), 
				this._hour, this._minute, 
				CoreMisc.collectionToString(this._commands));
	}
}
