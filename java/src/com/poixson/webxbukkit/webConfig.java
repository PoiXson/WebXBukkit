package com.poixson.webxbukkit;

import org.bukkit.plugin.Plugin;

import com.poixson.webxbukkit.ConfigLoader.xBukkitConfig;


public class webConfig extends xBukkitConfig {

	// web economy
	public static final String PATH_Standalone_WebEconomy_Enabled        = "Standalone.WebEconomy.Enabled";
	// web inventory
	public static final String PATH_Standalone_WebInventory_Enabled      = "Standalone.WebInventory.Enabled";
		public static final String PATH_Standalone_WebInventory_AllowCommand = "Standalone.WebInventory.AllowCommand";
		public static final String PATH_Standalone_WebInventory_AllowSign    = "Standalone.WebInventory.AllowSign";
	// web permissions
	public static final String PATH_Standalone_WebPermissions_Enabled    = "Standalone.WebPermissions.Enabled";
	// web worldguard
	public static final String PATH_Standalone_WebWorldGuard_Enabled     = "Standalone.WebWorldGuard.Enabled";


	public webConfig(Plugin plugin) {
		super(plugin);
	}


	@Override
	protected void defaults() {
		// database
		defaultDatabase();
		config.addDefault(xBukkitConfig.PATH_Database_Prefix, "pxn_");
		// web economy
		config.addDefault(PATH_Standalone_WebEconomy_Enabled,        false);
		// web inventory
		config.addDefault(PATH_Standalone_WebInventory_Enabled,      false);
			config.addDefault(PATH_Standalone_WebInventory_AllowCommand, true);
			config.addDefault(PATH_Standalone_WebInventory_AllowSign,    true);
		// web permissions
		config.addDefault(PATH_Standalone_WebPermissions_Enabled,    false);
		// web worldguard
		config.addDefault(PATH_Standalone_WebWorldGuard_Enabled,     false);
	}


}
