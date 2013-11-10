package com.poixson.webxbukkit.webLink.handlers;

import com.poixson.webxbukkit.webLink.linkFactory;
import com.poixson.webxbukkit.webLink.linkHandler;


public class inventoryFactory extends linkFactory {


	@Override
	public linkHandler newActionHandler(String dbKey) {
		return new inventoryHandler(getHandlerName(), dbKey);
	}
	@Override
	public String getHandlerName() {
		return "inventory";
	}


}
