package com.poixson.webxbukkit.webLink.handlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.poixson.commonjava.Utils.StringParser;
import com.poixson.commonjava.Utils.utilsMath;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.webxbukkit.Plugins3rdParty;
import com.poixson.webxbukkit.SafetyBukkit;
import com.poixson.webxbukkit.WebAPI;
import com.poixson.webxbukkit.webLink.ActionEvent;
import com.poixson.webxbukkit.webLink.ActionHandler;


public class economyHandler extends ActionHandler {
	private static final String HANDLER_NAME = "economy";

	private final Map<String, Double> cache = new ConcurrentHashMap<String, Double>();


	public economyHandler(String dbKey) {
		super(dbKey);
	}
	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}


	// update db cache
	@Override
	public void doUpdate() {
		Economy econ = Plugins3rdParty.get().getEconomy();
		// get online players
		Player[] players = SafetyBukkit.getOnlinePlayers();
		if(players == null || players.length == 0) return;
		// update db cache for each player
		for(Player p : players) {
			if(p == null) continue;
			String playerName = p.getName();
			if(playerName == null || playerName.isEmpty()) continue;
			Double money = econ.getBalance(playerName);
			if(money == null) continue;
			Double moneyCached = cache.get(playerName);
			// balance hasn't changed
			if(moneyCached != null)
				if(moneyCached.doubleValue() == money.doubleValue())
					continue;
			// update db cache
			UpdateCache(playerName);
		}
	}
	// push to db
	private void UpdateCache(String playerName) {
		dbQuery db = null;
		synchronized(cache) {
			Double moneyCached = cache.get(playerName);
			double money = Plugins3rdParty.get().getEconomy().getBalance(playerName);
			// no change
			if(moneyCached != null)
				if(moneyCached.equals(money))
					return;
			// update value in db
			db = dbQuery.get(dbKey());
			db.prepare("UPDATE `pxn_Players` SET `money` = ? WHERE `name` = ? LIMIT 1");
			db.setDecimal(1, money);
			db.setString (2, playerName);
			db.exec();
			if(db.getAffectedRows() == 0) {
				// insert new row
				db.prepare("INSERT INTO `pxn_Players` (`name`, `money`) VALUES (?, ?)");
				db.setString (1, playerName);
				db.setDecimal(2, money);
				db.exec();
				if(db.getAffectedRows() == 0)
					System.out.println("Failed to create new player account");
			}
			// save cached value
			cache.put(playerName, money);
		}
		if(db != null)
			db.release();
	}


	// perform action
	@Override
	@EventHandler
	public void onAction(ActionEvent event) {
		if(!event.isHandler(getHandlerName())) return;
		if(event.isCancelled()) return;
		if(!event.complete()) return;
		// action parser
		StringParser action = event.getActionParser();
		if(action == null || !action.next()) return;
		Economy econ = WebAPI.get().getEconomy();

		// deposit
		if(action.isFirst("deposit")) {
			// player
			final String playerName = event.getPlayerName();
			// amount
			final Double amount = utilsMath.parseDouble(action.getNext());
			if(amount == null) {
				System.out.println("Invalid deposit amount "+action.get());
				return;
			}
			// call vault api
			econ.depositPlayer(
				playerName,
				amount
			);
			// update db cache
			UpdateCache(playerName);
			// done
			System.out.println("Deposit "+Double.toString(amount)+" to "+playerName);
			return;
		}

		// withdraw
		if(action.isFirst("withdraw")) {
			// player
			final String playerName = event.getPlayerName();
			// amount
			final Double amount = utilsMath.parseDouble(action.getNext());
			if(amount == null) {
				System.out.println("Invalid withdraw amount "+action.get());
				return;
			}
			// call vault api
			econ.withdrawPlayer(
				playerName,
				amount
			);
			// update db cache
			UpdateCache(playerName);
			// done
			System.out.println("Withdraw "+Double.toString(amount)+" from "+playerName);
			return;
		}

		// unknown action
		System.out.println("Failed to execute action: "+event.toString());
	}


}
