package com.poixson.webxbukkit;

import java.io.IOException;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsManager;

import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.pxdb.dbConfig;
import com.poixson.commonjava.pxdb.dbManager;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.webxbukkit.webLink.LinkManager;
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
	private final webLanguage lang = new webLanguage();

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

		// standalone web economy
		if(config.getBool(webConfig.PATH_Standalone_WebEconomy_Enabled)) {
			LinkManager.get(dbKey);
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


	// get plugins/ dir
	public static String getPluginsDir() {
		return getPluginsDir(get());
	}
	public static String getPluginsDir(Plugin plugin) {
		return plugin.getDataFolder().getParentFile().toString();
	}


}
