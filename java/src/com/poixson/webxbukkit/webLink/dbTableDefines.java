package com.poixson.webxbukkit.webLink;

import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.commonjava.pxdb.TableManager.TableDAO;
import com.poixson.commonjava.pxdb.TableManager.dbTableManager;


public class dbTableDefines extends dbTableManager {

	private static final String TABLE_PREFIX = "pxn_";


	@Override
	public void InitTables() {
		// Players table
		TableDAO tablePlayers =
			defineTable("Players")
				.idField("player_id")
				//			type	name			size	default	nullable
				.addField("str",	"player",		"32",	null,	false)
				.addField("str",	"pass",			"32",	null,	false)
				.addField("dec",	"money",		"10,4",	"0.0",	false)
				.addField("bool",	"locked",		null,	"0",	false)
				.addField("str",	"ip",			"15",	null,	true)
				.unique("player");
		createIfMissing(tablePlayers);
		// Actions table
		TableDAO tableActions =
			defineTable("Actions")
				.idField("auction_id")
				//			type	name			size	default	nullable
				.addField("int",	"priority",		"3",	"0",	false)
				.addField("str",	"player",		"32",	null,	true)
				.addField("str",	"handler",		"16",	null,	true)
				.addField("str",	"action",		"255",	null,	true);
		createIfMissing(tableActions);
	}


	@Override
	protected dbQuery getDB() {
		return null;
	}


	@Override
	protected String getTablePrefix() {
		return TABLE_PREFIX;
	}


}
