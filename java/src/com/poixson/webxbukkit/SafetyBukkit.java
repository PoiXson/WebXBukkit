package com.poixson.webxbukkit;

import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class SafetyBukkit {


	public static boolean isServerThread() {
		return Bukkit.isPrimaryThread();
	}


	// online players
	public static int getOnlineCount() {
		return Bukkit.getOnlinePlayers().length;
	}
	public static Player[] getOnlinePlayers() throws Exception {
		// run in server thread
		if(!isServerThread()) {
			Callable<Player[]> task = new Callable<Player[]>() {
				@Override
				public Player[] call() throws Exception {
					return getOnlinePlayers();
				}
			};
			return Bukkit.getScheduler().callSyncMethod(WebAPI.get(), task).get();
		}
		// get online players
		return Bukkit.getOnlinePlayers();
	}


}
