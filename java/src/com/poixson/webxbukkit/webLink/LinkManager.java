package com.poixson.webxbukkit.webLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.commonjava.Utils.utilsString;
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
	private static final Map<String, LinkManager> managers  = new HashMap<String, LinkManager>();
	// handler instances
	private final Map<String, ActionHandler> handlers = new HashMap<String, ActionHandler>();

	// db connection key
	private final String dbKey;

	// update task
	private static volatile UpdateTask task = null;
	private static volatile Boolean active = false;


	// get manager instance (per db key)
	public static LinkManager get(String dbKey) {
		synchronized(managers) {
			// use existing manager
			if(managers.containsKey(dbKey))
				return managers.get(dbKey);
			// new manager instance
			LinkManager manager = new LinkManager(dbKey);
			managers.put(dbKey, manager);
			return manager;
		}
	}
	// new manager instance
	private LinkManager(String dbKey) {
		if(dbKey == null || dbKey.isEmpty()) throw new NullPointerException("dbKey cannot be null");
		this.dbKey = dbKey;
		// register default handlers
		register(new economyHandler   (dbKey));
		register(new inventoryHandler (dbKey));
		register(new permsHandler     (dbKey));
		register(new worldguardHandler(dbKey));
	}


	// register action handler/listener
	public void register(ActionHandler handler) {
		synchronized(handlers) {
			String name = handler.getHandlerName();
			if(!handlers.containsKey(name))
				handlers.put(name, handler);
		}
	}
	// get handler
	public ActionHandler getHandler(String handlerName) {
		synchronized(handlers) {
			if(handlers.containsKey(handlerName))
				return handlers.get(handlerName);
		}
		return null;
	}


	// handler enabled
	public void setEnabled(String handlerName, boolean enabled) {
		ActionHandler handler = getHandler(handlerName);
		if(handler == null) {
			System.out.println("Unknown web action handler: "+handlerName);
			return;
		}
		handler.setEnabled(enabled);
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
				if(handler.isEnabled())
					handler.doUpdate();
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
				"WHERE TRUE ORDER BY `priority` DESC, `action_id` ASC LIMIT 100 /* query actions table */");
			if(!db.exec()) return;
			List<Integer> deleteList = new ArrayList<Integer>();

			// iterate actions
			while(db.next()) {
				String handlerName = db.getString("handler");
				if(handlerName == null || handlerName.isEmpty()) {
					// removal list
					deleteList.add(db.getInt("action_id"));
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
				{
					// call event from main thread
					ActionTask task = new ActionTask(event);
					task.runTask(WebAPI.get());
				}
				// removal list
				deleteList.add(db.getInt("action_id"));
			}
			db.clean();

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
					deleteList.clear();
				}
				db.exec();
				System.out.println("Triggered [ "+Integer.toString(deleteSize)+" ] actions.");
			}
			db.clean();

		} finally {
			// unlock table
			db.prepare("UNLOCK TABLES /* unlock actions table */");
			db.exec();
			db.release();
		}
	}


	private String getTableName() {
		return dbQuery.san("pxn_Actions");
	}


}
