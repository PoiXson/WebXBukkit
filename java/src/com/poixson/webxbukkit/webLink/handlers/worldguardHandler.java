package com.poixson.webxbukkit.webLink.handlers;

import org.bukkit.event.EventHandler;

import com.poixson.webxbukkit.webLink.ActionEvent;
import com.poixson.webxbukkit.webLink.ActionHandler;


public class worldguardHandler extends ActionHandler {
	private static final String HANDLER_NAME = "worldguard";


	public worldguardHandler(String dbKey) {
		super(dbKey);
	}
	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}


	@Override
	public void doUpdate() {
	}


	@Override
	@EventHandler
	public void onAction(ActionEvent event) {
		if(event.isCancelled()) return;
		if(!event.isHandler(HANDLER_NAME)) return;
		// process worldguard action
//System.out.println("ACTION EVENT: "+event.getActionName());
	}


}
