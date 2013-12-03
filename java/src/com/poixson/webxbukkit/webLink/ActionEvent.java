package com.poixson.webxbukkit.webLink;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.poixson.commonjava.Utils.StringParser;
import com.poixson.commonjava.pxdb.dbQuery;


public class ActionEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private volatile boolean cancelled = false;
	private volatile Boolean completed = null;

	private final String dbKey;
	private final int    id;
	private final String player;
	private final String handlerName;
	private final String actionName;


	public ActionEvent(String dbKey, int id, String player, String handlerName, String actionName) {
		this.dbKey       = dbKey;
		this.id          = id;
		this.player      = player;
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


	// getters
	public String dbKey() {
		return this.dbKey;
	}
	public dbQuery getDB() {
		return dbQuery.get(dbKey());
	}
	public int getId() {
		return this.id;
	}
	public String getPlayerName() {
		return player;
	}
	public Player getPlayer() {
		return Bukkit.getPlayer(getPlayerName());
	}
	public String getHandlerName() {
		return handlerName;
	}
	public String getActionStr() {
		return actionName;
	}
	public StringParser getActionParser() {
		return new StringParser(" ", getActionStr());
	}


	// player equals
	public boolean isPlayer(String playerName) {
		if(playerName == null || playerName.isEmpty()) return false;
		return playerName.equalsIgnoreCase(this.player);
	}
	public boolean isPlayer(Player player) {
		if(player == null) return false;
		return isPlayer(player.getName());
	}
	// handler equals
	public boolean isHandler(String str) {
		if(str == null || str.isEmpty()) return false;
		return str.equalsIgnoreCase(handlerName);
	}


	// event cancelled
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}


	// remove completed action from db
	public boolean complete() {
		if(completed == null)
			completed = LinkManager.removeAction(dbKey, id);
		return completed;
	}


}
