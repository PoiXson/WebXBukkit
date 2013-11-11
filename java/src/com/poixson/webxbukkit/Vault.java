package com.poixson.webxbukkit;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;


public class Vault {

	private static volatile Vault instance = null;
	private static final Object lock = new Object();

	private static volatile Economy econ = null;


	public static void Init() {
		if(instance != null)
			return;
		synchronized(lock) {
			if(instance == null)
				instance = new Vault();
		}
	}
	private Vault() {
		if(Bukkit.getPluginManager().getPlugin("Vault") == null)
			return;
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		if(rsp == null)
			return;
		econ = rsp.getProvider();
	}


	public static Economy getEconomy() {
		return econ;
	}


}
