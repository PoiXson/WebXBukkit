package com.poixson.webxbukkit.webSettings;

import java.util.HashMap;

import com.poixson.commonjava.pxdb.dbQuery;


public final class SettingsManager {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


	// db connection key
	private final String dbKey;

	// cached settings
	protected final HashMap<String, String> settings = new HashMap<String, String>();


	// settings manager
	private static final HashMap<String, SettingsManager> instances =
		new HashMap<String, SettingsManager>();
	public static SettingsManager get(String dbKey) {
		synchronized(instances) {
			// use existing instance
			if(instances.containsKey(dbKey))
				return instances.get(dbKey);
			// new instance
			SettingsManager settings = new SettingsManager(dbKey);
			instances.put(dbKey, settings);
			return settings;
		}
	}
	// new instance
	private SettingsManager(String dbKey) {
		this.dbKey = dbKey;
	}


	public void Update() {
		synchronized(settings) {
			// db connection
			dbQuery db = dbQuery.get(dbKey);
			if(db == null) {
				System.out.println("Failed to find db connection.");
				return;
			}
			db.prepare("SELECT `setting_id`, `name`, `value` FROM `"+getTableName()+"`");
			db.exec();
			settings.clear();
			while(db.next()) {
				String name = db.getString("name");
				String value = db.getString("value");
				if(name == null || name.isEmpty() || value == null || value.isEmpty()) continue;
				settings.put(
					name,
					value
				);
			}
			db.release();
		}
	}


	public String getTableName() {
		return dbQuery.san("pxn_Settings");
	}


}
