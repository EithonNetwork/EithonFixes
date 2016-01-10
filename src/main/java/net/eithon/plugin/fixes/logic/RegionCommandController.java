package net.eithon.plugin.fixes.logic;

import java.io.File;
import java.util.HashMap;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.plugin.PluginMisc;
import net.eithon.library.time.TimeMisc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

class RegionCommandController {
	private WorldEditPlugin _worldEditPlugin;	
	private HashMap<String, RegionCommand> _regionCommandsByName = null;
	private EithonPlugin _eithonPlugin;

	public RegionCommandController(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
		this._regionCommandsByName = new HashMap<String, RegionCommand>();
		if (!PluginMisc.isPluginEnabled("WorldEdit")) {
			eithonPlugin.getEithonLogger().error("Expected WorldEdit to be enabled");
			return;
		}
		Plugin plugin = PluginMisc.getPlugin("WorldEdit");
		if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
			eithonPlugin.getEithonLogger().error("Expected to be able to get the WorldEditPlugin");
			return;
		}
		delayedLoad(this._eithonPlugin, 0);
		this._worldEditPlugin = (WorldEditPlugin) plugin;
	}
	
	public String[] getAllRegionCommands() {
		return this._regionCommandsByName.keySet().toArray(new String[0]);
	}
	
	public void updateOrCreateRegionCommand(Player player, String name, String commands, boolean onEnter, boolean triggerOnEnterFromOtherWorld) {
		Selection selection =  this._worldEditPlugin.getSelection(player);
		if (selection == null) {
			player.sendMessage("No selection found");
			return;
		}

		if (!(selection instanceof CuboidSelection)) {
			player.sendMessage("Selection was not a cuboid selection.");
			return;
		}

		Vector minVector = selection.getNativeMinimumPoint();
		Vector maxVector = selection.getNativeMaximumPoint();

		Location minLocation = new Location(player.getWorld(), minVector.getX(), minVector.getY(), minVector.getZ());
		Location maxLocation = new Location(player.getWorld(), maxVector.getX(), maxVector.getY(), maxVector.getZ());
		
		Block minBlock = minLocation.getBlock();
		Block maxBlock = maxLocation.getBlock();
		
		RegionCommand regionCommand = new RegionCommand(player, name, commands, triggerOnEnterFromOtherWorld, onEnter, minBlock, maxBlock);
		this._regionCommandsByName.put(name, regionCommand);
		delayedSave(this._eithonPlugin, 0);
		player.sendMessage(String.format("Added RegionCommand %s", regionCommand.toString()));
	}

	public void editRegionCommand(Player player, String name, String commands, boolean onEnter, boolean triggerOnEnterFromOtherWorld) {
		RegionCommand regionCommand = getRegionCommandOrInformPlayer(player,
				name);
		if (regionCommand == null) return;
		regionCommand.edit(player, commands, onEnter, triggerOnEnterFromOtherWorld);
		delayedSave(this._eithonPlugin, 0);
		player.sendMessage(String.format("Edited RegionCommand %s", regionCommand.toString()));
	}

	public void editRegionCommand(Player player, String name, boolean onEnter, boolean onOtherWorld) {
		RegionCommand regionCommand = getRegionCommandOrInformPlayer(player,
				name);
		if (regionCommand == null) return;
		regionCommand.edit(player, onEnter, onOtherWorld);
		delayedSave(this._eithonPlugin, 0);
		player.sendMessage(String.format("Edited RegionCommand %s", regionCommand.toString()));
	}

	public void deleteRegionCommand(Player player, String name) {
		RegionCommand regionCommand = getRegionCommandOrInformPlayer(player, name);
		if (regionCommand == null) return;
		this._regionCommandsByName.remove(name);
		delayedSave(this._eithonPlugin, 0);
		player.sendMessage(String.format("Deleted RegionCommand %s", regionCommand.toString()));
	}

	public void gotoRegionCommand(Player player, String name) {
		RegionCommand regionCommand = getRegionCommandOrInformPlayer(player,
				name);
		if (regionCommand == null) return;
		regionCommand.teleportTo(player);
	}

	private RegionCommand getRegionCommandOrInformPlayer(Player player,
			String name) {
		RegionCommand regionCommand = this._regionCommandsByName.get(name);
		if (regionCommand == null) {
			player.sendMessage(String.format("Could not find RegionCommand %s", name));
			return null;
		}
		return regionCommand;
	}

	public void listRegionCommands(CommandSender sender) {
		for (RegionCommand regionCommand : this._regionCommandsByName.values()) {
			sender.sendMessage(regionCommand.toString());
		}
	}

	public void playerMovedOneBlockAsync(Player player, Block fromBlock, Block toBlock) {
		for (RegionCommand regionCommand : this._regionCommandsByName.values()) {
			regionCommand.maybeExecuteCommand(this._eithonPlugin, player, fromBlock, toBlock);
		}
	}

	public void delayedSave(JavaPlugin plugin, double seconds)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				save();
			}
		}, TimeMisc.secondsToTicks(seconds));		
	}

	public void delayedLoad(JavaPlugin plugin, double seconds)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				load();
			}
		}, TimeMisc.secondsToTicks(seconds));		
	}

	@SuppressWarnings("unchecked")
	public
	void save() {
		JSONArray array = new JSONArray();
		for (RegionCommand rc : this._regionCommandsByName.values()) {
			array.add(rc.toJson());
		}
		if ((array == null) || (array.size() == 0)) {
			this._eithonPlugin.getEithonLogger().info("No RegionCommands saved.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Saving %d RegionCommands", array.size());
		File file = getRegionCommandsStorageFile();
		
		FileContent fileContent = new FileContent("RegionCommands", 1, array);
		fileContent.save(file);
	}

	private File getRegionCommandsStorageFile() {
		File file = this._eithonPlugin.getDataFile("regioncommands.json");
		return file;
	}

	void load() {
		File file = getRegionCommandsStorageFile();
		FileContent fileContent = FileContent.loadFromFile(file);
		if (fileContent == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "File was empty.");
			return;			
		}
		JSONArray array = (JSONArray) fileContent.getPayload();
		if ((array == null) || (array.size() == 0)) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The list of RegionCommands was empty.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Restoring %d RegionCommands from loaded file.", array.size());
		this._regionCommandsByName = new HashMap<String, RegionCommand>();
		for (int i = 0; i < array.size(); i++) {
			this.add(RegionCommand.getFromJson((JSONObject) array.get(i)));
		}
	}

	private void add(RegionCommand regionCommand) {
		this._regionCommandsByName.put(regionCommand.getName(), regionCommand);
	}
}
