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

	public static final String PATH_Database_Host   = "Database.Host";
	public static final String PATH_Database_Port   = "Database.Port";
	public static final String PATH_Database_DBase  = "Database.Database";
	public static final String PATH_Database_User   = "Database.User";
	public static final String PATH_Database_Pass   = "Database.Pass";
	public static final String PATH_Database_Prefix = "Database.Prefix";

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
		config.addDefault(PATH_Database_Host,   "localhost");
		config.addDefault(PATH_Database_Port,   3306);
		config.addDefault(PATH_Database_DBase,  "bukkit");
		config.addDefault(PATH_Database_User,   "minecraft");
		config.addDefault(PATH_Database_Pass,   "password123");
		config.addDefault(PATH_Database_Prefix, "pxn_");
	}


	// get dbConfig
	public String dbKey() {
		if(this.dbKey == null || this.dbKey.isEmpty()) {
			this.dbKey = dbConfig.load(
				config.getString(PATH_Database_Host),
				config.getInt   (PATH_Database_Port),
				config.getString(PATH_Database_DBase),
				config.getString(PATH_Database_User),
				config.getString(PATH_Database_Pass)
			).getKey();
			this.dbPrefix = config.getString(PATH_Database_Prefix);
		}
		return this.dbKey;
	}
	// table prefix
	public String dbPrefix() {
		return this.dbPrefix;
	}


	public String getString(String path) {
		return config.getString(path);
	}
	public int getInt(String path) {
		return config.getInt(path);
	}
	public boolean getBool(String path) {
		return config.getBoolean(path);
	}
	public double getDouble(String path) {
		return config.getDouble(path);
	}
	public long getLong(String path) {
		return config.getLong(path);
	}


}
