package net.eithon.plugin.fixes.logic.regioncommand;

import java.util.UUID;

import net.eithon.library.extensions.EithonBlock;
import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.JsonObject;
import net.eithon.library.plugin.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

class RegionCommand extends JsonObject<RegionCommand> {
	private UUID _id;
	private String _name;
	private String _commands;
	private EithonLocation _createdFrom;
	private EithonPlayer _creator;
	private EithonBlock _min;
	private EithonBlock _max;
	private boolean _onEnter;
	private boolean _triggerForOtherWorld;

	public RegionCommand(Player player, String name, String commands, boolean onEnter, boolean triggerForOtherWorld, Block min, Block max)
	{
		this._id = UUID.randomUUID();
		this._name = name;
		edit(player, commands, onEnter, triggerForOtherWorld);
		this._createdFrom = new EithonLocation(player.getLocation());
		this._min = new EithonBlock(min);
		this._max = new EithonBlock(max);
	}

	public void edit(Player player, String commands, boolean onEnter, boolean triggerForOtherWorld) {
		this._creator = new EithonPlayer(player);
		this._commands = commands;
		this._onEnter = onEnter;
		this._triggerForOtherWorld = triggerForOtherWorld;
	}


	public void edit(Player player, boolean onEnter, boolean triggerForOtherWorld) {
		this._creator = new EithonPlayer(player);
		this._onEnter = onEnter;
		this._triggerForOtherWorld = triggerForOtherWorld;
	}

	public boolean maybeExecuteCommand(Plugin plugin, Player player, Block from, Block to) {
		if (plugin == null) {
			Logger.libraryError("%s", "EithonFixes.RegionCommand.maybeExecuteCommand: Plugin was null.");
			return false;
		}
		if (this._onEnter) {
			if (!inRegion(to)) return false;
			if (!isSameWorld(from)) {
				if (!this._triggerForOtherWorld) return false;
			} else if (inRegion(from)) return false;
		} else {
			if (!inRegion(from)) return false;
			if (!isSameWorld(to)) {
				if (!this._triggerForOtherWorld) return false;
			} else if (inRegion(to)) return false;
		}	
		Bukkit.getServer().getScheduler()
		.runTask(plugin, new Runnable() {
			public void run() {
				executeCommands(player);
			}
		});
		return true;
	}

	void executeCommands(Player player) {
		String[] commands = this._commands.split(";");
		for (String command : commands) {
			boolean setOp = false;
			CommandSender commandSender = player;
			if (command.startsWith("/")) command = command.substring(1);
			if (command.startsWith("#")) {
				command = command.substring(1);
				if (this._creator.isOp()) commandSender = Bukkit.getConsoleSender();
			} else if (command.startsWith("*")) {
				command = command.substring(1);
				setOp = !player.isOp();			}
			try {
				if (setOp) player.setOp(true);
				Bukkit.getServer().dispatchCommand(commandSender, command);
			} finally {
				if (setOp) player.setOp(false);
			}
		}
	}

	private boolean inRegion(Block block) {
		if (!isSameWorld(block)) return false;
		Block min = this._min.getBlock();
		Block max = this._max.getBlock();
		if (block.getX() < min.getX()) return false;
		if (block.getX() > max.getX()) return false;
		if (block.getY() < min.getY()) return false;
		if (block.getY() > max.getY()) return false;
		if (block.getZ() < min.getZ()) return false;
		if (block.getZ() > max.getZ()) return false;
		return true;
	}

	private boolean isSameWorld(Block block) {
		World blockWorld = block.getWorld();		
		World minWorld = this._min.getBlock().getWorld();
		return blockWorld.equals(minWorld);
	}

	RegionCommand()	{ }

	@Override
	public RegionCommand factory() {
		return new RegionCommand();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("id", this._id.toString());
		json.put("name", this._name);
		json.put("command", this._commands);
		json.put("creator", this._creator.toJson());
		json.put("createdFrom", this._createdFrom.toJson());
		json.put("min", this._min.toJson());
		json.put("max", this._max.toJson());
		json.put("onEnter", this._onEnter ? 1 : 0);
		json.put("triggerOnEnterFromOtherWorld", this._triggerForOtherWorld ? 1 : 0);
		return json;
	}

	@Override
	public RegionCommand fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._id = UUID.fromString((String) jsonObject.get("id"));
		this._name = (String) jsonObject.get("name");
		this._commands = (String) jsonObject.get("command");
		this._creator = EithonPlayer.getFromJson(jsonObject.get("creator"));
		this._createdFrom = EithonLocation.getFromJson(jsonObject.get("createdFrom"));
		this._min = EithonBlock.getFromJson(jsonObject.get("min"));
		this._max = EithonBlock.getFromJson(jsonObject.get("max"));
		this._onEnter = ((long) jsonObject.get("onEnter")) == 1;
		Object object = jsonObject.get("triggerOnEnterFromOtherWorld");
		if (object == null) {
			this._triggerForOtherWorld = true;
		} else {
			this._triggerForOtherWorld = ((long) object) == 1;
		}
		return this;	
	}

	public static RegionCommand getFromJson(Object json) {
		return new RegionCommand().fromJson(json);
	}

	public String getName() { return this._name; }

	public Object getCommand() { return this._commands; }

	public void teleportTo(Player player) {
		player.teleport(this._createdFrom.getLocation());
	}

	public String toString() {
		return String.format("%s: \"/%s\" (%s, %s)", this._name, this._commands,
				this._onEnter?"enter":"leave", this._triggerForOtherWorld?"from other worlds":"within world");
	}
}
