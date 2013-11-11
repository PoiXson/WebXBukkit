package com.poixson.webxbukkit;

import java.io.IOException;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsManager;

import com.poixson.commonjava.pxdb.dbConfig;
import com.poixson.commonjava.pxdb.dbManager;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.webxbukkit.webLink.LinkManager;
import com.poixson.webxbukkit.webLink.handlers.economyHandler;
import com.poixson.webxbukkit.webLink.handlers.inventoryHandler;
import com.poixson.webxbukkit.webLink.handlers.permissionsHandler;
import com.poixson.webxbukkit.webSettings.SettingsManager;


public class WebAPI extends JavaPlugin {

	private static volatile WebAPI instance = null;
	private static final Object lock = new Object();
//	private static volatile xLog log = null;

	// objects
	private volatile webConfig config = null;
@SuppressWarnings("unused")
	private volatile SettingsManager settings = null;
	private volatile PluginVersion version = null;
@SuppressWarnings("unused")
	private final webLanguage lang = new webLanguage();
	// web link
	private volatile LinkManager link = null;

	// database key
	private volatile String dbKey = null;

	// run state
@SuppressWarnings("unused")
	private static volatile boolean debug = false;


	// get api instance
	public static WebAPI get() {
		synchronized(lock) {
			if(instance == null) throw new RuntimeException("WebAPI is not enabled! Plugin instance cannot be obtained!");
			return instance;
		}
	}


	// enable api plugin
	@Override
	public void onEnable() {
		synchronized(lock) {
			if(instance != null) throw new RuntimeException("WebAPI plugin already enabled?!");
			instance = this;
		}
		// load vault (required)
		Vault.Init();
		if(Vault.getEconomy() == null) {
			System.out.println("Economy plugin not found!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
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
		LinkManager.start();
		// stand-alone web economy
		if(config.getBool(webConfig.PATH_Standalone_WebEconomy_Enabled)) {
			@SuppressWarnings("unused")
			economyHandler economy = (economyHandler) link.getHandler("economy");
			System.out.println("Enabled web link: economy");
		}
		// stand-alone web inventory
		if(config.getBool(webConfig.PATH_Standalone_WebInventory_Enabled)) {
			@SuppressWarnings("unused")
			inventoryHandler inventory = (inventoryHandler) link.getHandler("inventory");
			System.out.println("Enabled web link: inventory");
		}
		// stand-alone web permissions
		if(config.getBool(webConfig.PATH_Standalone_WebPermissions_Enabled)) {
			@SuppressWarnings("unused")
			permissionsHandler permissions = (permissionsHandler) link.getHandler("permissions");
			System.out.println("Enabled web link: permissions");
		}




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
	}


	public boolean isDebug() {
		return true;
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


	public dbQuery getDB() {
		return dbQuery.get(dbKey);
	}


	public Economy getEconomy() {
		return Vault.getEconomy();
	}


}
