package com.poixson.webxbukkit.webSettings;

import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.commonjava.pxdb.TableManager.TableDAO;
import com.poixson.commonjava.pxdb.TableManager.dbTableManager;
import com.poixson.webxbukkit.WebAPI;


public class dbTableDefines extends dbTableManager {

	private static final String TABLE_PREFIX = "pxn_";


	@Override
	public void InitTables() {
		// Settings table
		TableDAO tableSettings =
			defineTable("Settings")
				.idField("setting_id")
				//			type	name			size	default	nullable
				.addField("str",	"name",			"32",	null,	false)
				.addField("str",	"value",		"255",	null,	true)
				.unique("name");
		createIfMissing(tableSettings);
//		protected boolean setTableExists(String tableName, String Sql) {
//			if (tableExists(tableName)) return false;
//			log.info(logPrefix+"Creating table "+tableName);
//			executeRawSQL("CREATE TABLE `"+dbPrefix+tableName+"` ( "+Sql+" );");
//			return true;
//		}
//		setTableExists("Settings",
//				"`id`				INT(11)			NOT NULL	AUTO_INCREMENT	, PRIMARY KEY(`id`), " +
//				"`name`				VARCHAR(32)		NULL		DEFAULT NULL	, UNIQUE(`name`)   , " +
//				"`value`			VARCHAR(255)	NULL		DEFAULT NULL	");
	}


	@Override
	protected dbQuery getDB() {
		return WebAPI.get().getDB();
	}


	@Override
	protected String getTablePrefix() {
		return TABLE_PREFIX;
	}


}
