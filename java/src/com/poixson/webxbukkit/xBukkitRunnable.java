package com.poixson.webxbukkit;

import org.bukkit.scheduler.BukkitRunnable;


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
					System.out.println(Thread.currentThread().getName()+" still running..");
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
			// run
			this.runTask();
			// reset thread name
			thread.setName(savedThreadName);
		}
		active--;
	}


}
