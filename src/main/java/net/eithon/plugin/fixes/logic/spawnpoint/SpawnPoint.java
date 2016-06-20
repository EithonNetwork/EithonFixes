package net.eithon.plugin.fixes.logic.spawnpoint;

import java.util.UUID;

import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.JsonObject;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

class SpawnPoint extends JsonObject<SpawnPoint> {
	private UUID _id;
	private String _name;
	private EithonLocation _location;
	private EithonPlayer _creator;
	private long _distance;

	public SpawnPoint(Player player, String name, long distance)
	{
		this._id = UUID.randomUUID();
		this._name = name;
		edit(player, distance);
	}

	public void edit(Player player, long distance) {
		this._location = new EithonLocation(player.getLocation());
		this._creator = new EithonPlayer(player);
		this._distance = distance;
	}

	public boolean maybeTeleportToSpawnPoint(Player player) {
		if (!inDistance(player)) return false;
		teleportPlayerHere(player);
		return true;
	}

	private boolean inDistance(Player player) {
		if (!isSameWorld(player.getWorld())) return false;
		Block spawnBlock = this._location.getLocation().getBlock();
		Block playerBlock = player.getLocation().getBlock();
		return (inDistance(spawnBlock.getX(), playerBlock.getX())
				&& inDistance(spawnBlock.getY(), playerBlock.getY())
				&& inDistance(spawnBlock.getZ(), playerBlock.getZ()));
	}

	private boolean inDistance(int spawn, int player) {
		return ((player + this._distance > spawn) 
				&& (player - this._distance < spawn));
	}

	private boolean isSameWorld(World world) {
		return this._location.getLocation().getWorld().equals(world);
	}

	SpawnPoint()	{ }

	@Override
	public SpawnPoint factory() {
		return new SpawnPoint();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("id", this._id.toString());
		json.put("name", this._name);
		json.put("creator", this._creator.toJson());
		json.put("location", this._location.toJson());
		json.put("distance", this._distance);
		return json;
	}

	@Override
	public SpawnPoint fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._id = UUID.fromString((String) jsonObject.get("id"));
		this._name = (String) jsonObject.get("name");
		this._creator = EithonPlayer.getFromJson(jsonObject.get("creator"));
		this._location = EithonLocation.getFromJson(jsonObject.get("location"));
		this._distance = ((long) jsonObject.get("distance"));
		return this;	
	}

	public static SpawnPoint getFromJson(Object json) {
		return new SpawnPoint().fromJson(json);
	}

	public String getName() { return this._name; }

	public long getDistance() { return this._distance; }

	public void teleportPlayerHere(Player player) {
		player.teleport(this._location.getLocation());
	}

	public String toString() {
		return String.format("%s: %d blocks", this._name, this._distance);
	}
}
