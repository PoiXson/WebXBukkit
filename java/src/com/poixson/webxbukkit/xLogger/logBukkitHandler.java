package com.poixson.webxbukkit.xLogger;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLogRecord;
import com.poixson.commonjava.xLogger.handlers.logHandlerConsole;


public class logBukkitHandler extends logHandlerConsole {


	// get bukkit logger
	private final java.util.logging.Logger bukkitLog;
	public logBukkitHandler() {
		bukkitLog = java.util.logging.Logger.getGlobal();
		// set bukkit log level (if needed)
		setLevel(getLevel());
	}
	private java.util.logging.Logger _getBukkitLogger() {
		return bukkitLog;
	}
	// bukkit log handlers
	private java.util.logging.Handler[] _getBukkitHandlers() {
		try {
			return _getBukkitLogger().getParent().getHandlers();
		} catch (Exception ignore) {}
		return null;
	}


	// publish to bukkit
	@Override
	public void publish(xLogRecord record) {
		_getBukkitLogger().log(
			record.getJavaLevel(),
			msgFormat(record)
		);
	}


}
