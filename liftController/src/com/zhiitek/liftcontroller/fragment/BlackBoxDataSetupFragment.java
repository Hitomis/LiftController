package com.zhiitek.liftcontroller.fragment;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;
import com.zhiitek.liftcontroller.components.UdpSocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.model.BlackBoxCommand;
import com.zhiitek.liftcontroller.utils.AppUtil;
import com.zhiitek.liftcontroller.utils.BlackBoxUtil;
import com.zhiitek.liftcontroller.utils.DensityUtil;
import com.zhiitek.liftcontroller.views.CustomFormCellView;
import com.zhiitek.liftcontroller.views.CustomPromptDialog;

/**
 * 配置接口板参数
 * 
 * 配置步骤beginConfig -> saveConfig(多个参数时重复调用该步骤) -> endConfig
 * 
 * @author 
 *
 */
public class BlackBoxDataSetupFragment extends BaseFragment {
	
	private View parentView;
	
	private CustomFormCellView chooseConfigItem, saveConfig;
	
	private LinearLayout llLayout;
	
	private UdpSocketConnection udpconn;
	
	/**
	 * 配置项list的adapter
	 */
	private BaseAdapterHelper<BlackBoxCommand> adapterHelper;
	
	private int count;
	
	/** 填写的配置项内容 */
	private ArrayList<String> configValues = new ArrayList<String>();

	/** 所有需要配置的命令 */
	private ArrayList<BlackBoxCommand> commands = new ArrayList<BlackBoxCommand>();
	
	/** 选中的需要配置的命令集合 */
	private ArrayList<BlackBoxCommand> preSetupCommands = new ArrayList<BlackBoxCommand>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_blackbox_datasetup, container, false);
		return parentView;
	}
	
	/**
	 * 初始化所需配置的命令
	 */
	public void initData() {
		commands.clear();
		commands.add(new BlackBoxCommand(1, 32, "电梯编号", BlackBoxCommand.DATA_TYPE_ASCII));
		commands.add(new BlackBoxCommand(10, 1, "物理顶层", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(11, 1, "校准层", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(33, 1, "电梯自动返回楼层", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(12, 2, "正常速度(cm/s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(13, 1, "平层正常通过时间(ms)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(14, 2, "正常抖动周期(ms)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(15, 1, "人体传感延时(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(17, 1, "平层停止判断时间(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(18, 1, "层间停止判断时间(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(19, 1, "电梯空闲判断时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(20, 1, "电梯停用判定时间(hour)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(30, 1, "关门故障判定时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(31, 1, "开门故障判定时间(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(32, 1, "电梯困人判定时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(40, 1, "同一过性事件过滤去重复时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(41, 1, "同一状态性事件报告间隔时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(201, 4, "网络地址", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(202, 2, "网络端口", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(203, 4, "子网掩码", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(204, 4, "互连网关", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(205, 4, "域名服务器地址", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(206, 96, "远程管理服务器ip/域名", BlackBoxCommand.DATA_TYPE_ASCII));
		commands.add(new BlackBoxCommand(207, 2, "远程管理服务器端口", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(208, 2, "远程管理在线心跳间隔(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(0, 0, "《 配置全部 》", BlackBoxCommand.DATA_TYPE_INT));
	}

	@Override
	protected void findViewById() {
		chooseConfigItem = (CustomFormCellView) parentView.findViewById(R.id.black_box_datasetup_chooseconfig);
		saveConfig = (CustomFormCellView) parentView.findViewById(R.id.black_box_datasetup_saveconfig);
		llLayout = (LinearLayout) parentView.findViewById(R.id.ll_data_setup_content);
	}

	@Override
	protected void setListener() {
		chooseConfigItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showConfigItemDlg();
			}
		});
		saveConfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveConfig();
			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		initData();
		udpconn = new UdpSocketConnection(getActivity());
	}
	
	private BaseAdapterHelper<BlackBoxCommand> createAdapter() {
		return new BaseAdapterHelper<BlackBoxCommand>(mContext, commands, R.layout.item_blackbox_datasetup_item) {
			@Override
			public void convert(ViewHolder viewHolder, final BlackBoxCommand item) {
				viewHolder.setText(R.id.blackbox_datasetup_item_name, item.getName());
				CheckBox cb = viewHolder.getView(R.id.blackbox_datasetup_item_checkbox);
				//上下滑动list时，设置正确的勾选状态
				if (preSetupCommands.contains(item)) {
					cb.setChecked(true);
				} else {
					cb.setChecked(false);
				}
				cb.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (((CheckBox)v).isChecked()) {
							selectedItem(item);
						} else {
							disSelectedItem(item);
						}
					}
				});
			}
		};
	}
	
	/**
	 * 勾选中一个item
	 * @param item
	 */
	private void selectedItem(BlackBoxCommand item) {
		if (item.getNumber() == 0) {//勾选配置全部
			preSetupCommands.clear();
			preSetupCommands.addAll(commands);
			adapterHelper.notifyDataSetChanged();
		} else {
			if (!preSetupCommands.contains(item)) {
				preSetupCommands.add(item);
			}
		}
	}
	
	/**
	 * 去勾选一个item
	 * @param item
	 */
	private void disSelectedItem(BlackBoxCommand item) {
		if (item.getNumber() == 0) {//去勾选配置全部
			preSetupCommands.clear();
			adapterHelper.notifyDataSetChanged();
		} else {
			if (preSetupCommands.contains(item)) {
				preSetupCommands.remove(item);
			}
		}
	}
	
	/**
	 * 弹出选择配置项的dialog
	 */
	protected void showConfigItemDlg() {
		final ListView listView = new ListView(getActivity());
		adapterHelper = createAdapter();
		listView.setAdapter(adapterHelper);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CheckBox cb = ((CheckBox)view.findViewById(R.id.blackbox_datasetup_item_checkbox));
				BlackBoxCommand item = (BlackBoxCommand)listView.getAdapter().getItem(position);
				if (cb.isChecked()) {//点击item时，checkbox为选中状态，则取消该item并设置checkbox为去勾选状态
					disSelectedItem(item);
					cb.setChecked(false);
				} else {
					selectedItem(item);
					cb.setChecked(true);
				}
				
			}
		});
		CustomPromptDialog.Builder builder = new CustomPromptDialog.Builder(getActivity());
		builder.setTitle("选择配置项").setContentView(listView).setButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				addConfigView();
			}
		});
		builder.setCancelable(true);
		builder.setCanceledOnTouchOutside(true);
		builder.setContentViewHeight(DensityUtil.getScreenHeight(mContext)/2);
		CustomPromptDialog dlg = builder.create();
		dlg.show();
	}
	
	/**
	 * 根据选中的配置项,绘制view
	 */
	protected void addConfigView() {
		llLayout.removeAllViews();
		for (BlackBoxCommand blackBoxCommand : preSetupCommands) {
			if (blackBoxCommand.getNumber() != 0) {
				llLayout.addView(createCellView(blackBoxCommand));
				llLayout.addView(AppUtil.createDivider(getActivity()));
			}
		}
	}
	
	/**
	 * 创建一个配置项的view
	 * @param blackBoxCommand
	 * @return
	 */
	private CustomFormCellView createCellView(BlackBoxCommand blackBoxCommand) {
		CustomFormCellView cellView = new CustomFormCellView(getActivity());
		cellView.setInfoViewType(CustomFormCellView.TYPE_EDITTEXT);
		cellView.setTextViewAttributes(getActivity(), DensityUtil.dip2Px(getActivity(), 8), 3f, blackBoxCommand.getName(), getResources().getColor(R.color.text_color), DensityUtil.sp2px(getActivity(), 12f));
		cellView.setEdittextAttributes(getActivity(), 0, 7f, null, Color.BLACK, DensityUtil.sp2px(getActivity(), 12f), null);
		cellView.getInfoEditText().setTag(blackBoxCommand);
		if (BlackBoxUtil.isIpCommand(blackBoxCommand)) {//ip地址需要输入"."
			cellView.getInfoEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		} else if (blackBoxCommand.getNumber() == 206){//服务器地址需要输入字母
			cellView.getInfoEditText().setInputType(InputType.TYPE_CLASS_TEXT);
		} else {//其他配置项只需输入数字
			cellView.getInfoEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, DensityUtil.dip2Px(getActivity(), 40));
		cellView.setLayoutParams(layoutParams);
		return cellView;
	}
	
	/**
	 * 保存配置
	 */
	private void saveConfig() {
		configValues.clear();
		if (preSetupCommands.size() > 0) {
			String patternIp = "^[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}";
			for(BlackBoxCommand blackBoxCommand : preSetupCommands) {
				//遍历配置项edittext，获取配置value
				String config = ((EditText)parentView.findViewWithTag(blackBoxCommand)).getText().toString().trim();
				if (TextUtils.isEmpty(config)) {
					showToast("所有配置项都不能为空");
					return;
				} else if (BlackBoxUtil.isIpCommand(blackBoxCommand)) {
					if (config.matches(patternIp)) {
						configValues.add(config);
					} else {
						showToast("IP地址格式不正确");
						return;
					}
					
				} else {
					configValues.add(config);
				}
			}
			beginConfig();
		} else {
			showToast("没有配置项，无法保存");
		}
	}
	
	/**
	 * 启动配置模式
	 */
	private void beginConfig() {
		count = 0;
		showLoadingDlg();
		udpconn.post(BlackBoxUtil.makeSendCommand(0x02), new SocketDataCallback() {
			@Override
			public void onSuccess(byte[] result) {
				if (checkCmd(result, 0x02)) {
					if (BlackBoxUtil.getResponseCode(result) == BlackBoxUtil.SSE_SUCCESS) {
						//启动配置成功才能开始进行setConfig
						setConfig(preSetupCommands.get(count), configValues.get(count));
					} else {
						showToast("启动配置模式失败，请重新保存");
						dismissDialog(mProgressDialog);
					}
				}
			}
			
			@Override
			public void onFailure(int errorCode) {
				promptBlackBoxSocketonFailure("启动配置模式", errorCode);
				dismissDialog(mProgressDialog);
			}
		});
	}
	
	/**
	 * 配置单个value
	 * @param blackBoxCommand
	 * @param config
	 */
	private void setConfig(BlackBoxCommand blackBoxCommand, String config) {
		byte[] value = new byte[blackBoxCommand.getLength()];
		if (BlackBoxUtil.isIpCommand(blackBoxCommand)) {
			//ip地址以*.*.*.*形式输入，4个数字依次存入4个字节中
			String[] ipValue = config.split("\\.");
			for (int i = 0; i < blackBoxCommand.getLength(); i++) {
				value[i] = (byte) (Integer.valueOf(ipValue[i]) & 0x0ff);
			}
		} else {
			switch (blackBoxCommand.getDataType()) {
			case BlackBoxCommand.DATA_TYPE_INT:
				value = BlackBoxUtil.int2byteArray(Integer.valueOf(config), blackBoxCommand.getLength());
				break;
			case BlackBoxCommand.DATA_TYPE_ASCII://ascii串以'/0'结束
				value = BlackBoxUtil.byteMerger(config.getBytes(), new byte[] {0});
				break;
			}
		}
		udpconn.post(BlackBoxUtil.makeSendCommand(0x03, BlackBoxUtil.byteMerger(BlackBoxUtil.int2byteArray(blackBoxCommand.getNumber(), 2), value), value.length + 2), new SocketDataCallback() {
			@Override
			public void onSuccess(byte[] result) {
				//本次配置得到回应后,才能进行下一个配置
				nextSetCommand();
			}
			
			@Override
			public void onFailure(int errorCode) {
				//本次配置得到回应后,才能进行下一个配置
				nextSetCommand();
			}
		});
	}
	
	/**
	 * 下一个set命令
	 */
	protected void nextSetCommand() {
		count++;
		if (count == preSetupCommands.size()) {//所有选择配置项都配置完毕
			endConfig();
		} else {
			setConfig(preSetupCommands.get(count), configValues.get(count));
		}
	}

	/**
	 * 结束配置模式
	 */
	private void endConfig() {
		udpconn.post(BlackBoxUtil.makeSendCommand(0x04, BlackBoxUtil.int2byteArray(1, 1), 1), new SocketDataCallback() {
			@Override
			public void onSuccess(byte[] result) {
				if (0x04 == BlackBoxUtil.getResponseCmd(result)) {
					if (BlackBoxUtil.SSE_SUCCESS == BlackBoxUtil.getResponseCode(result)) {
						//配置过程成功终止，保存成功
						dismissDialog(mProgressDialog);
						showToast("保存配置成功");
						AppUtil.saveQueryConfigFlag(getActivity(), true);
					} else if (BlackBoxUtil.SSE_SUCCESS < BlackBoxUtil.getResponseCode(result)){
						//配置过程成功终止，但保存失败，配置修改丢失
						dismissDialog(mProgressDialog);
						showToast("保存配置失败");
					}
				}
			}
			
			@Override
			public void onFailure(int errorCode) {
				//结束配置模式失败则继续结束,否则接口板处于config状态,无法接受其他命令
				endConfig();
			}
		});
	}

	public static BlackBoxDataSetupFragment newInstance() {
		return new BlackBoxDataSetupFragment();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		udpconn.shutdown();
		preSetupCommands.clear();
	}
}
