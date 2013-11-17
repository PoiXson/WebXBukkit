package com.poixson.webxbukkit;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


public class Plugins3rdParty {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private static volatile Plugins3rdParty instance = null;
	private static final Object lock = new Object();

	private final Economy econ;
	private final WorldGuardPlugin wg;


	public static Plugins3rdParty get() {
		if(instance != null)
			return instance;
		synchronized(lock) {
			if(instance == null)
				instance = new Plugins3rdParty();
		}
		return instance;
	}
	private Plugins3rdParty() {
		econ = initVaultEconomy();
		wg   = initWorldGuard();
	}


	// load vault
	private static Economy initVaultEconomy() {
		try {
			PluginManager pm = Bukkit.getPluginManager();
			if(pm.getPlugin("Vault") == null)
				return null;
			RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
			if(rsp == null)
				return null;
			return rsp.getProvider();
		} catch (Exception ignore) {}
		return null;
	}
	// load worldguard
	private static WorldGuardPlugin initWorldGuard() {
		try {
			PluginManager pm = Bukkit.getPluginManager();
			Plugin plugin = pm.getPlugin("WorldGuard");
			if(plugin == null || !(plugin instanceof WorldGuardPlugin))
				return null;
			return (WorldGuardPlugin) plugin;
		} catch (Exception ignore) {}
		return null;
	}


	public Economy getEconomy() {
		return econ;
	}
	public WorldGuardPlugin getWorldGuard() {
//TODO: remove this
if(wg == null)System.out.println("WG is null");
		return wg;
	}


}
