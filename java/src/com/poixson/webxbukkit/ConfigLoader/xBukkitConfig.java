package com.poixson.webxbukkit.ConfigLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.pxdb.dbConfig;


public class xBukkitConfig {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	protected final YamlConfiguration config;
	protected volatile String dbKey = null;
	protected volatile String dbPrefix = null;


	// load plugin config
	public xBukkitConfig(Plugin plugin) {
		if(plugin == null) throw new NullPointerException("plugin cannot be null");
		this.config = (YamlConfiguration) plugin.getConfig();
		this.defaults();
		this.config.options().copyDefaults(true);
		plugin.saveConfig();
	}
	// load custom config
	public xBukkitConfig(String filePath, String fileName) throws IOException {
		File file = new File(
			utilsDirFile.buildFilePath(filePath, fileName, ".yml")
		);
		if(!file.exists() || !file.canRead())
			throw new FileNotFoundException("file not found "+file.toString());
		this.config = YamlConfiguration.loadConfiguration(file);
		this.defaults();
		this.config.options().copyDefaults(true);
		this.config.save(file);
	}
	public xBukkitConfig(YamlConfiguration config) {
		if(config == null) throw new NullPointerException("config cannot be null");
		this.config = config;
		this.config.options().copyDefaults(true);
		this.defaults();
	}


	// default config
	protected void defaults() {}
	// example database config
	protected void defaultDatabase() {
		config.addDefault("Database.Host",     "localhost");
		config.addDefault("Database.Port",     3306);
		config.addDefault("Database.Database", "bukkit");
		config.addDefault("Database.User",     "minecraft");
		config.addDefault("Database.Pass",     "password123");
		config.addDefault("Database.Prefix",   "pxn_");
	}


	// get dbConfig
	public String dbKey() {
		if(this.dbKey == null || this.dbKey.isEmpty()) {
			this.dbKey = dbConfig.get(
				config.getString("Database.Host"),
				config.getInt   ("Database.Port"),
				config.getString("Database.Database"),
				config.getString("Database.User"),
				config.getString("Database.Pass")
			).getKey();
			this.dbPrefix = config.getString("Database.Prefix");
		}
		return this.dbKey;
	}
	// table prefix
	public String dbPrefix() {
		return this.dbPrefix;
	}


}
