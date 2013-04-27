package com.mcdimensions.bungeesuitebukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import net.buddat.bungeesuite.database.Database;
import net.buddat.bungeesuite.database.DatabaseDependencyException;
import net.milkbowl.vault.chat.Chat;
import net.buddat.bungeesuite.channels.ChannelCommunication;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcdimensions.bungeesuitebukkit.listeners.PluginMessengerListener;
import com.mcdimensions.bungeesuitebukkit.listeners.PortalListener;
import com.mcdimensions.bungeesuitebukkit.listeners.PlayerConnect;
import com.mcdimensions.bungeesuitebukkit.listeners.SignListener;
import com.mcdimensions.bungeesuitebukkit.listeners.VaultListener;
import com.mcdimensions.bungeesuitebukkit.portals.Portal;
import com.mcdimensions.bungeesuitebukkit.portals.RegionSelectionManager;
import com.mcdimensions.bungeesuitebukkit.signs.BungeeSign;
import com.mcdimensions.bungeesuitebukkit.signs.MOTDUpdater;
import com.mcdimensions.bungeesuitebukkit.signs.ServerInfo;
import com.mcdimensions.bungeesuitebukkit.signs.SignHandler;
import com.mcdimensions.bungeesuitebukkit.utilities.Utilities;

public class BungeeSuiteBukkit extends JavaPlugin {
	public static final String OUTGOING_PLUGIN_CHANNEL = ChannelCommunication.SERVER_TO_PROXY_CHANNEL;
	public static final String INCOMING_PLUGIN_CHANNEL = ChannelCommunication.PROXY_TO_SERVER_CHANNEL;
	public static final String OUTGOING_BUNGEECORD_CHANNEL = "BungeeCord";
	
	public String motd, OnDisableTarget;
	public Boolean dynamicMOTD, showPlayers, usingSigns, usingPortals,
			portalRegionSelectionMessage, usingWarps, usingVault;
	public Database database;
	
	public ConsoleCommandSender log;
	public Utilities utils;
	public RegionSelectionManager rsm;
	public HashMap<String, Portal> portals;
	public HashMap<String, ServerInfo> servers;
	public HashMap<String, String> signFormats;
	public HashMap<String, ArrayList<BungeeSign>> signs;
	public HashSet<BungeeSign> AllSigns;
	public SignHandler SignHandler;
	
	private long signUpdatePeriod = 200L;
	private long MOTDUpdatePeriod = 200L;

	private FileConfiguration config;
	private String username, password, databaseHost, port, url;

	// TODO: Fix Public variable
	public static Chat CHAT = null;

	@Override
	public void onEnable() {
		log = Bukkit.getServer().getConsoleSender();
		log.sendMessage(ChatColor.DARK_GREEN
				+ "========BungeeSuite Enabling========");
		log.sendMessage(ChatColor.GREEN + "- Loading Config");
		loadConfig();
		log.sendMessage(ChatColor.GREEN + "- Initialising Variables");
		try {
			initialiseVariables();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (DatabaseDependencyException e) {
			e.printStackTrace();
		}

		log.sendMessage(ChatColor.GREEN + "- Registering Plugin Channels");
		registerPluginChannels();
		log.sendMessage(ChatColor.GREEN + "- Registering Listeners");
		registerListeners();
		try {
			utils.setOnline();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(usingSigns){
			try {
				new SignHandler(this).runTaskTimerAsynchronously(this, 100, signUpdatePeriod);
			} catch (IllegalArgumentException | IllegalStateException
					| SQLException e) {
				e.printStackTrace();
			}
		}
		if(dynamicMOTD){
			new MOTDUpdater(this).runTaskTimerAsynchronously(this, 100, MOTDUpdatePeriod);
		}
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new PlayerConnect(this), this);
		if (usingSigns) {
			getServer().getPluginManager().registerEvents(
					new SignListener(this), this);
		}
		if (usingPortals) {
			getServer().getPluginManager().registerEvents(
					rsm = new RegionSelectionManager(this), this);
			getServer().getPluginManager().registerEvents(
					new PortalListener(this), this);
		}
		if(usingVault){
			getServer().getPluginManager().registerEvents(
					new VaultListener(this), this);
		}

	}

	private void registerPluginChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this, INCOMING_PLUGIN_CHANNEL, new PluginMessengerListener(this));
		Bukkit.getMessenger()
				.registerOutgoingPluginChannel(this, OUTGOING_PLUGIN_CHANNEL);
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, OUTGOING_BUNGEECORD_CHANNEL);

	}

	private void initialiseVariables() throws SQLException, DatabaseDependencyException {
		portals = new HashMap<String, Portal>();
		database = new Database(url, databaseHost, port, username, password);
		motd = Bukkit.getMotd();
		utils = new Utilities(this);
		try {
			utils.setMOTD();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (usingSigns) {
			AllSigns =new HashSet<BungeeSign>();
			servers = utils.getServers();
			signFormats = utils.getSignFormats();
			signs = utils.loadSigns();
		}
		if (usingPortals) {
			portals = utils.loadPortals();
		}
		if(usingVault){
			usingVault = setupChat();
		}
	}

	private void loadConfig() {
		this.config = getConfig();
		config.addDefault("Database.UseSQL", true);
		config.addDefault("Database.username", "username");
		config.addDefault("Database.password", "password");
		config.addDefault("Database.database", "minecraft");
		config.addDefault("Database.url", "localhost");
		config.addDefault("Database.port", "3306");
		config.addDefault("Signs.Enabled", true);
		config.addDefault("Signs.UpdatePeriod", "200");
		config.addDefault("Signs.SendDynamicMOTD", false);
		config.addDefault("Signs.MOTDUpdatePeriod", "200");
		config.addDefault("Warps.Enabled", true);
		config.addDefault("Warps.WarpOnDisable", true);
		config.addDefault("Warps.OnDisableTarget", "none");
		config.addDefault("Portals.Enabled", true);
		config.addDefault("Portals.AlertOnRegionSelection", true);
		config.addDefault("Chat.SendVaultInfo", true);
		config.addDefault("Chat.UseGroupPrefixesAndSuffixes", true);
		config.options().copyDefaults(true);
		
		saveConfig();
		
		this.username = config.getString("Database.username");
		this.password = config.getString("Database.password");
		this.databaseHost = config.getString("Database.database");
		this.url = config.getString("Database.url");
		this.port = config.getString("Database.port");
		
		this.usingSigns = config.getBoolean("Signs.Enabled");
		this.signUpdatePeriod = (Long) Long.parseLong(config
				.getString("Signs.UpdatePeriod"));
		this.dynamicMOTD = config.getBoolean("Signs.SendDynamicMOTD");
		this.MOTDUpdatePeriod = (Long) Long.parseLong(config
				.getString("Signs.MOTDUpdatePeriod"));
		
		this.usingWarps = config.getBoolean("Warps.Enabled");
		this.OnDisableTarget = config.getString("Warps.OnDisableTarget");
		
		this.usingPortals = config.getBoolean("Portals.Enabled");
		this.portalRegionSelectionMessage = config.getBoolean("Portals.AlertOnRegionSelection");
		
		this.usingVault = config.getBoolean("Chat.SendVaultInfo");
	}

	public Collection<Portal> getPortals() {
		return portals.values();
	}

	@Override
	public void onDisable() {
		Bukkit.getLogger().info("Disabling");
		try {
			utils.setOffline();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setMOTD(String motd2) {
		this.motd = motd2;
	}

	public String getMOTD() {
		return motd;
	}
	private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            CHAT = chatProvider.getProvider();
        } else {
        	usingVault = false;
        }

        return (CHAT != null);
    }
}
