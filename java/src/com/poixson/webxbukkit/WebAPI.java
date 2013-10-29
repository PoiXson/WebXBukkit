package com.poixson.webxbukkit;

import java.io.IOException;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsManager;


public class WebAPI extends JavaPlugin {

	private static volatile WebAPI instance = null;
//	private static volatile xLog log = null;


	// get api instance
	public static synchronized WebAPI get() {
		if(instance == null) throw new RuntimeException("WebAPI is not enabled! Plugin instance cannot be obtained!");
		return instance;
	}


	// enable api plugin
	@Override
	public void onEnable() {
		if(instance != null) throw new RuntimeException("WebAPI plugin already enabled?!");
		instance = this;
		this.isEnabled();
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
