package com.zhiitek.liftcontroller.activity;

import android.graphics.Color;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.model.NoticeInfo;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;

/**
 * 
 * 通告详情页面  </br>
 * 
 * 我的设备 - 通告管理  - 通告详细
 * 
 * @author ZhaoFan
 *
 */
public class NoticeDetailesActivity extends BaseActivity {
	
	private TextView tvStatus; // 通告状态
	private TextView tvType; // 通告类型
	private TextView tvTargetName; // 通告目标名称
	private TextView tvStartTime; // 通告起始时间
	private TextView tvEndTime; // 通告截止时间
	private TextView tvUpdateTime; // 通告更改时间
	private TextView tvSendUsername; // 通告发送人
	private TextView tvContent; // 通告内容
	

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_notice_details);
	}

	@Override
	protected void findViewById() {
		tvStatus = (TextView) findViewById(R.id.tv_status);
		tvType = (TextView) findViewById(R.id.tv_type);
		tvTargetName = (TextView) findViewById(R.id.tv_target_name);
		tvStartTime = (TextView) findViewById(R.id.tv_start_time);
		tvEndTime = (TextView) findViewById(R.id.tv_end_time);
		tvUpdateTime = (TextView) findViewById(R.id.tv_update_time);
		tvSendUsername = (TextView) findViewById(R.id.tv_send_username);
		tvContent = (TextView) findViewById(R.id.tv_content);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void dealProcessLogic() {
		setTitleBar("通告详情", null);

		SwipeFinishLayout.attachToActivity(this);
		
		//接受列表页面传递过来的通告对象数据
		NoticeInfo noticeInfo = (NoticeInfo) getIntent().getSerializableExtra(NoticeMainActivity.INTENT_EXTRA_NOTICEINFO);
		if (noticeInfo != null)
		inflateNoticeData(noticeInfo);
	}

	/**
	 * 为各个字段填充数据
	 * @param noticeInfo
	 */
	private void inflateNoticeData(NoticeInfo noticeInfo) {
		tvStatus.setText(noticeInfo.displayStatusText());
		tvStatus.setTextColor(Color.parseColor(noticeInfo.displayStatusColor()));
		tvType.setText(noticeInfo.displayTypeText());
		tvTargetName.setText(noticeInfo.getTargetName());
		tvStartTime.setText(noticeInfo.getStartTime());
		tvEndTime.setText(noticeInfo.getEndTime());
		tvUpdateTime.setText(noticeInfo.getUpdateTime());
		tvSendUsername.setText(noticeInfo.getSendUserName());
		tvContent.setText(noticeInfo.getContent());
	}



}
