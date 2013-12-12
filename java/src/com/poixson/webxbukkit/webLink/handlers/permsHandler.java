package com.poixson.webxbukkit.webLink.handlers;

import org.bukkit.event.EventHandler;

import com.poixson.webxbukkit.webLink.ActionEvent;
import com.poixson.webxbukkit.webLink.ActionHandler;


public class permsHandler extends ActionHandler {
	private static final String HANDLER_NAME = "perms";


	public permsHandler(String dbKey) {
		super(dbKey);
	}
	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}


	// update db cache
	@Override
	public void doUpdate() {
	}
	// intermittent cleanup
	@Override
	public void onCleanup() {
	}


	// perform action
	@Override
	@EventHandler
	public void onAction(ActionEvent event) {
		if(!event.isHandler(getHandlerName())) return;
		if(event.isCancelled()) return;
		if(!event.complete()) return;
		// process permissions action
//System.out.println("ACTION EVENT: "+event.getActionName());
	}


}
