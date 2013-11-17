package com.poixson.webxbukkit;

import java.io.IOException;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsManager;

import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.webxbukkit.webLink.LinkManager;
import com.poixson.webxbukkit.webSettings.SettingsManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


public class WebAPI extends JavaPlugin {

	private static volatile WebAPI instance = null;
	private static final Object lock = new Object();
//	private static volatile xLog log = null;

	// objects
	private volatile webConfig config = null;
@SuppressWarnings("unused")
	private volatile SettingsManager settings = null;
	private volatile PluginVersion version = null;
	private final webLanguage lang = new webLanguage();
	// web link
	private volatile LinkManager link = null;

	// database key
	private volatile String dbKey = null;

	// null=unloaded false=failed true=loaded
	private static volatile Boolean isOk = null;
	private static volatile boolean debug = false;


	// get instance
	public static WebAPI get() {
		synchronized(lock) {
			if(instance == null) throw new RuntimeException("WebAPI is not enabled! Plugin instance cannot be obtained!");
			return instance;
		}
	}
	public WebAPI() {
		super();
		synchronized(lock) {
			if(instance != null) throw new RuntimeException("API already loaded?!");
			instance = this;
		}
	}
	public static boolean isOk() {
		return isOk;
	}
	public boolean isDebug() {
		return debug;
	}


	// enable api plugin
	@Override
	public void onEnable() {
		synchronized(lock) {
			if(isOk != null) {
				getServer().getConsoleSender().sendMessage(ChatColor.RED+"************************************");
				getServer().getConsoleSender().sendMessage(ChatColor.RED+"*** WebAPI is already running!!! ***");
				getServer().getConsoleSender().sendMessage(ChatColor.RED+"************************************");
				return;
			}
			isOk = false;
			if(instance == null)
				instance = this;
		}
		// load vault economy
		if(Plugins3rdParty.get().getEconomy() == null)
			System.out.println("Economy plugin not found");
		else
			System.out.println("Economy plugin found");
		// load world guard
		if(Plugins3rdParty.get().getWorldGuard() == null)
			System.out.println("WorldGuard plugin not found");
		else
			System.out.println("WorldGuard plugin found");

		}

		// plugin version
		version = PluginVersion.get(this);
		version.update();
		// config.yml
		config = new webConfig(this);
		// connect to db
		dbKey = config.dbKey();
		// shared settings
		settings = SettingsManager.get(dbKey);
		// language
		lang.load(this, "en");

		// web link
		link = LinkManager.get(dbKey);
		// stand-alone web economy
		link.setEnabled(
			"economy",
			config.getBool(webConfig.PATH_Standalone_WebEconomy_Enabled)
		);
		// stand-alone web inventory
		link.setEnabled(
			"inventory",
			config.getBool(webConfig.PATH_Standalone_WebInventory_Enabled)
		);
		// stand-alone web permissions
		link.setEnabled(
			"perms",
			config.getBool(webConfig.PATH_Standalone_WebPermissions_Enabled)
		);
		// stand-alone web worldguard
		link.setEnabled(
			"worldguard",
			config.getBool(webConfig.PATH_Standalone_WebWorldGuard_Enabled)
		);
		// start updates
		link.start();

		isOk = true;








//		log = xLog.getRoot().get("WebAPI");
//		log.info("Loaded API "+this.getDescription().getVersion());
		try {
			MetricsManager.get(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// prepare to end
	@Override
	public void onDisable() {
		isOk = false;
		// stop schedulers
		try {
			Bukkit.getScheduler().cancelTasks(this);
		} catch (Exception ignore) {}



		isOk = null;
	}


	// get plugins/name/ dir
	public static String getPluginDir() {
		return getPluginDir(get());
	}
	public static String getPluginDir(Plugin plugin) {
		if(plugin == null) return null;
		return plugin.getDataFolder().toString();
	}
	// get plugins/ dir
	public static String getPluginsDir() {
		return getPluginsDir(get());
	}
	public static String getPluginsDir(Plugin plugin) {
		if(plugin == null) return null;
		return plugin.getDataFolder().getParentFile().toString();
	}


	// get objects
	public dbQuery getDB() {
		return dbQuery.get(dbKey);
	}
	public Economy getEconomy() {
		return Plugins3rdParty.get().getEconomy();
	}
	public WorldGuardPlugin getWorldGuard() {
		return Plugins3rdParty.get().getWorldGuard();
	}


}
