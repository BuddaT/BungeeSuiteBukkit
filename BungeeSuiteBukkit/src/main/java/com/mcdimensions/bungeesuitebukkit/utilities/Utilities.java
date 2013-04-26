package com.mcdimensions.bungeesuitebukkit.utilities;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import net.buddat.bungeesuite.database.Database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.mcdimensions.bungeesuitebukkit.BungeeSuiteBukkit;
import com.mcdimensions.bungeesuitebukkit.portals.FillType;
import com.mcdimensions.bungeesuitebukkit.portals.Portal;
import com.mcdimensions.bungeesuitebukkit.portals.Region;
import com.mcdimensions.bungeesuitebukkit.signs.BungeeSign;
import com.mcdimensions.bungeesuitebukkit.signs.ServerInfo;

public class Utilities {
	BungeeSuiteBukkit plugin;
	Database db;

	public Utilities(BungeeSuiteBukkit bungeeSuiteBukkit) {
		plugin = bungeeSuiteBukkit;
		db = plugin.database;
	}

	public boolean serverExists(String name) throws SQLException {
		boolean check = db
				.existenceQuery("SELECT ServerName FROM BungeeServers WHERE ServerName = '"
						+ name + "'");
		return check;
	}

	public boolean portalExists(String name) throws SQLException {
		boolean check = db
				.existenceQuery("SELECT Name FROM BungeePortals WHERE Name = '"
						+ name + "'");
		return check;
	}

	public boolean warpExists(String name) throws SQLException {
		boolean check = db
				.existenceQuery("SELECT Name FROM BungeeWarps WHERE Name = '"
						+ name + "'");
		return check;
	}

	public boolean portalLocationExists(String name) throws SQLException {
		boolean check = db
				.existenceQuery("SELECT Name FROM BungeeWarps WHERE Name = '"
						+ name + "'");
		return check;
	}

	public boolean ServerStatus(String server) throws SQLException {
		boolean check = db
				.existenceQuery("SELECT Online FROM BungeeServers WHERE ServerName = '"
						+ server + "' AND Online =TRUE");
		return check;
	}

	public String getMOTD(String server) throws SQLException {
		String motd = db.singleResultStringQuery("SELECT MOTD FROM BungeeServers WHERE ServerName = '"
						+ server + "'");
		return motd;
	}

	public void setMOTD() throws SQLException {
		db.update("UPDATE BungeeServers SET MOTD = ? WHERE ServerName = ?", plugin.motd, Bukkit.getServerName());
	}

	public void setOnline() throws SQLException {
		db.update("UPDATE BungeeServers SET Online=TRUE, MaxPlayers = ? WHERE ServerName = ?",
				Bukkit.getMaxPlayers(), Bukkit.getServerName());
	}

	public void setOffline() throws SQLException {
		db.update("UPDATE BungeeServers SET Online=FALSE WHERE ServerName = ?",
				Bukkit.getServerName());
	}

	public void updateAllPlayerSigns() {

	}

	public void updateAllMOTDSigns() {

	}

	public void updateAllPortSigns() {

	}

	public String getType(Sign sign) throws SQLException {
		String type = db.singleResultStringQuery(
				"SELECT Type FROM BungeeSignLocations WHERE  World ='"
						+ sign.getWorld().getName()
						+ "' AND Server = '"
						+ Bukkit.getServerName()
						+ "' AND X="
						+ sign.getBlock().getX()
						+ " AND Y="
						+ sign.getBlock().getY()
						+ " AND Z="
						+ sign.getBlock().getZ() + "");
		return type;
	}

	public String getTarget(Sign sign) throws SQLException {
		String target = db.singleResultStringQuery("Select TargetServer FROM BungeeSignLocations WHERE Server = '"
						+ Bukkit.getServerName()
						+ "' AND World ='"
						+ sign.getWorld().getName()
						+ "' AND X="
						+ sign.getBlock().getX()
						+ " AND Y="
						+ sign.getBlock().getY()
						+ " AND Z="
						+ sign.getBlock().getZ() + "");
		return target;
	}

	public void updatePlayerCount(int i) throws SQLException {
		db.update("UPDATE BungeeServers SET PlayersOnline = ? WHERE ServerName = ?",
				(Bukkit.getOnlinePlayers().length + i), Bukkit.getServerName());
	}

	public void createPortal(String name, String toServer, Region portalArea,
			FillType fillType) throws SQLException {
		Region region = new Region(portalArea.getFirst(), portalArea.getEnd());
		Portal portal = new Portal(name, toServer, region, fillType);
		int MaxY = portalArea.getEnd().getBlockY();
		int MinY = portalArea.getFirst().getBlockY();
		int MaxX = portalArea.getEnd().getBlockX();
		int MinX = portalArea.getFirst().getBlockX();
		int MaxZ = portalArea.getEnd().getBlockZ();
		int MinZ = portalArea.getFirst().getBlockZ();
		db.update("INSERT INTO BungeePortals (Name, Server, ToServer, World, XMax, XMin, YMax, YMin, ZMax, ZMin) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
		name, Bukkit.getServerName(), toServer, portalArea.getEnd().getWorld().getName(), MaxX, MinX, MaxY, MinY, MaxZ, MinZ);
		plugin.portals.put(name, portal);
	}

	public void createPortal(String name, String toServer, String toWarp,
			Region portalArea, FillType fillType) throws SQLException {
		if (toServer == null) {
			toServer = getWarpServer(toWarp);
		}
		Region region = new Region(portalArea.getFirst(), portalArea.getEnd());
		Portal portal = new Portal(name, toServer, toWarp, region, fillType);
		int MaxY = portalArea.getEnd().getBlockY();
		int MinY = portalArea.getFirst().getBlockY();
		int MaxX = portalArea.getEnd().getBlockX();
		int MinX = portalArea.getFirst().getBlockX();
		int MaxZ = portalArea.getEnd().getBlockZ();
		int MinZ = portalArea.getFirst().getBlockZ();
		if (toWarp == null) {
			db.update("INSERT INTO BungeePortals (Name, Server, ToServer, World, XMax, XMin, YMax, YMin, ZMax, ZMin) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					name,
					Bukkit.getServerName(),
					toServer,
					portalArea.getEnd().getWorld().getName(),
					MaxX,
					MinX,
					MaxY,
					MinY,
					MaxZ,
					MinZ);
		} else {
			db.update("INSERT INTO BungeePortals (Name, Server, ToServer, Warp, World, XMax, XMin, YMax, YMin, ZMax, ZMin) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					name,
					Bukkit.getServerName(),
					toServer,
					toWarp,
					portalArea.getEnd().getWorld().getName(),
					MaxX,
					MinX,
					MaxY,
					MinY,
					MaxZ,
					MinZ);
		}
		plugin.portals.put(name, portal);
	}

	public String getWarpServer(String warp) throws SQLException {
		String server = db.singleResultStringQuery("Select Server FROM BungeeWarps WHERE Name = '"
						+ warp + "'");
		return server;
	}

	public void deletePortal(Portal portal) throws SQLException {
		db.update("DELETE FROM BungeePortals WHERE Name = ?", portal.getTag());
		plugin.portals.remove(portal);
	}

	public void deletePortal(String portal) throws SQLException {
		db.update("DELETE FROM BungeePortals WHERE Name = ?", portal);
		plugin.portals.remove(getPortal(portal));
	}

	public Portal getPortal(String name) {
		return plugin.portals.get(name);
	}

	public HashMap<String, Portal> loadPortals() throws SQLException {
		HashMap<String, Portal> portals = new HashMap<String, Portal>();
		try (Connection connection = db.getConnection();
				ResultSet res = db
				.sqlQuery("SELECT * FROM BungeePortals WHERE Server = '"
						+ Bukkit.getServerName() + "'", connection)) {
			while (res.next()) {
				String name = res.getString("Name");
				FillType ft = FillType.getFillType("WATER");
				World world = Bukkit.getWorld(res.getString("World"));
				Location first = new Location(world, res.getDouble("XMax"),
						res.getDouble("YMax"), res.getDouble("ZMax"));
				Location end = new Location(world, res.getInt("XMin"),
						res.getInt("YMin"), res.getInt("ZMin"));
				Region region = new Region(first, end);
				String toServer = res.getString("ToServer");
				String toWarp = res.getString("Warp");
				Portal portal;
				if (toWarp == null) {
					portal = new Portal(name, toServer, region, ft);
				} else {
					portal = new Portal(name, toServer, toWarp, region, ft);
				}
				portals.put(name, portal);
			}
			return portals;
		}
	}

	public Portal getPortalByPosition(Location l, double offset) {
		for (Portal p : plugin.getPortals()) {
			if (p.containsLocation(l, offset) && p.isActive())
				return p;
		}
		return null;
	}

	public Portal getPortalByPosition(Location l) {
		return this.getPortalByPosition(l, 0);
	}

	public void TeleportPlayerServer(String server, Player player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			out.writeUTF("Connect");
			out.writeUTF(server); // Target Server
		} catch (IOException e) {
			// Can never happen
		}
		player.sendPluginMessage(this.plugin, BungeeSuiteBukkit.OUTGOING_BUNGEECORD_CHANNEL, b.toByteArray());
	}

	public void createWarp(String name, Location loc, boolean visible)
			throws SQLException {
		db.update("INSERT INTO BungeeWarps(Name,Server,World,X,Y,Z,Yaw,Pitch,Visible) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
				name,
				Bukkit.getServerName(),
				loc.getWorld().getName(),
				loc.getX(),
				loc.getY(),
				loc.getZ(),
				loc.getYaw(),
				loc.getPitch(),
				visible);
	}

	public void warp(String name, Player player) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		out.writeUTF("warp");
		out.writeUTF(name);
		out.writeUTF(player.getName());
		player.sendPluginMessage(plugin, BungeeSuiteBukkit.OUTGOING_PLUGIN_CHANNEL, b.toByteArray());
	}

	public void deleteWarp(String name) throws SQLException {
		db.update("DELETE FROM BungeeWarps WHERE Name = ?", name);
	}

	public HashMap<String, ServerInfo> getServers() throws SQLException {
		HashMap<String, ServerInfo> list = new HashMap<String, ServerInfo>();
		try (Connection connection = db.getConnection();
				ResultSet res = db.sqlQuery("SELECT * FROM BungeeServers", connection)) {
			while (res.next()) {
				String name = res.getString("ServerName");
				list.put(name,
						new ServerInfo(plugin, name, res.getInt("PlayersOnline"),
								res.getInt("MaxPlayers"), res.getString("MOTD"),
								res.getBoolean("Online")));
			}
			return list;
		}
	}

	public HashMap<String, String> getSignFormats() throws SQLException {
		HashMap<String, String> list = new HashMap<String, String>();
		try (Connection connection = db.getConnection();
				ResultSet res = db.sqlQuery("SELECT * FROM BungeeSignFormats", connection)) {
			while (res.next()) {
				list.put("ColoredMOTD", res.getBoolean("ColoredMOTD") + "");
				list.put("MOTDOnline", res.getString("MOTDOnline"));
				list.put("MOTDOffline", res.getString("MOTDOffline"));
				list.put("PlayerCountOnline", res.getString("PlayerCountOnline"));
				list.put("PlayerCountOnlineClick",
						res.getString("PlayerCountOnlineClick"));
				list.put("PlayerCountOffline", res.getString("PlayerCountOffline"));
				list.put("PlayerCountOfflineClick",
						res.getString("PlayerCountOfflineClick"));
				list.put("PortalFormatOnline", res.getString("PortalFormatOnline"));
				list.put("PortalFormatOffline",
						res.getString("PortalFormatOffline"));
				list.put("PortalFormatOfflineClick",
						res.getString("PortalFormatOfflineClick"));
			}
			return list;
		}
	}

	public int getOnlinePlayerCount(String serverName) throws SQLException {
		try (Connection connection = db.getConnection();
				ResultSet res = db.sqlQuery("SELECT PlayersOnline FROM BungeeServers WHERE ServerName = '"
								+ serverName + "'", connection);				) {
			int players = 0;
			while (res.next()) {
				players = res.getInt("PlayersOnline");
			}
	
			return players;
		}
	}

	public HashMap<String, ArrayList<BungeeSign>> loadSigns()
			throws SQLException {
		HashMap<String, ArrayList<BungeeSign>> list = new HashMap<String, ArrayList<BungeeSign>>();
		Set<String> servers = plugin.servers.keySet();
		try (Connection connection = db.getConnection()) {
			for (String server : servers) {
				ArrayList<BungeeSign> signs = new ArrayList<BungeeSign>();
				try (ResultSet res = db.sqlQuery("SELECT * FROM BungeeSignLocations WHERE Server = '"
								+ Bukkit.getServerName()
								+ "' AND TargetServer = '"+server+"'", connection)) {
					while (res.next()) {
						try{
							BungeeSign sign = new BungeeSign(res.getString("Type"),
									(Sign) (new Location(Bukkit.getWorld(res
											.getString("World")), res.getDouble("X"),
											res.getDouble("Y"), res.getDouble("Z")))
											.getBlock().getState(), res.getString("TargetServer"));
							signs.add(sign);
							plugin.AllSigns.add(sign);
						} catch (ClassCastException e){
							db.update("DELETE FROM BungeeSignLocations WHERE World = ? AND X = ? AND Y = ? AND Z = ?", res.getString("World"), res.getInt("X"), res.getInt("Y"), res.getInt("Z"));
						}
					}
				}
				list.put(server, signs);
			}
			return list;
		}
	}

	public void createSign(BungeeSign bs) throws SQLException {
		Sign sign = bs.getSign();
		db.update("INSERT INTO BungeeSignLocations (Type,Server,TargetServer,World,X,Y,Z) VALUES (?, ?, ?, ?, ?, ?, ?)", bs.getType(), Bukkit.getServerName(), bs.getServer(), sign.getWorld().getName(), sign.getX(), sign.getY(), sign.getZ());
	}

}
