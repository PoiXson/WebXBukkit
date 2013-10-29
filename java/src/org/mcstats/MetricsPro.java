package org.mcstats;

import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;


public class MetricsPro extends Metrics {

	//protected static final String DEFAULT_BASE_URL = "http://metrics.poixson.com";

	private volatile String baseUrl   = null;
	private volatile String reportUrl = null;

	protected volatile boolean first = true;


	protected MetricsPro(final Plugin plugin) throws IOException {
		super(plugin);
		String tmp = configuration.getString("baseurl", null);
		if(tmp != null && !tmp.isEmpty())
			baseUrl = tmp;
		if(debug) MetricsManager.get().setDebug();
	}


//	@Override
//	protected void LoadConfigDefaults() {
//		super.LoadConfigDefaults();
//		// more defaults
//		configuration.addDefault("baseurl", DEFAULT_BASE_URL);
//		//configuration.addDefault("reporturl", DEFAULT_REPORT_URL);
//	}


	@Override
	public String getBaseUrl() {
		if(baseUrl == null || baseUrl.isEmpty())
			return DEFAULT_BASE_URL;
		return baseUrl;
	}
	@Override
	public String getReportUrl() {
		if(reportUrl == null || reportUrl.isEmpty())
			return DEFAULT_REPORT_URL;
		return baseUrl;
	}


	@Override
	public boolean start() {
		synchronized(optOutLock) {
			// Did we opt out?
			if(isOptOut())
				return false;
			MetricsManager.get().start();
		}
		return true;
	}


	protected void doPostPlugin() {
		if(isOptOut()) {
			// Tell all plotters to stop gathering information.
			if(!graphs.isEmpty())
				for(Graph graph : graphs)
					graph.onOptOut();
			return;
		}
		try {
			postPlugin(!first);
		} catch (IOException e) {
			if(debug)
				Bukkit.getLogger().log(Level.INFO, "[Metrics] "+e.getMessage());
		} finally {
			first = false;
		}
	}


}
