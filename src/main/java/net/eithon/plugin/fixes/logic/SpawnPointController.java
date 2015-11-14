package net.eithon.plugin.fixes.logic;

import java.io.File;
import java.util.HashMap;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.plugin.EithonLogger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SpawnPointController {
	private HashMap<String, SpawnPoint> _spawnPointsByName = null;
	private EithonPlugin _eithonPlugin;

	public SpawnPointController(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
		this._spawnPointsByName = new HashMap<String, SpawnPoint>();
		delayedLoad(this._eithonPlugin, 0);
	}
	
	public void updateOrCreateSpawnPoint(Player player, String name, long distance) {
		SpawnPoint spawnPoint = new SpawnPoint(player, name, distance);
		this._spawnPointsByName.put(name, spawnPoint);
		delayedSave(this._eithonPlugin, 0);
		player.sendMessage(String.format("Added SpawnPoint %s", spawnPoint.toString()));
	}

	public void editSpawnPoint(Player player, String name, long distance) {
		SpawnPoint spawnPoint = getSpawnPointOrInformPlayer(player, name);
		if (spawnPoint == null) return;
		spawnPoint.edit(player, distance);
		delayedSave(this._eithonPlugin, 0);
		player.sendMessage(String.format("Edited SpawnPoint %s", spawnPoint.toString()));
	}

	public void deleteSpawnPoint(CommandSender sender, String name) {
		this._spawnPointsByName.remove(name);
	}

	public void gotoSpawnPoint(Player player, String name) {
		SpawnPoint spawnPoint = getSpawnPointOrInformPlayer(player, name);
		if (spawnPoint == null) return;
		spawnPoint.teleportPlayerHere(player);
	}

	private SpawnPoint getSpawnPointOrInformPlayer(Player player,
			String name) {
		SpawnPoint spawnPoint = this._spawnPointsByName.get(name);
		if (spawnPoint == null) {
			player.sendMessage(String.format("Could not find SpawnPoint %s", name));
			return null;
		}
		return spawnPoint;
	}

	public boolean maybeTeleportToSpawnPoint(Player player) {
		for (SpawnPoint spawnPoint : this._spawnPointsByName.values()) {
			if (spawnPoint.maybeTeleportToSpawnPoint(player)) return true;
		}
		return false;
	}

	public void listSpawnPoints(CommandSender sender) {
		for (SpawnPoint spawnPoint : this._spawnPointsByName.values()) {
			sender.sendMessage(spawnPoint.toString());
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
		for (SpawnPoint sp : this._spawnPointsByName.values()) {
			array.add(sp.toJson());
		}
		if ((array == null) || (array.size() == 0)) {
			this._eithonPlugin.getEithonLogger().info("No SpawnPoints saved.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Saving %d SpawnPoints", array.size());
		File file = getSpawnPointStorageFile();
		
		FileContent fileContent = new FileContent("SpawnPoints", 1, array);
		fileContent.save(file);
	}

	private File getSpawnPointStorageFile() {
		File file = this._eithonPlugin.getDataFile("spawnpoints.json");
		return file;
	}

	void load() {
		File file = getSpawnPointStorageFile();
		FileContent fileContent = FileContent.loadFromFile(file);
		if (fileContent == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "File was empty.");
			return;			
		}
		JSONArray array = (JSONArray) fileContent.getPayload();
		if ((array == null) || (array.size() == 0)) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The list of SpawnPoints was empty.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Restoring %d SpawnPoints from loaded file.", array.size());
		this._spawnPointsByName = new HashMap<String, SpawnPoint>();
		for (int i = 0; i < array.size(); i++) {
			this.add(SpawnPoint.getFromJson((JSONObject) array.get(i)));
		}
	}

	private void add(SpawnPoint spawnPoint) {
		this._spawnPointsByName.put(spawnPoint.getName(), spawnPoint);
	}
}
