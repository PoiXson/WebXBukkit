package com.poixson.webxbukkit.webLink.handlers;

import com.poixson.webxbukkit.webLink.linkHandler;


public class economyHandler extends linkHandler {


	public economyHandler(String handlerName, String dbKey) {
		super(handlerName, dbKey);
//		System.out.println(dbKey+" New economy web link!");
	}


	@Override
	public void doUpdate() {
	}


	@Override
	public void doAction(String player, String action) {
		if(player == null || player.isEmpty()) return;
		if(action == null || action.isEmpty()) return;
		// deposit action
		if(action.startsWith("deposit"))
			return;
		// withdraw action
		if(action.startsWith("withdraw"))
			return;
		System.out.println("Unknown action: ["+player+"] "+action);
	}


}
