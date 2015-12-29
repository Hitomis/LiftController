package com.zhiitek.liftcontroller.views;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.activity.AlarmDetailsActivity;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.fragment.MyAlarmFragment;
import com.zhiitek.liftcontroller.model.AlarmInfo;
import com.zhiitek.liftcontroller.utils.DensityUtil;

public class AlarmsInfoSearchDialog extends Dialog{
	private Context context;
	
	private ListView lvSearchContent;
	
	private TextView tvSearchCancel;
	
	private EditText etKeyWords;
	
	private List<AlarmInfo> dataList;

	protected BaseAdapterHelper<AlarmInfo> mSearchDvicesInfoAdapter;
	
	public AlarmsInfoSearchDialog(Context context, List<AlarmInfo> devicesInfoList) {
		super(context, R.style.search_dialog);
		
		this.context = context;
		this.dataList = devicesInfoList;
		setContentView(R.layout.dialog_search);
		setCanceledOnTouchOutside(true);
		
		initDialog();
		findViewByID();
		setListener();
	}

	private void findViewByID() {
		lvSearchContent = (ListView) findViewById(R.id.lv_search_content);
		tvSearchCancel = (TextView) findViewById(R.id.tv_search_cancel);
		etKeyWords = (EditText) findViewById(R.id.et_key_words);
	}
	
	private void setListener() {
		tvSearchCancel.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		etKeyWords.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				searchKeyWordsInDataList(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		
		etKeyWords.setOnKeyListener(new android.view.View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
					String keyWords = etKeyWords.getText().toString().trim();
					searchKeyWordsInDataList(keyWords);
				}
				return false;
			}

		});
		
		lvSearchContent.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(context, AlarmDetailsActivity.class);
				intent.putExtra("alarmInfo", mSearchDvicesInfoAdapter.getItem(position));
				context.startActivity(intent);
			}
		});
	}
	
	/**
	 * 在当前ListView数据表中搜索关键字
	 * @param keyWords
	 */
	private void searchKeyWordsInDataList(String keyWords) {
		
		List<AlarmInfo> searchDevicesInfoList = new ArrayList<AlarmInfo>();
		for (AlarmInfo info : dataList) {
			if (isContain(info, keyWords)) {
				searchDevicesInfoList.add(info);
			}
		}
		
		if (mSearchDvicesInfoAdapter == null) {
			mSearchDvicesInfoAdapter = new BaseAdapterHelper<AlarmInfo>(context, searchDevicesInfoList, R.layout.item_alarm) {

				@Override
				public void convert(ViewHolder viewHolder, AlarmInfo item) {
					ImageView ivtab = viewHolder.getView(R.id.iv_tab);
					if (item.getAlarmLevel().equals("9")) {
						ivtab.setImageResource(R.drawable.icon_tab_one);
					} else if (item.getAlarmLevel().equals("8")) {
						ivtab.setImageResource(R.drawable.icon_tab_two);
					} else {
						ivtab.setImageResource(R.drawable.icon_tab_three);
					}
					viewHolder.setText(R.id.tv_lift_no, item.getLiftNo());
					viewHolder.setText(R.id.tv_lift_name, item.getLiftName());
					viewHolder.setText(R.id.tv_alarm_name, item.getAlarmName());
					viewHolder.setText(R.id.tv_alarm_time, item.getAlarmTime());
				}
			};
			lvSearchContent.setAdapter(mSearchDvicesInfoAdapter);
		} else {
			mSearchDvicesInfoAdapter.setDataList(searchDevicesInfoList);
			mSearchDvicesInfoAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * DevicesInfo 对象的各个属性值中是否包含搜索的关键字
	 * @param info
	 * @param keyWords
	 * @return
	 */
	private boolean isContain(AlarmInfo info, String keyWords) {
		if (TextUtils.isEmpty(keyWords)) return false;
		if (info.getLiftName().contains(keyWords) ||
			info.getLiftNo().contains(keyWords) ||
			info.getAlarmLevel().contains(keyWords)) {
			return true;
		}
		return false;
	}

	private void initDialog() {
		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.gravity = Gravity.TOP;
		layoutParams.width = DensityUtil.getScreenWidth(context);
		layoutParams.alpha = 1.0f;//dialog中内容视图透明度
		getWindow().setAttributes(layoutParams);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | 
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
}
