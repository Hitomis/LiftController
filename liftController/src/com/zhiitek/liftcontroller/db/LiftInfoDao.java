package com.zhiitek.liftcontroller.db;

import com.zhiitek.liftcontroller.model.LiftInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LiftInfoDao {
	
	private LiftControllerSqlHelper dbHelper;
	
	public LiftInfoDao(Context context) {
		dbHelper = new LiftControllerSqlHelper(context);
	}

	public boolean add(LiftInfo liftInfo) {
		boolean flag = false;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("liftNo", liftInfo.getLiftNo());
		cv.put("devSn", liftInfo.getDevSerial());
		cv.put("chkCode", liftInfo.getCheckCode());
		
		if (database.insert("liftInfoTable", null, cv) > -1)
			flag = true;
		database.close();
		return flag;
	}
	
	public LiftInfo queryLift(String liftNo) {
		LiftInfo liftInfo = new LiftInfo();
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select * from liftInfoTable where liftNo=?", new String[]{liftNo});
		if (cursor.moveToFirst()) {
			liftInfo.setLiftNo(cursor.getString(cursor.getColumnIndex("liftNo")));
			liftInfo.setDevSerial(cursor.getString(cursor.getColumnIndex("devSn")));
			liftInfo.setCheckCode(cursor.getString(cursor.getColumnIndex("chkCode")));
		} else {
			liftInfo = null;
		}
		cursor.close();
		database.close();
		return liftInfo;
	}
	
	public void clear() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.execSQL("delete from liftInfoTable");
		database.close();
	}
}
