package com.poixson.webxbukkit.webLink;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.poixson.commonjava.xLogger.xLog;
import com.poixson.webxbukkit.WebAPI;


public abstract class ActionHandler implements Listener {

	private volatile Boolean enabled = false;
	private final String dbKey;


	public ActionHandler(String dbKey) {
		if(dbKey == null || dbKey.isEmpty()) throw new NullPointerException("dbKey cannot be null");
		this.dbKey = dbKey;
	}


	// handler name
	public abstract String getHandlerName();

	// outbound updates
	public abstract void doUpdate();
	// inbound updates
	public abstract void onAction(ActionEvent event);
	// intermittent cleanup
	public abstract void onCleanup();


	// enable handler
	public void setEnabled(boolean enabled) {
		synchronized(this.enabled) {
			if(this.enabled == enabled) return;
			this.enabled = enabled;
			if(enabled) {
				// action listener
				Bukkit.getPluginManager().registerEvents(this, WebAPI.get());
				log().stats("Enabled web link updates: "+getHandlerName());
			} else {
				// stop listening
				HandlerList.unregisterAll(this);
				log().stats("Disabled web link updates: "+getHandlerName());
			}
		}
	}
	public void setEnabled() {
		setEnabled(true);
	}
	public boolean isEnabled() {
		return enabled;
	}


	protected String dbKey() {
		return dbKey;
	}


	// logger
	private volatile xLog _log = null;
	private final Object logLock = new Object();
	public xLog log() {
		if(_log == null) {
			synchronized(logLock) {
				if(_log == null)
					_log = LinkManager.get(dbKey).log(getHandlerName());
			}
		}
		return _log;
	}
	public void setLog(xLog log) {
		synchronized(logLock) {
			_log = log;
		}
	}


}
