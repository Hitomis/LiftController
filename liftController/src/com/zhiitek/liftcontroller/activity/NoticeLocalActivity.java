package com.zhiitek.liftcontroller.activity;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.model.LocalInfo;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;

import java.util.List;

public class NoticeLocalActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ListView lvLocal;

    private List<LocalInfo> localInfoList;

    static final String CURR_CHOOSE_LOCAL_STR = "curr_choose_local_str";

    static final String CURR_CHOOSE_LOCAL_CODE = "curr_choose_local_code";

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_local);
    }

    @Override
    protected void findViewById() {
        lvLocal = (ListView) findViewById(R.id.lv_local);
    }

    @Override
    protected void setListener() {
        lvLocal.setOnItemClickListener(this);
    }

    @Override
    protected void dealProcessLogic() {
        SwipeFinishLayout.attachToActivity(this);
        localInfoList = (List<LocalInfo>) getIntent().getSerializableExtra(NoticeMainActivity.INTENT_EXTRA_LOCALINFO_LIFT);
        if (localInfoList == null || localInfoList.isEmpty()) return;
        String title = null;
        int layer = localInfoList.get(0).getLayer();
        if (layer == 1) { // 省份
            title = "省份选择";
        } else if (layer == 2) { // 市区
            title = "市区选择";
        } else { // 县区
            title = "县区选择";
        }
        setTitleBar(title, null);

        inflateListViewData();
    }

    /**
     * 省市区菜单填充数据
     */
    private void inflateListViewData() {
        BaseAdapterHelper<LocalInfo> localInfoBaseAdapterHelper = new BaseAdapterHelper<LocalInfo>(this, localInfoList, R.layout.item_notice_local) {
            @Override
            public void convert(ViewHolder viewHolder, LocalInfo item) {
                TextView tv = viewHolder.getView(R.id.tv_local);
                tv.setText(item.getName());
            }
        };
        lvLocal.setAdapter(localInfoBaseAdapterHelper);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LocalInfo localInfo = localInfoList.get(position);

        String chooseStr = getIntent().getStringExtra(CURR_CHOOSE_LOCAL_STR);
        String chooseLocalStr = (chooseStr == null ? "" : chooseStr) + localInfo.getName();

        if (localInfo.getLayer() < 3) {
            Intent intent = new Intent(this, NoticeLocalActivity.class);
            intent.putExtra(NoticeMainActivity.INTENT_EXTRA_LOCALINFO_LIFT, localInfo.getChildren());
            intent.putExtra(CURR_CHOOSE_LOCAL_STR, chooseLocalStr);
            startActivity(intent);
        } else {
            Intent rcIntent = new Intent(getPackageName() + NoticeAddActivity.BROADCAST_LOCAL);
            rcIntent.putExtra(CURR_CHOOSE_LOCAL_STR, chooseLocalStr);
            rcIntent.putExtra(CURR_CHOOSE_LOCAL_CODE, localInfo.getCode());
            sendBroadcast(rcIntent);

            Intent intent = new Intent(this, NoticeAddActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
