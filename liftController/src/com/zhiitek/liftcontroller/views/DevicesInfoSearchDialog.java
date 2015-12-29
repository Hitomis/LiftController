package com.zhiitek.liftcontroller.views;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.model.DevicesInfo;
import com.zhiitek.liftcontroller.utils.DensityUtil;

public class DevicesInfoSearchDialog extends Dialog {
	
	private Context context;
	
	private ListView lvSearchContent;
	
	private TextView tvSearchCancel;
	
	private EditText etKeyWords;
	
	private List<DevicesInfo> dataList;

	protected BaseAdapterHelper<DevicesInfo> mSearchDvicesInfoAdapter;
	
	public DevicesInfoSearchDialog(Context context, List<DevicesInfo> devicesInfoList) {
		super(context, R.style.search_dialog);
		
		this.context = context;
		this.dataList = devicesInfoList;
		setContentView(R.layout.dialog_search);
		setCanceledOnTouchOutside(true);
		
		initDialog();
		findViewByID();
		setListener();
	}

	public void setItemClickListener(OnItemClickListener itemClickListener) {
		if (itemClickListener != null) {
			lvSearchContent.setOnItemClickListener(itemClickListener);
		}
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
	}
	
	/**
	 * 在当前ListView数据表中搜索关键字
	 * @param keyWords
	 */
	private void searchKeyWordsInDataList(String keyWords) {
		
		List<DevicesInfo> searchDevicesInfoList = new ArrayList<DevicesInfo>();
		for (DevicesInfo info : dataList) {
			if (isContain(info, keyWords)) {
				searchDevicesInfoList.add(info);
			}
		}
		
		if (mSearchDvicesInfoAdapter == null) {
			mSearchDvicesInfoAdapter = new BaseAdapterHelper<DevicesInfo>(context, searchDevicesInfoList, R.layout.item_my_devices) {

				@Override
				public void convert(ViewHolder viewHolder, DevicesInfo item) {
					viewHolder.setText(R.id.tv_lift_name, item.getLiftName());
					viewHolder.setText(R.id.tv_lift_no, item.getLiftNo());
					viewHolder.setText(R.id.tv_lift_status, item.getRunningStatus());
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
	private boolean isContain(DevicesInfo info, String keyWords) {
		if (TextUtils.isEmpty(keyWords)) return false;
		if (info.getLiftName().contains(keyWords) ||
			info.getLiftNo().contains(keyWords)) {
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
