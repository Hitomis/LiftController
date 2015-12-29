package com.zhiitek.liftcontroller.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zhiitek.liftcontroller.utils.AppUtil;

public class LiftControllerSqlHelper extends SQLiteOpenHelper {
	
	private final static String DATANAME = "liftcontroller";
	private final static int VERSION = 1;

	public LiftControllerSqlHelper(Context context) {
		super(context, DATANAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table if not exists apListTable(ssid varchar)");
		
		db.execSQL("create table if not exists liftInfoTable(liftNo varchar, devSn varchar, chkCode varchar)");
		
		db.execSQL("create table if not exists updateTimeTable(itemName varchar, itemValue varchar)");
		
		db.execSQL("insert into updateTimeTable values('updateTime', '"+AppUtil.getBottomTime()+"');");
		
		db.execSQL("create table if not exists loginServerUrlTable(url varchar)");
		
		db.execSQL("CREATE UNIQUE INDEX unique_index_loginurl ON loginServerUrlTable (url);");
		
		db.execSQL("create table if not exists pushServerUrlTable(url varchar)");
		
		db.execSQL("CREATE UNIQUE INDEX unique_index_pushurl ON pushServerUrlTable (url);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
