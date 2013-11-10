package com.poixson.webxbukkit.webLink;

import org.bukkit.scheduler.BukkitRunnable;


public class actionRunnable extends BukkitRunnable {

	private final linkHandler handler;
	private final String playerName;
	private final String action;


	public actionRunnable(linkHandler handler, String playerName, String action) {
		this.handler = handler;
		this.playerName = playerName;
		this.action = action;
	}


	@Override
	public void run() {
		handler.doAction(
			playerName,
			action
		);
	}


}
