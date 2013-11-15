package com.poixson.webxbukkit.webLink;


public abstract class linkHandler {

	private final String handlerName;
	protected final String dbKey;


	public linkHandler(String handlerName, String dbKey) {
		if(handlerName == null || handlerName.isEmpty()) throw new NullPointerException("handlerName cannot be null");
		if(dbKey       == null || dbKey.isEmpty()      ) throw new NullPointerException("dbKey cannot be null");
		this.handlerName = handlerName;
		this.dbKey = dbKey;
	}
	// handler name
	public String getHandlerName() {
		return handlerName;
	}


	// outbound updates
	public abstract void doUpdate();
	// inbound updates
	public abstract void doAction(String player, String action);


}
