package com.poixson.webxbukkit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.commonjava.Utils.xTime;


public class PluginVersion {

	private final String running;
	private volatile String latest  = null;
	private volatile Boolean available = null;

	private static final Map<Plugin, PluginVersion> instances = new HashMap<Plugin, PluginVersion>();
	private final Plugin plugin;


	public static PluginVersion get(final Plugin plugin) {
		synchronized(instances) {
			if(instances.containsKey(plugin))
				return instances.get(plugin);
			PluginVersion version = new PluginVersion(plugin);
			instances.put(plugin, version);
			return version;
		}
	}
	private PluginVersion(final Plugin plugin) {
		this.plugin = plugin;
		running = plugin.getDescription().getVersion();
	}


	public String getRunning() {
		return running;
	}
	public String getLatest() {
		return latest;
	}
	public Boolean newAvailable() {
		return available;
	}


	// check for new version
	private final BukkitRunnable updateTask = new BukkitRunnable() {
		@Override
		public void run() {
			doUpdate();
		}
	};
	// update in 5 seconds
	public void update() {
		updateTask.runTaskLater(plugin, xTime.get("5s").getTicks());
	}
	// update now
	public void doUpdate() {
System.out.println("LOOKING FOR NEW VERSION!!! :-D");
available = true;
	}


}
