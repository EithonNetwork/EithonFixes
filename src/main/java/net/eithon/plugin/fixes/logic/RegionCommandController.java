package net.eithon.plugin.fixes.logic;

import java.io.File;
import java.util.HashMap;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.move.IBlockMoverFollower;
import net.eithon.library.move.MoveEventHandler;
import net.eithon.library.plugin.PluginMisc;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class RegionCommandController implements IBlockMoverFollower {
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
		MoveEventHandler.addBlockMover(this); 
	}
	
	public void updateOrCreateRegionCommand(Player player, String name, String command, boolean onEnter) {
		Selection selection =  this._worldEditPlugin.getSelection(player);
		if (selection == null) {
			player.sendMessage("No selection found");
			return;
		}

		if (!(selection instanceof CuboidSelection)) {
			player.sendMessage("Selection was not a cuboid selection.");
			return;
		}
		
		if (command.startsWith("/")) command = command.substring(1);

		Vector minVector = selection.getNativeMinimumPoint();
		Vector maxVector = selection.getNativeMaximumPoint();

		Location minLocation = new Location(player.getWorld(), minVector.getX(), minVector.getY(), minVector.getZ());
		Location maxLocation = new Location(player.getWorld(), maxVector.getX(), maxVector.getY(), maxVector.getZ());
		
		Block minBlock = minLocation.getBlock();
		Block maxBlock = maxLocation.getBlock();
		
		RegionCommand regionCommand = new RegionCommand(player, name, command, onEnter, minBlock, maxBlock);
		this._regionCommandsByName.put(name, regionCommand);
		delayedSave(this._eithonPlugin, 0);
		player.sendMessage(String.format("Added RegionCommand %s", regionCommand.toString()));
	}

	public void editRegionCommand(Player player, String name, String command, boolean onEnter) {
		RegionCommand regionCommand = getRegionCommandOrInformPlayer(player,
				name);
		if (regionCommand == null) return;
		if (command.startsWith("/")) command = command.substring(1);
		regionCommand.edit(player, command, onEnter);
		delayedSave(this._eithonPlugin, 0);
		player.sendMessage(String.format("Edited RegionCommand %s", regionCommand.toString()));
	}

	public void deleteRegionCommand(CommandSender sender, String name) {
		this._regionCommandsByName.remove(name);
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

	@Override
	public void moveEventHandler(PlayerMoveEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		Block fromBlock = event.getFrom().getBlock();
		Block toBlock = event.getTo().getBlock();
		for (RegionCommand regionCommand : this._regionCommandsByName.values()) {
			regionCommand.maybeExecuteCommand(player, fromBlock, toBlock);
		}
	}

	@Override
	public String getName() {
		return "EithonFixes.RegionCommandController";
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