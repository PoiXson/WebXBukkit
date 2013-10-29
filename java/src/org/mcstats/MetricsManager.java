package org.mcstats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.commonjava.Utils.xTime;
import com.poixson.webxbukkit.WebAPI;


public class MetricsManager {

	private static volatile MetricsManager manager = null;
	private static final Map<Plugin, MetricsPro> instances = new HashMap<Plugin, MetricsPro>();

	// delay between simultaneous reports
	private static final long SLEEP_MS          = xTime.get("250n").getMS();
	private static final long FIRST_DELAY_TICKS = xTime.get("30s" ).getTicks();
	private static final long INTERVAL_TICKS    = xTime.get("15m" ).getTicks();
	// run state
	private static volatile Boolean running = false;
	private static volatile Boolean active = false;

	private static volatile boolean debug = false;


	private final BukkitRunnable task = new BukkitRunnable() {
		@Override
		public void run() {
			MetricsManager.run();
		}
	};


	// get manager
	public static synchronized MetricsManager get() {
		if(manager == null)
			manager = new MetricsManager();
		return manager;
	}
	// get metrics
	public static MetricsPro get(final Plugin plugin) throws IOException {
		return get().getMetrics(plugin);
	}
	public MetricsPro getMetrics(final Plugin plugin) throws IOException {
		synchronized(instances) {
			if(instances.containsKey(plugin))
				return instances.get(plugin);
			MetricsPro metrics = new MetricsPro(plugin);
			instances.put(plugin, metrics);
			return metrics;
		}
	}
	private MetricsManager() {
	}


	public void start() {
		if(running) return;
		synchronized(running) {
			if(running) return;
			task.runTaskTimerAsynchronously(WebAPI.get(), FIRST_DELAY_TICKS, INTERVAL_TICKS);
		}
	}
	public void stop() {
		running = false;
		if(task != null) {
			try {
				task.cancel();
			} catch (Exception ignore) {}
		}
	}


	protected static void run() {
		if(active) {
			if(debug)
				Bukkit.getLogger().log(Level.INFO, "[Metrics] Task already running?");
			return;
		}
		if(instances.isEmpty()) {
			get().stop();
			return;
		}
		synchronized(active) {
			if(active) {
				if(debug)
					Bukkit.getLogger().log(Level.INFO, "[Metrics] Task already running?");
				return;
			}
			active = true;
		}
		for(MetricsPro metrics : instances.values()) {
			try {
				metrics.doPostPlugin();
				// short sleep between reports
				try {
					if(!running) break;
					Thread.sleep(SLEEP_MS);
					if(!running) break;
				} catch (InterruptedException ignore) {
					break;
				}
			} catch (Exception e) {
				if(debug)
					Bukkit.getLogger().log(Level.INFO, "[Metrics] "+e.getMessage());
			}
		}
		active = false;
	}


	protected void setDebug() {
		debug = true;
	}


}
