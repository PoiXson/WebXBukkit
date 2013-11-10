package com.poixson.webxbukkit.webLink;


public abstract class linkFactory {


	public linkFactory() {
		// register factory
		LinkManager.register(this);
	}


	// get new action handler
	public abstract linkHandler newActionHandler(String dbKey);
	// handler name
	public abstract String getHandlerName();


}
