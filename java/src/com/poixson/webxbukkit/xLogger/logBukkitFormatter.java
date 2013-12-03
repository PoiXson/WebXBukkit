package com.poixson.webxbukkit.xLogger;

import com.poixson.commonjava.xLogger.xLogRecord;
import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter;


public class logBukkitFormatter extends defaultLogFormatter {


	public String formatMsg(xLogRecord record) {
		String crumbs = partCrumbs(record);
		if(crumbs == null || crumbs.isEmpty())
			return partMessage(record);
		return partCrumbs(record)+" "+partMessage(record);
	}


	@Override
	protected String partTimestamp(xLogRecord record) {
		return null;
	}
	@Override
	protected String partLevel(xLogRecord record) {
		return null;
	}


}
