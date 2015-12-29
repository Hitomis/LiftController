package com.zhiitek.liftcontroller.activity;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.db.LiftControllerSqlHelper;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.views.SpinnerEditText;

public class UrlSettingActivity extends BaseActivity{
	
	private static final int MAX_SAVED_URL_NUMBERS = 5;

	private SpinnerEditText loginUrlEdit;
	
	SharedPreferences sharedPreferences;
	
	private ArrayList<String> loginServerUrlCache;
	
	public void saveUrl(View v) {
		String loginurl = loginUrlEdit.getText().toString().trim();
		if (loginurl.isEmpty()) {
			showToast("服务器地址不能为空！");
		} else {
			Editor editor = sharedPreferences.edit();
			editor.putString(AppConstant.KEY_SERVICE_URL, loginurl);
			editor.apply();
			saveServerUrl2Database(loginurl, "loginServerUrlTable");
			showToast("地址保存成功!");
			finish();
		}
	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_url_setting);
	}

	@Override
	protected void findViewById() {
		loginUrlEdit = (SpinnerEditText) findViewById(R.id.login_url_edit);
	}

	@Override
	protected void setListener() {
	}


	@Override
	protected void dealProcessLogic() {
		setTitleBar("服务器地址配置", null);
		sharedPreferences = getSharedPreferences(AppConstant.KEY_SERVICE_URL, Context.MODE_PRIVATE);
		if(sharedPreferences.contains(AppConstant.KEY_SERVICE_URL)) {
			loginUrlEdit.setText(sharedPreferences.getString(AppConstant.KEY_SERVICE_URL, ""));
		}
		loginServerUrlCache = new ArrayList<String>();
		getLoginServerUrl();
		if(loginServerUrlCache.isEmpty()) {
			loginServerUrlCache.add("");
		}
		loginUrlEdit.setAdapter(new BaseAdapterHelper<String>(this, loginServerUrlCache, R.layout.popup_spinner_item) {
			@Override
			public void convert(ViewHolder viewHolder, String item) {
				viewHolder.setText(R.id.tv_item_name, item);
			}
		});

	}
	
	private void saveServerUrl2Database(String url, String tableName) {
		LiftControllerSqlHelper database = new LiftControllerSqlHelper(this);
		SQLiteDatabase db = database.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("url", url);
		db.replace(tableName, null, cv);
		db.close();
	}
	
	private void getLoginServerUrl() {
		LiftControllerSqlHelper database = new LiftControllerSqlHelper(this);
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor c = db.query("loginServerUrlTable",null,null,null,null,null,null);//查询并获得游标
		if(c.moveToLast()){//判断游标是否为空
			int i = 0;
			loginServerUrlCache.add(c.getString(c.getColumnIndex("url")));
		    while(c.moveToPrevious() && ++i < MAX_SAVED_URL_NUMBERS) {
		    	loginServerUrlCache.add(c.getString(c.getColumnIndex("url")));
		    }
		    c.close();
		}
		db.close();
	}

}
