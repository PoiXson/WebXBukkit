package com.poixson.webxbukkit.webLink.handlers;

import java.util.HashMap;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.poixson.commonjava.Utils.StringParser;
import com.poixson.commonjava.Utils.utilsMath;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.webxbukkit.BukkitThreadSafe;
import com.poixson.webxbukkit.Plugins3rdParty;
import com.poixson.webxbukkit.WebAPI;
import com.poixson.webxbukkit.webLink.ActionEvent;
import com.poixson.webxbukkit.webLink.ActionHandler;


public class economyHandler extends ActionHandler {

	public static final String HANDLER_NAME = "economy";

	private final Map<String, Double> cachedMoney = new HashMap<String, Double>();


	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}


	private final Object updateLock = new Object();
	@Override
	public void doUpdate(String dbKey) {
		Economy econ = Plugins3rdParty.get().getEconomy();
		synchronized(updateLock) {
			// get online players
			Player[] players = BukkitThreadSafe.getOnlinePlayers();
			if(players.length == 0) return;
			dbQuery db = dbQuery.get(dbKey);
			try {
				for(Player p : players) {
					if(p == null) continue;
					String playerName = p.getName();
					Double money = econ.getBalance(playerName);
					if(money == null) continue;
					Double cached = cachedMoney.get(playerName);
					// balance hasn't changed
					if(cached != null && money == cached) continue;
					// update db cache
					db.prepare("UPDATE `pxn_Players` SET `money` = ? WHERE `name` = ? LIMIT 1");
					db.setDecimal(1, money);
					db.setString (2, playerName);
					db.exec();
					if(db.getAffectedRows() == 0) {
						// insert new row
						db.clean();
						db.prepare("INSERT INTO `pxn_Players` (`name`, `money`) VALUES (?, ?)");
						db.setString (1, playerName);
						db.setDecimal(2, money);
						db.exec();
						if(db.getAffectedRows() == 0)
							System.out.println("Failed to create new player account");
					}
					db.clean();
					cachedMoney.put(playerName, money);
				}
			} finally {
				db.release();
			}
		}
	}


	@Override
	@EventHandler
	public void onAction(ActionEvent event) {
		if(event.isCancelled()) return;
		if(!event.isHandler(HANDLER_NAME)) return;
		// action parser
		StringParser action = event.getActionParser();
		if(action == null || !action.next()) return;

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
			WebAPI.get().getEconomy().depositPlayer(
				playerName,
				amount
			);
			System.out.println("Deposit "+Double.toString(amount)+" to "+playerName);
		} else

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
			WebAPI.get().getEconomy().withdrawPlayer(
				playerName,
				amount
			);
			System.out.println("Withdraw "+Double.toString(amount)+" from "+playerName);

		// unknown action
		} else {
			System.out.println("Failed to execute action: "+event.toString());
		}
	}


}
