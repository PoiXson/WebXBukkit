package com.poixson.webxbukkit.webLink;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

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


	// enable handler
	public void setEnabled(boolean enabled) {
		synchronized(this.enabled) {
			if(this.enabled == enabled) return;
			this.enabled = enabled;
			if(enabled) {
				// action listener
				Bukkit.getPluginManager().registerEvents(this, WebAPI.get());
				System.out.println("Enabled web link updates: "+getHandlerName());
			} else {
				// stop listening
				HandlerList.unregisterAll(this);
				System.out.println("Disabled web link updates: "+getHandlerName());
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


}
