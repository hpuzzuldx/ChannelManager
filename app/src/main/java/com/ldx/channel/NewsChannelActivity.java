package com.ldx.channel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;

import com.ldx.channel.util.RomUtil;
import com.ldx.channel.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

public class NewsChannelActivity extends AppCompatActivity {
    public static final String CURRENT_TAB = "currentTab";
    private RecyclerView recyclerView;
    private NewsChannelAdapter adapter;
    private List<KeywordsBean> enableItems;
    private List<KeywordsBean> disableItems;

    private ImageView mBackIcon;
    private int mCurrentPos;
    private boolean mChannelClick = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywords_manageredit);
        if (RomUtil.isLightStatusBarAvailable()){
            StatusBarUtil.setLightStatusBar(this, true);
            StatusBarUtil.setColor(this, getResources().getColor(R.color.white));
        }
        initViews();
        initEvent();
        initData();
    }

    private void initEvent() {
        if (mBackIcon != null) {
            mBackIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // finish();
                }
            });
        }
    }

    private void initData() {
        //KeywordsBean
        ArrayList<KeywordsBean> enitem1 = new ArrayList<>();
        KeywordsBean bean1 = new KeywordsBean(111, "车辆网");
        KeywordsBean bean2 = new KeywordsBean(112, "智能出行");
        KeywordsBean bean3 = new KeywordsBean(113, "自动驾驶");
        KeywordsBean bean4 = new KeywordsBean(114, "后市场");
        enitem1.add(bean1);
        enitem1.add(bean2);
        enitem1.add(bean3);
        enitem1.add(bean4);
        ArrayList<KeywordsBean> enitem2 = new ArrayList<>();
        KeywordsBean bean5 = new KeywordsBean(115, "车辆1");
        KeywordsBean bean6 = new KeywordsBean(116, "智能2");
        KeywordsBean bean7 = new KeywordsBean(117, "自动3");
        KeywordsBean bean8 = new KeywordsBean(118, "添加1");
        KeywordsBean bean9 = new KeywordsBean(119, "添加2");
        KeywordsBean bean10 = new KeywordsBean(120, "添加3");
        enitem2.add(bean5);
        enitem2.add(bean6);
        enitem2.add(bean7);
        enitem2.add(bean8);
        enitem2.add(bean9);
        enitem2.add(bean10);
        showNewsData(enitem1, enitem2);
    }

    protected void initViews() {
        try {
            mCurrentPos = getIntent().getIntExtra(CURRENT_TAB, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mBackIcon = (ImageView) findViewById(R.id.commonlayout_headtool_lefticon);
        mBackIcon.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public synchronized <T extends Comparable<T>> boolean compare(List<T> a, List<T> b) {
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i)))
                return false;
        }
        return true;
    }

    public void showNewsData(ArrayList<KeywordsBean> enitem, ArrayList<KeywordsBean> disitem) {
        enableItems = new ArrayList<KeywordsBean>();
        disableItems = new ArrayList<KeywordsBean>();

        enableItems.addAll(enitem);
        disableItems.addAll(disitem);

        GridContentLayoutManager manager = new GridContentLayoutManager(this, 3);
        recyclerView.setLayoutManager(manager);

        ItemDragHelperCallback callback = new ItemDragHelperCallback();
        final ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        adapter = new NewsChannelAdapter(this, helper, enableItems, disableItems, mCurrentPos);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = adapter.getItemViewType(position);
                return viewType == NewsChannelAdapter.TYPE_MY || viewType == NewsChannelAdapter.TYPE_ADDCHANNEL || viewType == NewsChannelAdapter.TYPE_OTHER ? 1 : 3;
            }
        });
        recyclerView.setAdapter(adapter);

        adapter.setOnMyChannelItemClickListener(new NewsChannelAdapter.OnMyChannelItemClickListener() {
            @Override
            public void onItemClick(View v, int position, boolean isedit) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(adapter!= null){
            adapter.saveData();
        }
    }
}




