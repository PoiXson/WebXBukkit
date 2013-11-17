package com.poixson.webxbukkit.webLink;

import org.bukkit.event.Listener;


public interface ActionHandler extends Listener {

	// handler name
	public String getHandlerName();

	// outbound updates
	public void doUpdate(String dbKey);
	// inbound updates
	public void onAction(ActionEvent event);

}
