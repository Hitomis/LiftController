package com.zhiitek.liftcontroller.views.wheelpicker.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.model.LocalInfo;
import com.zhiitek.liftcontroller.views.wheelpicker.view.AbstractWheelDecor;
import com.zhiitek.liftcontroller.views.wheelpicker.view.IWheelPicker;
import com.zhiitek.liftcontroller.views.wheelpicker.view.WheelPicker;
import com.zhiitek.liftcontroller.views.wheelpicker.view.WheelPicker.OnWheelChangeListener;

public class WheelLocalPicker extends LinearLayout implements IWheelPicker {
	
	private WheelPicker pickerProvince;
	private WheelPicker pickerCity;
	private WheelPicker pickerDistricts;
	
	private List<LocalInfo> localList;
	
	private final int defaultIndex = 0;
	
	private LocalInfo currCheckProvince, currCheckcity, currCheckDistrict;
	
	public WheelLocalPicker(Context context) {
		this(context, null);
	}

	public WheelLocalPicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WheelLocalPicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		pickerProvince = new WheelPicker(context, attrs);
        pickerCity = new WheelPicker(context, attrs);
        pickerDistricts = new WheelPicker(context, attrs);
		initDisplay();
	}

	/**
	 * 初始化省市区级联选择控件的外观样式
	 */
	private void initDisplay() {
		setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        int padding = getResources().getDimensionPixelSize(R.dimen.WheelPadding1x);
        int padding2x = padding * 2;

        pickerProvince.setPadding(padding2x, padding, padding2x, padding);
        pickerCity.setPadding(padding2x, padding, padding2x, padding);
        pickerDistricts.setPadding(padding2x, padding, padding2x, padding);
        
        LayoutParams llParams = new LayoutParams(-2, -2);
        
        addView(pickerProvince, llParams);
        addView(pickerCity, llParams);
        addView(pickerDistricts, llParams);
	}
    
	public void setLocalData(List<LocalInfo> localList) {
		if (localList != null && !localList.isEmpty()) {
			this.localList = localList;
			
			
			LocalInfo localInfo = localList.get(defaultIndex);
			
			List<String> provinces = Arrays.asList(localInfo.getName());
			List<String> cities = obtainNameList(localInfo.getChildren());
			List<String> districts = obtainNameList(localInfo.getChildren().get(defaultIndex).getChildren());
			
			pickerProvince.setData(provinces);
			pickerCity.setData(cities);
			pickerDistricts.setData(districts);
		}
	}
	
	private List<String> obtainNameList(List<LocalInfo> localInfoList) {
		List<String> nameList = new ArrayList<String>();
		for (LocalInfo localInfo : localInfoList) {
			nameList.add(localInfo.getName());
		}
		return nameList;
	}
	
	@Override
	public void setData(List<String> data) {
		 throw new RuntimeException("Set data will not allow here!");
	}
	
	@Override
	public void setStyle(int style) {
		pickerProvince.setStyle(style);
		pickerCity.setStyle(style);
		pickerDistricts.setStyle(style);
	}

	@Override
	public void setTextColor(int color) {
		pickerProvince.setTextColor(color);
		pickerCity.setTextColor(color);
		pickerDistricts.setTextColor(color);

	}

	@Override
	public void setItemIndex(int index) {
		pickerProvince.setItemIndex(index);
		pickerCity.setItemIndex(index);
		pickerDistricts.setItemIndex(index);
	}

	@Override
	public void setTextSize(int size) {
		pickerProvince.setTextSize(size);
		pickerCity.setTextSize(size);
		pickerDistricts.setTextSize(size);
	}
	
	@Override
	public void setItemSpace(int space) {
		pickerProvince.setItemSpace(space);
		pickerCity.setItemSpace(space);
		pickerDistricts.setItemSpace(space);

	}

	@Override
	public void setItemCount(int count) {
		pickerProvince.setItemCount(count);
		pickerCity.setItemCount(count);
		pickerDistricts.setItemCount(count);
	}
	
	@Override
	public void setCurrentItemBackgroundDecor(boolean ignorePadding, AbstractWheelDecor decor) {
		pickerProvince.setCurrentItemBackgroundDecor(ignorePadding, decor);
		pickerCity.setCurrentItemBackgroundDecor(ignorePadding, decor);
		pickerDistricts.setCurrentItemBackgroundDecor(ignorePadding, decor);
	}

	@Override
	public void setCurrentItemForegroundDecor(boolean ignorePadding, AbstractWheelDecor decor) {
		pickerProvince.setCurrentItemForegroundDecor(ignorePadding, decor);
		pickerCity.setCurrentItemForegroundDecor(ignorePadding, decor);
		pickerDistricts.setCurrentItemForegroundDecor(ignorePadding, decor);
	}
	
	public Map<String, String> getData() {
		Map<String ,String> checkLLocalInfo = new HashMap<String, String>();
		String checkLocalName = new StringBuilder(currCheckProvince.getName())
									.append(currCheckcity.getName())
									.append(currCheckDistrict.getName())
									.toString();
		checkLLocalInfo.put(currCheckDistrict.getCode(), checkLocalName);
		return checkLLocalInfo;
	}
	
//	public void setCurrentLocal(String provinceName, String cityName, String districtName) {
//		pickerProvince.setItemIndex(findIndexByName(provinceName, 1));
//		pickerCity.setItemIndex(findIndexByName(cityName, 2));
//		pickerDistricts.setItemIndex(findIndexByName(districtName, 3));
//	}
//	
//	private int findIndexByName(String name, int layer) {
//		int index = -1;
//		if (layer == 1) {
//			for (int i = 0; i < localList.size(); i++) {
//				LocalInfo localProvince = localList.get(i);
//				if (name.equals(localProvince.getName())) {
//					index = i;
//					break;
//				}
//			}
//		}
//		
//		if (layer == 2) {
//			for (int i = 0; i < localList.size(); i++) {
//				List<LocalInfo> localCities = localList.get(i).getChildren();
//				for (int j = 0; j < localCities.size(); j++) {
//					LocalInfo localCity = localCities.get(j);
//					if (name.equals(localCity.getName())) {
//						index = j;
//						break;
//					}
//				}
//				
//			}
//		}
//		
//		if (layer == 3) {
//			for (int i = 0; i < localList.size(); i++) {
//				List<LocalInfo> localCities = localList.get(i).getChildren();
//				for (int j = 0; j < localCities.size(); j++) {
//					List<LocalInfo> localDistricts = localCities.get(j).getChildren();
//					for (int k = 0; k < localDistricts.size(); k++) {
//						LocalInfo localDistrict = localDistricts.get(k);
//						if (name.equals(localDistrict.getName())) {
//							index = k;
//							break;
//						}
//					}
//					
//				}
//				
//			}
//		}
//		
//		return index;
//	}
	
	@Override
	public void setOnWheelChangeListener(final OnWheelChangeListener listener) {
		pickerProvince.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
			
			@Override
			public void onWheelSelected(int index, String data) {
				currCheckProvince = localList.get(index);
				if (!data.equals(currCheckProvince.getName())) {
					currCheckcity = currCheckProvince.getChildren().get(defaultIndex);
					
					List<String> cities = obtainNameList(currCheckProvince.getChildren());
					List<String> districts = obtainNameList(currCheckcity.getChildren());
					
					pickerCity.setData(cities);
					pickerDistricts.setData(districts);
					
					pickerCity.setItemIndex(defaultIndex);
					pickerDistricts.setItemIndex(defaultIndex);
				}
			}
			
			@Override
			public void onWheelScrolling() {
				
			}
			
			@Override
			public void onWheelScrollStateChanged(int state) {
				
			}
		});
		
		pickerCity.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
			
			@Override
			public void onWheelSelected(int index, String data) {
				currCheckcity = currCheckProvince.getChildren().get(index);
				
				List<String> districts = obtainNameList(currCheckcity.getChildren());
				
				pickerDistricts.setData(districts);
				 
				pickerDistricts.setItemIndex(defaultIndex);
			}
			
			@Override
			public void onWheelScrolling() {
				
			}
			
			@Override
			public void onWheelScrollStateChanged(int state) {
				
			}
		});
		
		pickerDistricts.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
			
			@Override
			public void onWheelSelected(int index, String data) {
				currCheckDistrict = currCheckcity.getChildren().get(index);
			}
			
			@Override
			public void onWheelScrolling() {
				
			}
			
			@Override
			public void onWheelScrollStateChanged(int state) {
				
			}
		});

	}
	
}
