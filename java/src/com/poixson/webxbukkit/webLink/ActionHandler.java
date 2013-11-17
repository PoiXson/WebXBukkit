package com.poixson.webxbukkit.webLink;

import org.bukkit.event.Listener;

public abstract class ActionHandler implements Listener {


	// handler name
	public abstract String getHandlerName();

	// outbound updates
	public abstract void doUpdate(String dbKey);
	// inbound updates
	public abstract void onAction(ActionEvent event);


}
