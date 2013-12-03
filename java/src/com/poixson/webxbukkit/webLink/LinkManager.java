package com.poixson.webxbukkit.webLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsMath;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.webxbukkit.SafetyBukkit;
import com.poixson.webxbukkit.WebAPI;
import com.poixson.webxbukkit.xBukkitRunnable;
import com.poixson.webxbukkit.webLink.handlers.economyHandler;
import com.poixson.webxbukkit.webLink.handlers.inventoryHandler;
import com.poixson.webxbukkit.webLink.handlers.permsHandler;
import com.poixson.webxbukkit.webLink.handlers.worldguardHandler;


public class LinkManager {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// actions table
	private static final String TABLE_ACTIONS = "pxn_Actions";

	// manager instances
	private static final Map<String, LinkManager> managers  = new HashMap<String, LinkManager>();
	// handler instances
	private final Map<String, ActionHandler> handlers = new HashMap<String, ActionHandler>();
	// db connection key
	private final String dbKey;

	// update interval
	private static final xTime intervalShort = xTime.get("0.5s");
	private static final xTime intervalIdle  = xTime.get("5s");
	private static final CoolDown idle = CoolDown.get(intervalIdle);
	private static final int ActionsPerCycle = 5;
	private static volatile boolean hasMore = false;
	private volatile int lastId = 0;

	// update task
	private static final xBukkitRunnable task = new xBukkitRunnable("WebLinkUpdate", false) {
		@Override
		public void runTask() {
			updateManagers();
		}
	};
	private static volatile Boolean running = false;
	private static volatile boolean stopping = false;


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
		// register default handlers (disabled by default)
		register(new economyHandler   (dbKey));
		register(new inventoryHandler (dbKey));
		register(new permsHandler     (dbKey));
		register(new worldguardHandler(dbKey));
		// start task
		start();
	}


	// action/update task
	private static void start() {
		if(running == null) running = false;
		if(running == true) return;
		synchronized(running) {
			if(running) return;
			stopping = false;
			task.runTaskTimerAsynchronously(
				WebAPI.get(),
				utilsMath.MinMax(
					intervalShort.getTicks() * 2,
					20 * 3,
					20 * 15
				),
				intervalShort.getTicks()
			);
		}
	}
	public static void shutdown() {
		stopping = true;
		try {
			task.cancel();
		} catch (Exception ignore) {}
	}


	// updates task
	private static void updateManagers() {
		if(stopping) return;
		if(!hasMore && !idle.runAgain()) return;
		hasMore = false;
		synchronized(managers) {
			for(LinkManager manager : managers.values()) {
				try {
					manager.doUpdates();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if(hasMore) {
			idle.reset();
			idle.runAgain();
		}
	}
	private void doUpdates() {
		if(stopping) return;
		dbQuery db = dbQuery.get(dbKey);
		if(stopping) return;
		try {
			// query actions (inbound)
			execActions(db);
			if(stopping) return;
			// update db cache (outbound)
			execUpdates();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.release();
		}
	}
	// query actions (inbound)
	private void execActions(dbQuery db) {
		if(stopping) return;
		if(db == null) return;
		// query actions
		ActionEvent[] actions = queryActions(db);
		if(actions == null) {
			System.out.println("Failed to query actions!");
			return;
		}
		if(actions.length == 0)
			return;
		// not idling
		hasMore = true;
		// call events
		int count = 0;
		for(ActionEvent event : actions) {
			if(stopping) break;
			// call in main thread
			SafetyBukkit.callEvent(event);
			count++;
		}
		System.out.println("Trigger [ "+Integer.toString(count)+" ] actions..");
	}
	// update db cache (outbound)
	private void execUpdates() {
		if(stopping) return;
		synchronized(handlers) {
			for(ActionHandler handler : handlers.values()) {
				if(stopping) break;
				if(handler == null) continue;
				if(handler.isEnabled())
					handler.doUpdate();
			}
		}
	}


	// query actions table (inbound updates)
	private ActionEvent[] queryActions(dbQuery db) {
		if(stopping) return null;
		if(db == null) return null;
		// query table
		db.prepare("SELECT `action_id`, `player`, `handler`, `action` "+
			"FROM `"+TABLE_ACTIONS+"` "+
			"WHERE `action_id` > ? "+
			"ORDER BY `priority` DESC, `action_id` ASC "+
			"LIMIT "+Integer.toString(ActionsPerCycle)+
			" /* query actions table */");
		db.setInt(1, lastId);
		if(!db.exec())
			return null;
		// iterate actions
		List<ActionEvent> actions = new ArrayList<ActionEvent>();
		while(db.next()) {
			String handlerName = db.getString("handler");
			if(handlerName != null && !handlerName.isEmpty()) {
				// action event
				actions.add(
					new ActionEvent(
						db.dbKey(),
						db.getInt("action_id"),
						db.getString("player"),
						db.getString("handler"),
						db.getString("action")
					)
				);
				lastId = db.getInt("action_id");
			}
		}
		db.clean();
		// action events to perform
		return actions.toArray(new ActionEvent[actions.size()]);
	}
	// remove completed action from db
	public static boolean removeAction(String dbKey, int id) {
		if(stopping) return false;
		dbQuery db = dbQuery.get(dbKey);
		try {
			db.prepare("DELETE FROM `"+TABLE_ACTIONS+"` WHERE `action_id` = ? LIMIT 1");
			db.setInt(1, id);
			if(!db.exec())
				return false;
			return (db.getAffectedRows() != 0);
		} finally {
			db.release();
		}
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
	public Boolean getEnabled(String handlerName) {
		ActionHandler handler = getHandler(handlerName);
		if(handler == null) {
			System.out.println("Unknown web action handler: "+handlerName);
			return null;
		}
		return handler.isEnabled();
	}


}
