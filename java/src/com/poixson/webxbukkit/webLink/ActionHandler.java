package com.poixson.webxbukkit.webLink;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.poixson.webxbukkit.WebAPI;


public abstract class ActionHandler implements Listener {

	private volatile Boolean enabled = false;


	// handler name
	public abstract String getHandlerName();

	// outbound updates
	public abstract void doUpdate(String dbKey);
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


}
