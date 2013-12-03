package com.poixson.webxbukkit.xLogger;

import java.util.logging.Logger;

import com.poixson.commonjava.xLogger.xLogRecord;
import com.poixson.commonjava.xLogger.handlers.logHandlerConsole;


public class logBukkitHandler extends logHandlerConsole {

	// bukkit logger
	private final Logger bukkitLog;


	public logBukkitHandler() {
		bukkitLog = Logger.getGlobal();
	}


	@Override
	public void publish(xLogRecord record) {
		bukkitLog.log(
			record.getJavaLevel(),
			doFormat(record)
		);
	}


}
