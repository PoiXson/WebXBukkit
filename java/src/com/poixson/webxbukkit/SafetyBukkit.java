package com.poixson.webxbukkit;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.webxbukkit.webLink.ActionEvent;


public class SafetyBukkit {


	public static boolean isServerThread() {
		return Bukkit.isPrimaryThread();
	}


	// online players
	public static int getOnlineCount() {
		return Bukkit.getOnlinePlayers().length;
	}
	public static Player[] getOnlinePlayers() {
		// run in server thread
		if(isServerThread()) {
			// get online players
			return Bukkit.getOnlinePlayers();
		} else {
			Callable<Player[]> task = new Callable<Player[]>() {
				@Override
				public Player[] call() throws Exception {
					// get online players
					return getOnlinePlayers();
				}
			};
			// call in main thread
			try {
				return Bukkit.getScheduler().callSyncMethod(WebAPI.get(), task).get();
			} catch (InterruptedException | ExecutionException ignore) {
				return null;
			}
		}
	}


	// trigger event
	public static void callEvent(Event event) {
		if(isServerThread()) {
			doEvent(event);
		} else {
			(new EventRunnable(event))
				.runTask(WebAPI.get());
		}
	}
	private static class EventRunnable extends BukkitRunnable {
		private final Event event;
		public EventRunnable(Event event) {
			this.event = event;
		}
		@Override
		public void run() {
			doEvent(event);
		}
	}
	private static void doEvent(Event event) {
		Bukkit.getPluginManager().callEvent(event);
		if(event instanceof Cancellable) {
			if( ((Cancellable)event).isCancelled() )
				log().fine("Event "+Integer.toString( ((ActionEvent)event).getId() )+" was cancelled.");
			else
				log().fine("Event "+Integer.toString( ((ActionEvent)event).getId() )+" was called.");
		}
	}


	// open inventory
	public static void openInventory(Player player, Inventory chest) {
		if(isServerThread()) {
			doOpenInventory(player, chest);
		} else {
			(new InventoryOpener(player, chest))
				.runTask(WebAPI.get());
		}
	}
	private static class InventoryOpener extends BukkitRunnable {
		private final Player player;
		private final Inventory chest;
		public InventoryOpener(Player player, Inventory chest) {
			this.player = player;
			this.chest = chest;
		}
		@Override
		public void run() {
			doOpenInventory(player, chest);
		}
	}
	private static void doOpenInventory(Player player, Inventory chest) {
		synchronized(chest) {
			player.openInventory(chest);
		}
	}


	// logger
	public static xLog log() {
		return xVars.log();
	}


}
