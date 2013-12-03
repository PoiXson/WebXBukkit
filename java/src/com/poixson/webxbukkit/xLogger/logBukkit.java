package com.poixson.webxbukkit.xLogger;

import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.xLogHandler;


public class logBukkit extends xLog {


	// bukkit console logger abstraction
	protected logBukkit(String name, xLog parent) {
		super(name, parent);
	}
	@Override
	protected xLog newInstance(String name) {
		return new logBukkit(name, this);
	}


	// init logger
	public static void init() {
		if(root != null) return;
		synchronized(lock) {
			if(root != null) return;
			root = new logBukkit(null, null);
			initDefaultHandlers();
		}
	}
	// bukkit log handler
	protected static void initDefaultHandlers() {
		// bukkit console
		xLogHandler console = new logBukkitHandler();
		console.setFormatter(
			new logBukkitFormatter()
		);
		root.addHandler(console);
	}


}
