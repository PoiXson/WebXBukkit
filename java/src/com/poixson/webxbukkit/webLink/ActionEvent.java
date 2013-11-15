package com.poixson.webxbukkit.webLink;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.poixson.commonjava.pxdb.dbQuery;


public class ActionEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private volatile boolean cancelled = false;

	private final String dbKey;
	private final String player;
	private final String handlerName;
	private final String actionName;


	public ActionEvent(String dbKey, String player, String handlerName, String actionName) {
		this.dbKey  = dbKey;
		this.player = player;
		this.handlerName = handlerName;
		this.actionName  = actionName;
	}


	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() {
		return handlers;
	}


	public String dbKey() {
		return this.dbKey;
	}
	public dbQuery getDB() {
		return dbQuery.get(dbKey());
	}
	public String getPlayer() {
		return player;
	}
	public String getHandlerName() {
		return handlerName;
	}
	public String getActionName() {
		return actionName;
	}


	public boolean equalsHandler(String str) {
		if(str == null || str.isEmpty())
			return false;
		return (str.equalsIgnoreCase(handlerName));
	}


	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}


}
