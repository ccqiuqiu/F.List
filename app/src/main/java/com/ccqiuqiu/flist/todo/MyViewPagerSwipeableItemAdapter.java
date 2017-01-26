/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ccqiuqiu.flist.todo;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ccqiuqiu.flist.MainActivity;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.dao.BaseDataProvider;
import com.ccqiuqiu.flist.dao.TodoDataProviderFragment;
import com.ccqiuqiu.flist.model.Todo;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

class MyViewPagerSwipeableItemAdapter
        extends RecyclerView.Adapter<MyViewPagerSwipeableItemAdapter.MyViewHolder>
        implements SwipeableItemAdapter<MyViewPagerSwipeableItemAdapter.MyViewHolder> {
    private static final String TAG = "MySwipeableItemAdapter";
    private EventListener mEventListener;

    // NOTE: Make accessible with short name
    private interface Swipeable extends SwipeableItemConstants {
    }

    public interface EventListener {
        void onLongClick(int position, BaseDataProvider.Data data);

        void onClick(int position, BaseDataProvider.Data data);
    }

    private BaseDataProvider mProvider;
    private boolean mCanSwipeLeft;
    public MainActivity mMainActivity;

    public static class MyViewHolder extends AbstractSwipeableItemViewHolder {
        public PagerSwipeItemFrameLayout mContainer;
        public TextView mTextView;
        public ImageView mPriority;

        public MyViewHolder(View v) {
            super(v);
            mContainer = (PagerSwipeItemFrameLayout) v.findViewById(R.id.container);
            mTextView = (TextView) v.findViewById(android.R.id.text1);
            mPriority = (ImageView) v.findViewById(R.id.iv_priority);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }

        @Override
        public void onSlideAmountUpdated(float horizontalAmount, float verticalAmount, boolean isSwiping) {
            float alpha = 1.0f - Math.min(Math.max(Math.abs(horizontalAmount), 0.0f), 1.0f);
            ViewCompat.setAlpha(mContainer, alpha);
        }

    }

    public MyViewPagerSwipeableItemAdapter(BaseDataProvider dataProvider, boolean canSwipeLeft, MainActivity mainActivity) {
        mProvider = dataProvider;
        mCanSwipeLeft = canSwipeLeft;
        mMainActivity = mainActivity;

        // SwipeableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return mProvider.getItem(position).getViewType();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_item_view_pager, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final BaseDataProvider.Data item = mProvider.getItem(position);
        holder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mEventListener.onLongClick(holder.getLayoutPosition(), item);
                return true;
            }
        });

        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventListener.onClick(holder.getLayoutPosition(), item);
            }
        });

        Todo todo = (Todo) item.getmEntity();
        holder.mContainer.setCanSwipeLeft(mCanSwipeLeft);
        holder.mContainer.setCanSwipeRight(!mCanSwipeLeft);
        holder.mTextView.setText(todo.getTitle());
        if (todo.getPriority() == 0) {
            //holder.mPriority.setColorFilter(Color.parseColor("#ffbdbdbd"));
            holder.mPriority.setColorFilter(mMainActivity.getResources().getColor(R.color.item_priority_0));
        } else if (todo.getPriority() == 1) {
            holder.mPriority.setColorFilter(mMainActivity.getResources().getColor(R.color.bg_swipe_item_pinned));
        } else if (todo.getPriority() == 2) {
            holder.mPriority.setColorFilter(mMainActivity.getResources().getColor(R.color.bg_swipe_item_del));
        }
    }

    @Override
    public void onViewRecycled(MyViewHolder holder) {
        super.onViewRecycled(holder);
        ViewCompat.setAlpha(holder.mContainer, 1.0f);
    }

    @Override
    public int getItemCount() {
        return mProvider.getCount();
    }

    @Override
    public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
        // NOTE: Need to specify REACTION_MASK_START_xxx flags to make ViewPager can handle touch event.
        if (mCanSwipeLeft) {
            return Swipeable.REACTION_CAN_SWIPE_LEFT |
                    Swipeable.REACTION_CAN_NOT_SWIPE_RIGHT_WITH_RUBBER_BAND_EFFECT |
                    Swipeable.REACTION_MASK_START_SWIPE_RIGHT;
        } else {
            return Swipeable.REACTION_CAN_SWIPE_RIGHT |
                    Swipeable.REACTION_CAN_NOT_SWIPE_LEFT_WITH_RUBBER_BAND_EFFECT |
                    Swipeable.REACTION_MASK_START_SWIPE_LEFT;
        }
    }

    @Override
    public void onSetSwipeBackground(MyViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left_todo;
                break;
            case Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right_todo;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(MyViewHolder holder, final int position, int result) {
        //Log.d(TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");

        if (position == RecyclerView.NO_POSITION) {
            return null;
        }

        if ((mCanSwipeLeft && result == Swipeable.RESULT_SWIPED_LEFT) ||
                (!mCanSwipeLeft && result == Swipeable.RESULT_SWIPED_RIGHT)) {
            return new DismissResultAction(this, position, result);
        } else {
            return new DefaultResultAction(this, position, result);
        }
    }

    private static class DismissResultAction extends SwipeResultActionRemoveItem {
        private MyViewPagerSwipeableItemAdapter mAdapter;
        private final int mPosition;
        private int mResult;

        DismissResultAction(MyViewPagerSwipeableItemAdapter adapter, int position, int result) {
            mAdapter = adapter;
            mPosition = position;
            mResult = result;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();
            try {
                mAdapter.mProvider.removeItem(mPosition);
                //更新数据库
                mAdapter.mProvider.mLastRemovedData.getText();
                DbManager db = DbUtils.getDbManager();

                Todo todo = (Todo) mAdapter.mProvider.mLastRemovedData.getmEntity();
                if (mResult == 4) {
                    todo.setStatus(1);

                    todo.setDoneTime(System.currentTimeMillis());

                    if (todo.getPriority() == 1) {
                        mAdapter.mProvider.priority_1--;
                    } else if (todo.getPriority() == 2) {
                        mAdapter.mProvider.priority_2--;
                    }

                } else {
                    todo.setStatus(0);
                    todo.setAddTime(System.currentTimeMillis());
                }

                db.update(todo);

                mAdapter.notifyItemRemoved(mPosition);
                //更新后台viewpager的数据
                int curPagePosition = mResult == 4 ? 0 : 1;

                RecyclerListViewPageFragment fragmentTo = mAdapter.mMainActivity.getRecyclerListViewPageFragment(1 - curPagePosition);

                ((TodoDataProviderFragment.TodoDataProvider) fragmentTo.getDataProvider()).reload(todo.getCategoryId(), 1 - curPagePosition);

                fragmentTo.mAdapter.notifyDataSetChanged();

                mAdapter.mMainActivity.changeTipsView();

            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    private static class DefaultResultAction extends SwipeResultActionDefault {
        private MyViewPagerSwipeableItemAdapter mAdapter;
        private final int mPosition;

        DefaultResultAction(MyViewPagerSwipeableItemAdapter adapter, int position, int result) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            BaseDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
            if (item.isPinned()) {
                item.setPinned(false);
                mAdapter.notifyItemChanged(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }
}
