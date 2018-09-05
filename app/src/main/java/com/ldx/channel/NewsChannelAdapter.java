package com.ldx.channel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ldx.channel.lrucache.DiskCacheManager;

import java.util.List;

public class NewsChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IOnItemMoveListener {

    // my channel title
    public static final int TYPE_MY_CHANNEL_HEADER = 0;
    // my channel
    public static final int TYPE_MY = 1;
    // other channel title
    public static final int TYPE_OTHER_CHANNEL_HEADER = 2;
    // other channel
    public static final int TYPE_OTHER = 3;

    public static final int TYPE_ADDCHANNEL = 4;
    // header  1
    private static final int COUNT_PRE_MY_HEADER = 1;
    //private static final int COUNT_ADDCHANNEL_TAG = 1;
    private  int COUNT_ADDCHANNEL_TAG = 0;

    private static final int COUNT_PRE_OTHER_HEADER = 1;

    private static final long ANIM_TIME = 360L;
    private static final long SPACE_TIME = 100;

    private long startTime;
    private LayoutInflater mInflater;
    private ItemTouchHelper mItemTouchHelper;

    private boolean isEditMode = false;

    private List<KeywordsBean> mMyChannelItems, mOtherChannelItems;

    private OnMyChannelItemClickListener mChannelItemClickListener;
    private Handler delayHandler = new Handler();
    private Context mContext;
    private int mCurrentTab;

    public int getmCurrentTab() {
        return mCurrentTab;
    }

    public void setmCurrentTab(int mCurrentTab) {
        this.mCurrentTab = mCurrentTab;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        if (isEditMode != editMode){
            isEditMode = editMode;
            notifyItemRangeChanged(COUNT_PRE_MY_HEADER,mMyChannelItems.size());
        }

    }

    public void setShowAddItem(boolean show){
        int oldNum = COUNT_ADDCHANNEL_TAG;
        if (show){
            COUNT_ADDCHANNEL_TAG = 1;
        }else{
            COUNT_ADDCHANNEL_TAG = 0;
        }
        if (oldNum != COUNT_ADDCHANNEL_TAG){
            notifyDataSetChanged();
        }
    }

    public NewsChannelAdapter(Context context, ItemTouchHelper helper, List<KeywordsBean> mMyChannelItems, List<KeywordsBean> mOtherChannelItems, int currentTab) {
        this.mInflater = LayoutInflater.from(context);
        this.mItemTouchHelper = helper;
        this.mMyChannelItems = mMyChannelItems;
        this.mOtherChannelItems = mOtherChannelItems;
        mCurrentTab = currentTab + 1;
        if (mCurrentTab > mMyChannelItems.size()) {
            mCurrentTab = mMyChannelItems.size();
        }
        mContext = context;
//        isEditMode = true;
    }

    public List<KeywordsBean> getmMyChannelItems() {
        return mMyChannelItems;
    }

    public List<KeywordsBean> getmOtherChannelItems() {
        return mOtherChannelItems;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_MY_CHANNEL_HEADER;
        } else if (position == mMyChannelItems.size() + 1 + COUNT_ADDCHANNEL_TAG) {
            return TYPE_OTHER_CHANNEL_HEADER;
        } else if (position == mMyChannelItems.size() + COUNT_ADDCHANNEL_TAG && COUNT_ADDCHANNEL_TAG != 0) {
            return TYPE_ADDCHANNEL;
        } else if (position > 0 && position < mMyChannelItems.size() + 1) {
            return TYPE_MY;
        } else if (position >= (COUNT_PRE_MY_HEADER + COUNT_ADDCHANNEL_TAG + mMyChannelItems.size() + COUNT_PRE_OTHER_HEADER - 1)) {
            return TYPE_OTHER;
        } else {
            return TYPE_OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
            case TYPE_MY_CHANNEL_HEADER:
                view = mInflater.inflate(R.layout.channelpage_item_channel_my_header, parent, false);
                final MyChannelHeaderViewHolder holder = new MyChannelHeaderViewHolder(view);
                holder.rightTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setEditMode(!isEditMode);
                        if (isEditMode){
                            holder.rightTextView.setText(mContext.getResources().getText(R.string.text_finish_operation));
                        }else{
                            holder.rightTextView.setText(mContext.getResources().getText(R.string.text_edit_operation));
                        }
                    }
                });
                return holder;

            case TYPE_MY:
                view = mInflater.inflate(R.layout.channelpage_item_channel_my, parent, false);
                MyViewHolder myHolder = new MyViewHolder(parent, view);
                return myHolder;

            case TYPE_OTHER_CHANNEL_HEADER:
                view = mInflater.inflate(R.layout.channelpage_item_channel_other_header, parent, false);
                OtherChannelHeaderViewHolder otherChannelHeaderViewHolder = new OtherChannelHeaderViewHolder(view);
                return otherChannelHeaderViewHolder;

            case TYPE_ADDCHANNEL:
                view = mInflater.inflate(R.layout.channelpage_item_addchannel_my, parent, false);
                MyChannelAddViewHolder myChannelAddViewHolder = new MyChannelAddViewHolder(view);
                return myChannelAddViewHolder;
            case TYPE_OTHER:
                view = mInflater.inflate(R.layout.channelpage_item_channel_other, parent, false);
                final OtherViewHolder otherHolder = new OtherViewHolder(view);
                otherHolder.textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecyclerView recyclerView = ((RecyclerView) parent);
                        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                        int currentPosition = otherHolder.getAdapterPosition();
                        GridLayoutManager gridLayoutManager = ((GridLayoutManager) manager);
                        if (currentPosition == gridLayoutManager.findLastVisibleItemPosition()) {
                            moveOtherToMyWithDelay(otherHolder);
                        } else {
                            moveOtherToMy(otherHolder);
                        }
                    }
                });
                return otherHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            final MyViewHolder myHolder = (MyViewHolder) holder;

            myHolder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final int position = myHolder.getAdapterPosition();
                    if (position <= mMyChannelItems.size() && position >= 1) {
                        if (isEditMode && mMyChannelItems.size() > 1 && !(position == 1 && mMyChannelItems.size() == 1)) {
                            moveMyToOther(myHolder);
                        } else {
                            mChannelItemClickListener.onItemClick(v, position - COUNT_PRE_MY_HEADER, false);
                            if (mMyChannelItems.size() == 1) {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.channeledit_atleast_onekeywrods), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        notifyDataSetChanged();
                    }
                }
            });

            myHolder.textView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (isEditMode) {
                        switch (MotionEventCompat.getActionMasked(event)) {
                            case MotionEvent.ACTION_DOWN:
                                startTime = System.currentTimeMillis();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                    mItemTouchHelper.startDrag(myHolder);
                                }
                                break;
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP:
                                startTime = 0;
                                break;
                        }

                    }
                    return false;
                }

            });
            if (position == mCurrentTab) {
                myHolder.textView.setTextColor(Color.RED);
            } else {
                myHolder.textView.setTextColor(mContext.getResources().getColor(R.color.channelpage_itemtext_color));
            }
            myHolder.textView.setText(mMyChannelItems.get(position - COUNT_PRE_MY_HEADER).getName());
            if (isEditMode) {
                myHolder.imgEdit.setVisibility(View.VISIBLE);
            } else {
                myHolder.imgEdit.setVisibility(View.INVISIBLE);
            }

        } else if (holder instanceof OtherViewHolder) {

            ((OtherViewHolder) holder).textView.setText(mOtherChannelItems.get(position - mMyChannelItems.size() - COUNT_ADDCHANNEL_TAG - COUNT_PRE_OTHER_HEADER - 1).getName());

        } else if (holder instanceof MyChannelHeaderViewHolder) {
           /* if (isEditMode) {
                ((MyChannelHeaderViewHolder) holder).rightTextView.setVisibility(View.VISIBLE);
            } else {
                ((MyChannelHeaderViewHolder) holder).rightTextView.setVisibility(View.GONE);
            }*/
        } else if (holder instanceof OtherChannelHeaderViewHolder) {
            if (mOtherChannelItems.size() == 0) {
                ((OtherChannelHeaderViewHolder) holder).otherContainer.setVisibility(View.GONE);
            } else {
                ((OtherChannelHeaderViewHolder) holder).otherContainer.setVisibility(View.VISIBLE);
            }
        } else if (holder instanceof MyChannelAddViewHolder) {
            ((MyChannelAddViewHolder) holder).mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "add Channel", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMyChannelItems.size() + mOtherChannelItems.size() + COUNT_PRE_MY_HEADER + COUNT_PRE_OTHER_HEADER + COUNT_ADDCHANNEL_TAG;
    }

    private void startAnimation(RecyclerView recyclerView, final View currentView, float targetX, float targetY) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);

        if (mirrorView != null) {
            Animation animation = getTranslateAnimator(
                    targetX - currentView.getLeft(), targetY - currentView.getTop());
            currentView.setVisibility(View.INVISIBLE);
            mirrorView.startAnimation(animation);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    viewGroup.removeView(mirrorView);
                    if (currentView.getVisibility() == View.INVISIBLE) {
                        currentView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            notifyDataSetChanged();
        }

    }

    /**
     * my channel  to otherchannel
     *
     * @param myHolder
     */
    private void moveMyToOther(MyViewHolder myHolder) {
        try {
            final int position = myHolder.getAdapterPosition();
            int startPosition = position - 1;
            if (startPosition >= mMyChannelItems.size() || startPosition < 0) {
                notifyDataSetChanged();
                return;
            }

            KeywordsBean item = mMyChannelItems.get(startPosition);
            mMyChannelItems.remove(startPosition);
            mOtherChannelItems.add(item);
            // saveData();

            if (mCurrentTab == position) {
                if ((mMyChannelItems.size()) >= mCurrentTab) {

                } else {
                    if (mCurrentTab >= 2) {
                        mCurrentTab = mCurrentTab - 1;
                    } else {
                        mCurrentTab = 1;
                    }
                }

            } else if (mCurrentTab > position) {
                mCurrentTab = mCurrentTab - 1;

            } else if (mCurrentTab < position) {

            }

            notifyItemMoved(position, mMyChannelItems.size() + COUNT_ADDCHANNEL_TAG + COUNT_PRE_OTHER_HEADER + mOtherChannelItems.size() + COUNT_PRE_MY_HEADER - 1);
            notifyItemChanged(mMyChannelItems.size() + COUNT_ADDCHANNEL_TAG, 0);
            notifyItemChanged(mCurrentTab, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.d("moveMyToOther channel current tab:" + mCurrentTab);
    }

    /**
     * other channel to mychannel
     *
     * @param otherHolder
     */
    private void moveOtherToMy(OtherViewHolder otherHolder) {
        int position = processItemRemoveAdd(otherHolder);
        if (position == -1) {
            return;
        }
        notifyItemMoved(position, mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
        notifyItemChanged(mMyChannelItems.size() + 1);
    }

    private void moveOtherToMyWithDelay(OtherViewHolder otherHolder) {
        final int position = processItemRemoveAdd(otherHolder);
        if (position == -1) {
            return;
        }
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyItemMoved(position, mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
                notifyItemChanged(mMyChannelItems.size() + 1);
            }
        }, ANIM_TIME);
    }

    //move other to my
    private int processItemRemoveAdd(OtherViewHolder otherHolder) {
        int position = otherHolder.getAdapterPosition();
        //delete -1 -1 -1  two head and one add
        int startPosition = position - mMyChannelItems.size() - COUNT_PRE_MY_HEADER - COUNT_PRE_OTHER_HEADER - COUNT_ADDCHANNEL_TAG;
        if (startPosition > mOtherChannelItems.size() - 1 && !(startPosition > -1)) {
            return -1;
        }
        try {
            KeywordsBean item = mOtherChannelItems.get(startPosition);
            mOtherChannelItems.remove(startPosition);
            mMyChannelItems.add(item);
            // saveData();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return position;
    }

    public void saveData() {
        saveTabMenu();
    }

    private void saveTabMenu() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String datastr = "";
                    Gson gson = new Gson();
                    if (mMyChannelItems != null) {
                        datastr = gson.toJson(mMyChannelItems);
                    }
                    if (!TextUtils.isEmpty(datastr)) {
                        new DiskCacheManager(mContext, Constants.SELECTED_TAB_MENU_LIST).put(Constants.SELECTED_TAB_MENU_LIST, "");
                        new DiskCacheManager(mContext, Constants.SELECTED_TAB_MENU_LIST).put(Constants.SELECTED_TAB_MENU_LIST, datastr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private ImageView addMirrorView(ViewGroup parent, RecyclerView recyclerView, View view) {
        final ImageView mirrorView;
        try {
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(true);
            mirrorView = new ImageView(recyclerView.getContext());
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            mirrorView.setImageBitmap(bitmap);
            view.setDrawingCacheEnabled(false);
            int[] locations = new int[2];
            view.getLocationOnScreen(locations);
            int[] parenLocations = new int[2];
            recyclerView.getLocationOnScreen(parenLocations);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
            params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0);
            parent.addView(mirrorView, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return mirrorView;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        KeywordsBean item = mMyChannelItems.get(fromPosition - COUNT_PRE_MY_HEADER);
        if (mMyChannelItems.size() <= 1 || toPosition > mMyChannelItems.size()) return;

        mMyChannelItems.remove(fromPosition - COUNT_PRE_MY_HEADER);
        mMyChannelItems.add(toPosition - COUNT_PRE_MY_HEADER, item);

        if (fromPosition == mCurrentTab) {
            mCurrentTab = toPosition;
        } else if (toPosition == mCurrentTab) {
            if (Math.abs(fromPosition - toPosition) == 1) {
                mCurrentTab = fromPosition;
            } else if ((fromPosition - toPosition) > 1) {
                mCurrentTab = mCurrentTab + 1;
            } else if ((toPosition - fromPosition) > 1) {
                mCurrentTab = mCurrentTab - 1;
            }
        } else if ((toPosition < mCurrentTab && fromPosition > mCurrentTab)
                || (toPosition > mCurrentTab && fromPosition < mCurrentTab)) {
            if ((fromPosition - toPosition) > 1) {
                mCurrentTab = mCurrentTab + 1;
            } else if ((toPosition - fromPosition) > 1) {
                mCurrentTab = mCurrentTab - 1;
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        LogUtil.d("onItemMove channel current tab:" + mCurrentTab);
        //  saveData();
    }

    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY);
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    public void setOnMyChannelItemClickListener(OnMyChannelItemClickListener listener) {
        this.mChannelItemClickListener = listener;
    }

    public interface OnMyChannelItemClickListener {
        void onItemClick(View v, int position, boolean islong);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements IOnDragVHListener {
        private TextView textView;
        private ImageView imgEdit;
        private RecyclerView mParent;

        public MyViewHolder(ViewGroup parent, View itemView) {
            super(itemView);
            try {
                mParent = (RecyclerView) parent;
            } catch (Exception e) {
                e.printStackTrace();
            }
            textView = (TextView) itemView.findViewById(R.id.tv);
            imgEdit = (ImageView) itemView.findViewById(R.id.img_edit);
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemFinish() {

        }
    }

    class OtherViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public OtherViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
        }
    }

    class MyChannelHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView rightTextView;

        public MyChannelHeaderViewHolder(View itemView) {
            super(itemView);
            rightTextView = (TextView) itemView.findViewById(R.id.tv_btn_edit);
        }
    }

    class MyChannelAddViewHolder extends RecyclerView.ViewHolder {
        private ImageView addiv;
        private RelativeLayout mContainer;

        public MyChannelAddViewHolder(View itemView) {
            super(itemView);
            addiv = (ImageView) itemView.findViewById(R.id.addchannel);
            mContainer = (RelativeLayout) itemView.findViewById(R.id.addchannel_container);
        }
    }

    class OtherChannelHeaderViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout otherContainer;

        public OtherChannelHeaderViewHolder(View itemView) {
            super(itemView);
            otherContainer = (LinearLayout) itemView.findViewById(R.id.channelpage_otherchannelheader_tipcontainer);
        }
    }
}
