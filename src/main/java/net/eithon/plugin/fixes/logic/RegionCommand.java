package net.eithon.plugin.fixes.logic;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonBlock;
import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.IJson;
import net.eithon.library.plugin.Logger.DebugPrintLevel;

public class RegionCommand implements IJson<RegionCommand> {
	private static EithonPlugin eithonPlugin;

	private UUID _id;
	private String _name;
	private String _command;
	private EithonLocation _createdFrom;
	private EithonPlayer _creator;
	private EithonBlock _min;
	private EithonBlock _max;
	private boolean _onEnter;

	public static void initialize(EithonPlugin plugin) {
		eithonPlugin = plugin;
	}

	public RegionCommand(Player player, String name, String command, boolean onEnter, Block min, Block max)
	{
		this._id = UUID.randomUUID();
		this._name = name;
		edit(player, command, onEnter);
		this._createdFrom = new EithonLocation(player.getLocation());
		this._min = new EithonBlock(min);
		this._max = new EithonBlock(max);
	}

	public void edit(Player player, String command, boolean onEnter) {
		if (command.startsWith("/")) command = command.substring(1);
		this._creator = new EithonPlayer(player);
		this._command = command;
		this._onEnter = onEnter;
	}

	public boolean maybeExecuteCommand(Player player, Location from, Location to) {
		return maybeExecuteCommand(player, from.getBlock(), to.getBlock());
	}

	public boolean maybeExecuteCommand(Player player, Block from, Block to) {
		if (this._onEnter) {
			if (!inRegion(to)) return false;
			if (inRegion(from)) return false;
		} else {
			if (!inRegion(from)) return false;
			if (inRegion(to)) return false;
		}
		player.getServer().dispatchCommand(player, this._command);
		return true;
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
		json.put("command", this._command);
		json.put("creator", this._creator.toJson());
		json.put("createdFrom", this._createdFrom.toJson());
		json.put("min", this._min.toJson());
		json.put("max", this._max.toJson());
		json.put("onEnter", this._onEnter ? 1 : 0);
		return json;
	}

	@Override
	public RegionCommand fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._id = UUID.fromString((String) jsonObject.get("id"));
		this._name = (String) jsonObject.get("name");
		this._command = (String) jsonObject.get("command");
		this._creator = EithonPlayer.getFromJson(jsonObject.get("creator"));
		this._createdFrom = EithonLocation.getFromJson(jsonObject.get("createdFrom"));
		this._min = EithonBlock.getFromJson(jsonObject.get("min"));
		this._max = EithonBlock.getFromJson(jsonObject.get("max"));
		this._onEnter = ((long) jsonObject.get("onEnter")) == 1;
		return this;	
	}

	public static RegionCommand getFromJson(Object json) {
		return new RegionCommand().fromJson(json);
	}

	public String getName() { return this._name; }

	public Object getCommand() { return this._command; }

	public void teleportTo(Player player) {
		player.teleport(this._createdFrom.getLocation());
	}
	
	public String toString() {
		return String.format("%s: \"/%s\" (%s)", this._name, this._command, this._onEnter?"enter":"leave");
	}
	
	private void verbose(String method, String format, Object... args)
	{
		if (eithonPlugin == null) return;
		String message = CoreMisc.safeFormat(format, args);
		eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}
