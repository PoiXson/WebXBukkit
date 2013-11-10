package com.poixson.webxbukkit.webLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.webxbukkit.WebAPI;
import com.poixson.webxbukkit.webLink.handlers.economyFactory;
import com.poixson.webxbukkit.webLink.handlers.inventoryFactory;
import com.poixson.webxbukkit.webLink.handlers.permissionsFactory;


public class LinkManager {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// manager instances
	private static final HashMap<String, LinkManager> instances = new HashMap<String, LinkManager>();
	private static final HashMap<String, linkFactory> factories = new HashMap<String, linkFactory>();
	private final HashMap<String, linkHandler> handlers = new HashMap<String, linkHandler>();
	// db connection key
	private final String dbKey;

	// update task
	private static volatile BukkitRunnable task = null;
	private static volatile Boolean running = false;


	// get manager instance (per db key)
	public static LinkManager get(String dbKey) {
		synchronized(instances) {
			// register default factories
			if(instances.isEmpty()) {
				new economyFactory();
				new inventoryFactory();
				new permissionsFactory();
			}
			// use existing manager
			if(instances.containsKey(dbKey))
				return instances.get(dbKey);
			// new manager instance
			LinkManager manager = new LinkManager(dbKey);
			instances.put(dbKey, manager);
			return manager;
		}
	}
	// new manager instance
	private LinkManager(String dbKey) {
		this.dbKey = dbKey;
	}


	// get action handler
	public linkHandler getHandler(String handlerName) {
		synchronized(handlers) {
			// action already exists
			if(handlers.containsKey(handlerName))
				return handlers.get(handlerName);
			synchronized(factories) {
				// new action from factory
				if(factories.containsKey(handlerName)) {
					linkHandler action = factories.get(handlerName).newActionHandler(dbKey);
					handlers.put(handlerName, action);
					System.out.println("Registered new action handler: "+handlerName+" dbKey: "+dbKey);
					return action;
				}
			}
		}
		// action or factory not found
		return null;
	}


	// register handler factory
	public static void register(linkFactory factory) {
		if(factory == null) throw new NullPointerException("factory cannot be null");
		synchronized(factories) {
			String name = factory.getHandlerName();
			if(factories.containsKey(name)) {
				linkFactory existing = factories.get(name);
				if(factory != existing) throw new IllegalArgumentException("Handler factory already registered for: "+name);
				// object already registered, nothing to do
				return;
			}
			// register new factory
			factories.put(name, factory);
			System.out.println("Registered new handler factory: "+name);
		}
	}


	public static void start() {
		synchronized(instances) {
			if(task == null) {
				task = new BukkitRunnable() {
					@Override
					public void run() {
						runUpdates();
					}
				};
				task.runTaskTimerAsynchronously(
					WebAPI.get(),
					xTime.get("5s").getTicks(),
					xTime.get("3s").getTicks()
				);
			}
		}
	}
	private static void runUpdates() {
		if(running) return;
		synchronized(running) {
			if(running) return;
			running = true;
		}
		synchronized(instances) {
			for(LinkManager manager : instances.values()) {
				dbQuery db = dbQuery.get(manager.dbKey);
				if(db == null) {
					System.out.println("Failed to find db connection.");
					continue;
				}
				try {
					// query Actions table (inbound updates)
					manager.triggerActions(db);
					// update cached data (outbound updates)
					manager.triggerUpdates();
				} finally {
					db.release();
				}
			}
		}
		running = false;
	}


	// query Actions table (inbound updates)
	private void triggerActions(dbQuery db) {
		// lock table
		db.prepare("LOCK TABLES `"+getTableName()+"` WRITE /* lock actions table */");
		if(!db.exec()) return;
		// query table
		db.prepare("SELECT `action_id`, `player`, `handler`, `action` FROM `"+getTableName()+"` WHERE TRUE ORDER BY `priority` DESC LIMIT 100 /* query actions table */");
		if(!db.exec()) return;
		if(db.getResultInt() == 0) return;
		List<Integer> deleteList = new ArrayList<Integer>();
		synchronized(handlers) {
			while(db.hasNext()) {
				String handlerName = db.getString("handler");
				if(handlerName != null && !handlerName.isEmpty()) {
					linkHandler handler = getHandler(handlerName);
					if(handler == null) {
						System.out.println("Handler not found for action: ["+db.getString("handler")+"] "+db.getString("action"));
					} else {
						// send action to handler
						new actionRunnable(
							handler,
							db.getString("player"),
							db.getString("action")
						).runTask(WebAPI.get());
					}
				}
				deleteList.add(db.getInt("action_id"));
				db.clean();
			}
		}
		// remove completed actions
		int deleteSize = deleteList.size();
		if(deleteSize > 0) {
			db.prepare("DELETE FROM `"+getTableName()+"` WHERE "+utilsString.repeat(" OR ", "`action_id` = ?", deleteSize)+" LIMIT "+Integer.toString(deleteSize));
			{
				int i = 0;
				for(int id : deleteList) {
					i++;
					db.setInt(i, id);
				}
			}
			db.exec();
			System.out.println("Triggered [ "+Integer.toString(deleteSize)+" ] actions.");
		}
		// unlock table
		db.prepare("UNLOCK TABLES /* unlock actions table */");
		db.exec();
		db.clean();
	}
	// update cached data (outbound updates)
	private void triggerUpdates() {
		synchronized(handlers) {
			for(linkHandler handler : handlers.values())
				handler.doUpdate();
		}
	}


	private String getTableName() {
		return dbQuery.san("pxn_Actions");
	}


}
