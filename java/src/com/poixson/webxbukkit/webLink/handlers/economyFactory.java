package com.poixson.webxbukkit.webLink.handlers;

import com.poixson.webxbukkit.webLink.linkFactory;
import com.poixson.webxbukkit.webLink.linkHandler;


public class economyFactory extends linkFactory {


	@Override
	public linkHandler newActionHandler(String dbKey) {
		return new economyHandler(getHandlerName(), dbKey);
	}
	@Override
	public String getHandlerName() {
		return "economy";
	}


}
