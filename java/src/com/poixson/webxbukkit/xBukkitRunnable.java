package com.poixson.webxbukkit;

import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLog;


public abstract class xBukkitRunnable extends BukkitRunnable {

	private final String threadName;
	private final boolean concurrent;
	private final Object lock = new Object();
	private volatile int active = 0;


	public abstract void runTask();


	public xBukkitRunnable() {
		this(null);
	}
	public xBukkitRunnable(String threadName) {
		this(threadName, false);
	}
	public xBukkitRunnable(String threadName, boolean concurrent) {
		if(threadName == null || threadName.isEmpty())
			this.threadName = null;
		else
			this.threadName = threadName;
		this.concurrent = concurrent;
	}


	@Override
	public void run() {
		// single/multi-thread
		if(!concurrent) {
			synchronized(lock) {
				if(active > 0) {
					log().warning(Thread.currentThread().getName()+" still running..");
					return;
				}
			}
		}
		active++;
		if(this.threadName == null) {
			// run
			this.runTask();
		} else {
			// thread name
			final Thread thread = Thread.currentThread();
			final String savedThreadName = thread.getName();
			thread.setName(this.threadName);
			try {
				// run
				this.runTask();
			} catch (Exception e) {
				log().trace(e);
			}
			// reset thread name
			thread.setName(savedThreadName);
		}
		active--;
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
