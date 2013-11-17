package com.poixson.webxbukkit.webLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.webxbukkit.WebAPI;
import com.poixson.webxbukkit.webLink.handlers.economyHandler;
import com.poixson.webxbukkit.webLink.handlers.inventoryHandler;
import com.poixson.webxbukkit.webLink.handlers.permsHandler;
import com.poixson.webxbukkit.webLink.handlers.worldguardHandler;


public class LinkManager {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// manager instances
	private static final Map<String, LinkManager> instances  = new HashMap<String, LinkManager>();
	private static final Map<String, ActionHandler> handlers = new HashMap<String, ActionHandler>();

	// db connection key
	private final String dbKey;

	// update task
	private static volatile UpdateTask task = null;
	private static volatile Boolean active = false;


	// get manager instance (per db key)
	public static LinkManager get(String dbKey) {
		synchronized(instances) {
			if(instances.isEmpty()) {
				// register default handlers
				register(new economyHandler());
				register(new inventoryHandler());
				register(new permsHandler());
				register(new worldguardHandler());
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
		if(dbKey == null || dbKey.isEmpty()) throw new NullPointerException("dbKey cannot be null");
		this.dbKey = dbKey;
	}


	// register action handler/listener
	public static void register(ActionHandler handler) {
		synchronized(handlers) {
			String name = handler.getHandlerName();
			if(!handlers.containsKey(name)) {
				handlers.put(name, handler);
				Bukkit.getPluginManager().registerEvents(handler, WebAPI.get());
			}
		}
	}


	// start task
	public void start() {
		if(task != null) return;
		synchronized(active) {
			if(task != null) return;
			task = new UpdateTask(this);
			task.runTaskTimerAsynchronously(
				WebAPI.get(),
				xTime.get("6s").getTicks(),
				xTime.get("3s").getTicks()
			);
		}
	}


	// update task
	private class UpdateTask extends BukkitRunnable {

		private final LinkManager manager;
		public UpdateTask(LinkManager manager) {
			super();
			this.manager = manager;
		}

		@Override
		public void run() {
			synchronized(active) {
				if(active) {
					System.out.println("WebLink update task still running..");
					return;
				}
				active = true;
			}
			String threadName = Thread.currentThread().getName();
			Thread.currentThread().setName("WebLinkUpdate");
			manager.triggerUpdates();
			manager.triggerActions();
			Thread.currentThread().setName(threadName);
			active = false;
		}

	}
	// fire event in main thread
	private class ActionTask extends BukkitRunnable {

		private final ActionEvent event;
		public ActionTask(ActionEvent event) {
			this.event = event;
		}

		@Override
		public void run() {
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled())
				System.out.println("Event "+Integer.toString(event.getId())+" was cancelled.");
			else
				System.out.println("Event "+Integer.toString(event.getId())+" was called.");
		}

	}


	// outbound updates to db
	private void triggerUpdates() {
		synchronized(handlers) {
			for(ActionHandler handler : handlers.values())
				handler.doUpdate(dbKey);
		}
	}


	// inbound updates
	private void triggerActions() {
		// query Actions table
		dbQuery db = dbQuery.get(dbKey);
		try {
			// lock table
			db.prepare("LOCK TABLES `"+getTableName()+"` WRITE /* lock actions table */");
			if(!db.exec()) return;


			// query table
			db.prepare("SELECT `action_id`, `player`, `handler`, `action` FROM `"+getTableName()+"` "+
				"WHERE TRUE ORDER BY `priority` DESC LIMIT 100 /* query actions table */");
			if(!db.exec()) return;
			PluginManager pm = Bukkit.getServer().getPluginManager();
			List<Integer> deleteList = new ArrayList<Integer>();
			while(db.hasNext()) {
				String handlerName = db.getString("handler");
				if(handlerName == null || handlerName.isEmpty()) {
					// removal list
					deleteList.add(db.getInt("action_id"));
					db.clean();
					continue;
				}
				// trigger event
				ActionEvent event = new ActionEvent(
					dbKey,
					db.getInt("action_id"),
					db.getString("player"),
					db.getString("handler"),
					db.getString("action")
				);
				// call event from main thread
				(new ActionTask(event))
					.runTask(WebAPI.get());
				pm.callEvent(event);
				// removal list
				deleteList.add(db.getInt("action_id"));
				db.clean();
			}
			// remove completed actions
			int deleteSize = deleteList.size();
			if(deleteSize > 0) {
//				db.prepare("DELETE FROM `"+getTableName()+"` WHERE "+utilsString.repeat(" OR ", "`action_id` = ?", deleteSize)+" LIMIT "+Integer.toString(deleteSize));
//				{
//					int i = 0;
//					for(int id : deleteList) {
//						i++;
//						db.setInt(i, id);
//					}
//				}
//				db.exec();
				System.out.println("Triggered [ "+Integer.toString(deleteSize)+" ] actions.");
			}
			db.clean();



		} finally {
			// unlock table
			db.prepare("UNLOCK TABLES /* unlock actions table */");
			db.exec();
			db.release();
System.exit(0);
		}
	}


	private String getTableName() {
		return dbQuery.san("pxn_Actions");
	}


}
