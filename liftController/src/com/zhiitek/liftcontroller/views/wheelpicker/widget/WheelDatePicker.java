package com.zhiitek.liftcontroller.views.wheelpicker.widget;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.views.wheelpicker.view.AbstractWheelDecor;
import com.zhiitek.liftcontroller.views.wheelpicker.view.IWheelPicker;
import com.zhiitek.liftcontroller.views.wheelpicker.view.WheelPicker;

/**
 * 基于WheelPicker的日期选择控件
 * DatePicker base on WheelPicker
 *
 */
public class WheelDatePicker extends LinearLayout implements IWheelPicker {
    private WheelYearPicker pickerYear;
    private WheelMonthPicker pickerMonth;
    private WheelDayPicker pickerDay;
    private WheelHourPicker pickerHour;
    private WheelMinutePicker pickerMinute;

    private final Rect rectText = new Rect();

    private String year, month, day, hour, minute;
    private int labelColor = 0xFF000000;
    private int stateYear, stateMonth, stateDay, stateHour, stateMinute;

    public WheelDatePicker(Context context) {
        this(context, null);
    }

    public WheelDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        int padding = getResources().getDimensionPixelSize(R.dimen.WheelPadding1x);
        int padding2x = padding * 2;

        LayoutParams llParams = new LayoutParams(-2, -2);

        pickerYear = new WheelYearPicker(context);
        pickerMonth = new WheelMonthPicker(context);
        pickerDay = new WheelDayPicker(context);
        pickerHour = new WheelHourPicker(context);
        pickerMinute = new WheelMinutePicker(context);
        pickerYear.setPadding(padding2x, padding, padding2x, padding);
        pickerMonth.setPadding(padding2x, padding, padding2x, padding);
        pickerDay.setPadding(padding2x, padding, padding2x, padding);
        pickerHour.setPadding(padding2x, padding, padding2x, padding);
        pickerMinute.setPadding(padding2x, padding, padding2x, padding);
        addLabel(pickerYear, "年");
        addLabel(pickerMonth, "月");
        addLabel(pickerDay, "日");
        addLabel(pickerHour, "时");
        addLabel(pickerMinute, "分");

        addView(pickerYear, llParams);
        addView(pickerMonth, llParams);
        addView(pickerDay, llParams);
        addView(pickerHour, llParams);
        addView(pickerMinute, llParams);
    }

    private void addLabel(WheelPicker picker, final String label) {
        picker.setCurrentItemForegroundDecor(true, new AbstractWheelDecor() {
            @Override
            public void drawDecor(Canvas canvas, Rect rect, Paint paint) {
                paint.setColor(labelColor);
                paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.WheelPadding1x) * 1.5F);
                paint.getTextBounds(label, 0, 1, rectText);
                int width = rectText.width();
                canvas.drawText(label, rect.right - width / 2, rect.centerY() - (paint.ascent() + paint.descent()) / 2.0F, paint);
            }
        });
    }

    public void setLabelColor(int labelColor) {
        this.labelColor = labelColor;
        invalidate();
    }

    @Override
    public void setStyle(int style) {
        pickerYear.setStyle(style);
        pickerMonth.setStyle(style);
        pickerDay.setStyle(style);
        pickerHour.setStyle(style);
    }

    @Override
    public void setData(List<String> data) {
        throw new RuntimeException("Set data will not allow here!");
    }

    public void setCurrentDate(int year, int month, int day, int hour, int minute) {
        pickerYear.setCurrentYear(year);
        pickerMonth.setCurrentMonth(month);
        pickerDay.setCurrentYearAndMonth(year, month);
        pickerDay.setCurrentDay(day);
        pickerHour.setCurrentHour(hour);
        pickerMinute.setCurrentMinute(minute);
    }
    
    @SuppressLint("SimpleDateFormat")
	public void setCurrentDate(String timeStr) {
    	try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = sdf.parse(timeStr);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			setCurrentDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, 
								      c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), 
								      c.get(Calendar.MINUTE));
		} catch (ParseException e) {
		}
    }

    @Override
    public void setTextColor(int color) {
        pickerYear.setTextColor(color);
        pickerMonth.setTextColor(color);
        pickerDay.setTextColor(color);
        pickerHour.setTextColor(color);
        pickerMinute.setTextColor(color);
    }

    @Override
    public void setItemIndex(int index) {
        pickerYear.setItemIndex(index);
        pickerMonth.setItemIndex(index);
        pickerDay.setItemIndex(index);
        pickerHour.setItemIndex(index);
        pickerMinute.setItemCount(index);
    }

    @Override
    public void setTextSize(int size) {
        pickerYear.setTextSize(size);
        pickerMonth.setTextSize(size);
        pickerDay.setTextSize(size);
        pickerHour.setTextSize(size);
        pickerMinute.setTextSize(size);
    }

    private void transData(final WheelPicker.OnWheelChangeListener listener) {
    	listener.onWheelSelected(-1, year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + "00");
    }
    
    public String getData() {
    	return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + "00";
    }
    
    @Override
    public void setOnWheelChangeListener(final WheelPicker.OnWheelChangeListener listener) {
        pickerYear.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling() {
                listener.onWheelScrolling();
            }

            @Override
            public void onWheelSelected(int index, String data) {
                year = data;
                if (isValidDate()) {
                    pickerDay.setCurrentYearAndMonth(Integer.valueOf(year), Integer.valueOf(month));
                    transData(listener);
                }
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                stateYear = state;
                checkState(listener);
            }
        });
        pickerMonth.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling() {
                listener.onWheelScrolling();
            }

            @Override
            public void onWheelSelected(int index, String data) {
                month = data.length() == 1 ? String.format("0%s", data) : data;
                if (isValidDate()) {
                    pickerDay.setCurrentYearAndMonth(Integer.valueOf(year), Integer.valueOf(month));
                    transData(listener);
                }
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                stateMonth = state;
                checkState(listener);
            }
        });
        pickerDay.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling() {
                listener.onWheelScrolling();
            }

            @Override
            public void onWheelSelected(int index, String data) {
                day = data.length() == 1 ? String.format("0%s", data) : data;
                if (isValidDate()) {
                	transData(listener);
                }
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                stateDay = state;
                checkState(listener);
            }
        });
        pickerHour.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
			
        	@Override
        	public void onWheelScrolling() {
        		listener.onWheelScrolling();
        	}
			
        	@Override
        	public void onWheelSelected(int index, String data) {
        		hour = data.length() == 1 ? String.format("0%s", data) : data;
        		if (isValidDate()) {
        			transData(listener);
        		}
        	}
			
			@Override
			public void onWheelScrollStateChanged(int state) {
				stateHour = state;
				checkState(listener);
			}
		});
        pickerMinute.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
        	
        	@Override
			public void onWheelScrolling() {
        		listener.onWheelScrolling();
			}
			
			@Override
			public void onWheelSelected(int index, String data) {
				minute = data.length() == 1 ? String.format("0%s", data) : data;
				if (isValidDate()) {
					transData(listener);
				}
			}
			
			@Override
			public void onWheelScrollStateChanged(int state) {
				stateMinute = state;
				checkState(listener);
			}
		});
    }

    private void checkState(WheelPicker.OnWheelChangeListener listener) {
        if (stateYear == WheelPicker.SCROLL_STATE_IDLE &&
                stateMonth == WheelPicker.SCROLL_STATE_IDLE &&
                stateDay == WheelPicker.SCROLL_STATE_IDLE &&
                stateHour == WheelPicker.SCROLL_STATE_IDLE &&
                stateMinute == WheelPicker.SCROLL_STATE_IDLE) {
            listener.onWheelScrollStateChanged(WheelPicker.SCROLL_STATE_IDLE);
        }
        if (stateYear == WheelPicker.SCROLL_STATE_SCROLLING ||
                stateMonth == WheelPicker.SCROLL_STATE_SCROLLING ||
                stateDay == WheelPicker.SCROLL_STATE_SCROLLING || 
                stateHour == WheelPicker.SCROLL_STATE_SCROLLING ||
                stateMinute == WheelPicker.SCROLL_STATE_SCROLLING) {
            listener.onWheelScrollStateChanged(WheelPicker.SCROLL_STATE_SCROLLING);
        }
        if (stateYear + stateMonth + stateDay + stateHour + stateMinute == 1) {
            listener.onWheelScrollStateChanged(WheelPicker.SCROLL_STATE_DRAGGING);
        }
    }

    private boolean isValidDate() {
        return !TextUtils.isEmpty(year) && !TextUtils.isEmpty(month) && !TextUtils.isEmpty(day) && !TextUtils.isEmpty(hour) && !TextUtils.isEmpty(minute);
    }

    @Override
    public void setCurrentItemBackgroundDecor(boolean ignorePadding, AbstractWheelDecor decor) {
        setDecorBackgroundYear(ignorePadding, decor);
        setDecorBackgroundMonth(ignorePadding, decor);
        setDecorBackgroundDay(ignorePadding, decor);
        setDecorBackgroundHour(ignorePadding, decor);
        setDecorBackgroundMinute(ignorePadding, decor);
    }

	@Override
    public void setCurrentItemForegroundDecor(boolean ignorePadding, AbstractWheelDecor decor) {
        setDecorForegroundYear(ignorePadding, decor);
        setDecorForegroundMonth(ignorePadding, decor);
        setDecorForegroundDay(ignorePadding, decor);
        setDecorForegroundHour(ignorePadding, decor);
        setDecorForegroundMinute(ignorePadding, decor);
    }

	public void setDecorForegroundYear(boolean ignorePadding, AbstractWheelDecor decor) {
        pickerYear.setCurrentItemForegroundDecor(ignorePadding, decor);
    }

    public void setDecorForegroundMonth(boolean ignorePadding, AbstractWheelDecor decor) {
        pickerMonth.setCurrentItemForegroundDecor(ignorePadding, decor);
    }

    public void setDecorForegroundDay(boolean ignorePadding, AbstractWheelDecor decor) {
        pickerDay.setCurrentItemForegroundDecor(ignorePadding, decor);
    }
    
    private void setDecorForegroundHour(boolean ignorePadding, AbstractWheelDecor decor) {
    	pickerHour.setCurrentItemForegroundDecor(ignorePadding, decor);
    }
    
    private void setDecorForegroundMinute(boolean ignorePadding, AbstractWheelDecor decor) {
    	pickerMinute.setCurrentItemForegroundDecor(ignorePadding, decor);
    }

    public void setDecorBackgroundYear(boolean ignorePadding, AbstractWheelDecor decor) {
        pickerYear.setCurrentItemBackgroundDecor(ignorePadding, decor);
    }

    public void setDecorBackgroundMonth(boolean ignorePadding, AbstractWheelDecor decor) {
        pickerMonth.setCurrentItemBackgroundDecor(ignorePadding, decor);
    }

    public void setDecorBackgroundDay(boolean ignorePadding, AbstractWheelDecor decor) {
        pickerDay.setCurrentItemBackgroundDecor(ignorePadding, decor);
    }
    
	private void setDecorBackgroundHour(boolean ignorePadding, AbstractWheelDecor decor) {
		pickerHour.setCurrentItemBackgroundDecor(ignorePadding, decor);
	}

    private void setDecorBackgroundMinute(boolean ignorePadding, AbstractWheelDecor decor) {
    	pickerMinute.setCurrentItemBackgroundDecor(ignorePadding, decor);
	}

    @Override
    public void setItemSpace(int space) {
        pickerYear.setItemSpace(space);
        pickerMonth.setItemSpace(space);
        pickerDay.setItemSpace(space);
        pickerHour.setItemSpace(space);
        pickerMinute.setItemSpace(space);
    }

    @Override
    public void setItemCount(int count) {
        pickerYear.setItemCount(count);
        pickerMonth.setItemCount(count);
        pickerDay.setItemCount(count);
        pickerHour.setItemCount(count);
        pickerMinute.setItemCount(count);
        
    }
}