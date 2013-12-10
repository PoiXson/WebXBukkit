package com.poixson.webxbukkit.webSettings;

import java.sql.SQLException;
import java.util.HashMap;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.commonjava.xLogger.xLog;


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
				log().severe("Failed to find db connection.");
				return;
			}
			db.prepare("SELECT `setting_id`, `name`, `value` FROM `"+getTableName()+"`");
			db.exec();
			settings.clear();
			while(db.next()) {
				try {
					String name  = db.getStr("name");
					String value = db.getStr("value");
					if(name == null || name.isEmpty()) continue;
					settings.put(
						name,
						value
					);
				} catch (SQLException e) {
					log().trace(e);
				}
			}
			db.release();
		}
	}


	private String getTableName() {
		return dbQuery.san("pxn_Settings");
	}


	// logger
	private volatile xLog _log = null;
	private final Object logLock = new Object();
	public xLog log() {
		if(_log == null) {
			synchronized(logLock) {
				if(_log == null)
					_log = xVars.log();
			}
		}
		return _log;
	}
	public void setLog(xLog log) {
		synchronized(logLock) {
			_log = log;
		}
	}


}
