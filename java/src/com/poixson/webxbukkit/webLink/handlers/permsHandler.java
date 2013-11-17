package com.poixson.webxbukkit.webLink.handlers;

import org.bukkit.event.EventHandler;

import com.poixson.webxbukkit.webLink.ActionEvent;
import com.poixson.webxbukkit.webLink.ActionHandler;


public class permsHandler extends ActionHandler {

	public static final String HANDLER_NAME = "perms";


	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}


	@Override
	public void doUpdate(String dbKey) {
	}


	@Override
	@EventHandler
	public void onAction(ActionEvent event) {
		if(event.isCancelled()) return;
		if(!event.isHandler(HANDLER_NAME)) return;
		// process permissions action
//System.out.println("ACTION EVENT: "+event.getActionName());
	}


}
