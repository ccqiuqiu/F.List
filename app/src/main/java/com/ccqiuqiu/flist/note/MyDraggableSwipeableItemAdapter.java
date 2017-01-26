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

package com.ccqiuqiu.flist.note;

import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.MainActivity;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.dao.BaseDataProvider;
import com.ccqiuqiu.flist.model.Note;
import com.ccqiuqiu.flist.utils.ColorUtils;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.ccqiuqiu.flist.utils.DrawableUtils;
import com.ccqiuqiu.flist.utils.ViewUtils;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

class MyDraggableSwipeableItemAdapter
        extends RecyclerView.Adapter<MyDraggableSwipeableItemAdapter.MyViewHolder>
        implements DraggableItemAdapter<MyDraggableSwipeableItemAdapter.MyViewHolder>,
        SwipeableItemAdapter<MyDraggableSwipeableItemAdapter.MyViewHolder> {
    private static final String TAG = "MyDSItemAdapter";

    private interface Draggable extends DraggableItemConstants {
    }
    private interface Swipeable extends SwipeableItemConstants {
    }

    private BaseDataProvider mProvider;
    private EventListener mEventListener;
    private View.OnClickListener mItemViewOnClickListener;
    private View.OnLongClickListener mItemViewOnLongClickListener;
    private View.OnClickListener mSwipeableViewContainerOnClickListener;
    public MainActivity mMainActivity;

    public interface EventListener {
        void onItemRemoved(int position);

        void onItemPinned(int position,boolean isPinned);

        void onItemViewClicked(View v, boolean pinned);

        void onItemViewLongClicked(View v);
    }

    //构造方法
    public MyDraggableSwipeableItemAdapter(BaseDataProvider dataProvider,MainActivity mainActivity) {
        mProvider = dataProvider;
        mMainActivity = mainActivity;
        //监听条目点击事件，此事件会传pinned=true
        mItemViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemViewClick(v);
            }
        };
        //监听条目点击事件，此事件会传pinned=false
        mSwipeableViewContainerOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeableViewContainerClick(v);
            }
        };
        mItemViewOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemViewLongClick(RecyclerViewAdapterUtils.getParentViewHolderItemView(v));
                return true;
            }
        };

        // DraggableItemAdapter and SwipeableItemAdapter require stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
    }

    //adapter的视图持有者
    public static class MyViewHolder extends AbstractDraggableSwipeableItemViewHolder {
        public FrameLayout mContainer;
        public ImageView mDragHandle;
        public ImageView mIvPinned;
        public TextView mTextView;

        public MyViewHolder(View v) {
            super(v);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            mDragHandle = (ImageView) v.findViewById(R.id.drag_handle);
            mTextView = (TextView) v.findViewById(android.R.id.text1);
            mIvPinned = (ImageView) v.findViewById(R.id.iv_pinned);
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

    //已经固定的条目点击时触发，已经固定的条目返回正常状态
    private void onItemViewClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(v, true); // true --- pinned

        }
    }
    //在可以滑动的条目点击时触发
    private void onSwipeableViewContainerClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v), false);  // false --- not pinned
        }
    }
    private void onItemViewLongClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewLongClicked(v);

        }
    }
    //
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
        //根据viewType填充不同的视图，viewType由数据提供者决定？
        final View v = inflater.inflate((viewType == 0) ? R.layout.list_item_draggable : R.layout.list_item2_draggable, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final BaseDataProvider.Data item = mProvider.getItem(position);

        // set listeners
        // (if the item is *not pinned*, click event comes to the itemView)
        holder.itemView.setOnClickListener(mItemViewOnClickListener);
        holder.mContainer.setOnLongClickListener(mItemViewOnLongClickListener);
        // (if the item is *pinned*, click event comes to the mContainer)
        holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);
        Note note = (Note) item.getmEntity();
        // set text
        holder.mTextView.setText(note.getTitle());
        if(TextUtils.isEmpty(note.getDesc())){
            holder.mDragHandle.setImageResource(R.drawable.ic_label_outline_black_24dp);
            holder.mDragHandle.setColorFilter(mMainActivity.getResources().getColor(R.color.bg_swipe_item_pinned));
        }else{
            holder.mDragHandle.setImageResource(R.drawable.ic_menu_note);
            holder.mDragHandle.setColorFilter(mMainActivity.getResources().getColor(R.color.bg_swipe_item_del));
        }
        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();
        final int swipeState = holder.getSwipeStateFlags();

        if (((dragState & Draggable.STATE_FLAG_IS_UPDATED) != 0) ||
                ((swipeState & Swipeable.STATE_FLAG_IS_UPDATED) != 0)) {
            int bgResId;

            if ((dragState & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
                //bgResId = R.drawable.bg_item_swiping_active_state;
                bgResId = ColorUtils.modifyAlpha(App.colorPrimary, 180);

                // need to clear drawable state here to get correct appearance of the dragging item.
                DrawableUtils.clearState(holder.mContainer.getForeground());
            } else if ((dragState & Draggable.STATE_FLAG_DRAGGING) != 0) {
                //bgResId = R.drawable.bg_item_swiping_state;
                bgResId = ColorUtils.modifyAlpha(App.colorPrimary, 50);
            }
//            else if ((swipeState & Swipeable.STATE_FLAG_IS_ACTIVE) != 0) {
//                bgResId = R.drawable.bg_item_swiping_active_state;
//            }
//            else if ((swipeState & Swipeable.STATE_FLAG_SWIPING) != 0) {
//                bgResId = R.drawable.bg_item_swiping_state;
//            }
            else {
                //bgResId = R.drawable.bg_item_normal_state;
                bgResId = Color.WHITE;
            }

            //holder.mContainer.setBackgroundResource(bgResId);
            holder.mContainer.setBackgroundColor(bgResId);

        }
        holder.mIvPinned.setColorFilter(mMainActivity.getResources().getColor(R.color.bg_swipe_item_pinned));
        if(note.getIsPinned()){
            //holder.mContainer.setBackgroundResource(R.drawable.bg_swipe_item_left_note2);
            holder.mIvPinned.setVisibility(View.VISIBLE);
        }else{
            holder.mIvPinned.setVisibility(View.GONE);
            //holder.mContainer.setBackgroundResource(R.drawable.bg_swipe_item_neutral);
        }
        // set swiping properties
//        holder.setSwipeItemHorizontalSlideAmount(
//                item.isPinned() ? Swipeable.OUTSIDE_OF_THE_WINDOW_LEFT : 0);
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
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        mProvider.moveItem(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    //开始拖动时执行
    public boolean onCheckCanStartDrag(MyViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        final View containerView = holder.mContainer;
        final View dragHandleView = holder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);
        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(MyViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    @Override
    //返回条目的滑动类型
    public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H;//垂直滑动
        } else {
            return Swipeable.REACTION_CAN_SWIPE_BOTH_H;//水平滑动
        }
    }

    @Override
    //设置滑动时背景
    public void onSetSwipeBackground(MyViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                if(mProvider.getItem(position).isPinned()){
                    bgRes = R.drawable.bg_swipe_item_left_note_unlock;
                }else{
                    bgRes = R.drawable.bg_swipe_item_left_note;
                }
                break;
            case Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right_note;
                break;
        }
        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    //条目滑动完成后执行，根据滑动返回的result，执行不同的操作
    public SwipeResultAction onSwipeItem(MyViewHolder holder, final int position, int result) {
        switch (result) {
            // swipe right
            case Swipeable.RESULT_SWIPED_RIGHT:
                return new SwipeRightResultAction(this, position);
//                if (mProvider.getItem(position).isPinned()) {
//                    // pinned --- back to default position
//                    return new UnpinResultAction(this, position);
//                } else {
//                    // not pinned --- remove
//                    return new SwipeRightResultAction(this, position);
//                }
                // swipe left -- pin
            case Swipeable.RESULT_SWIPED_LEFT:
                return new SwipeLeftResultAction(this, position,holder);
            // other --- do nothing
            case Swipeable.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    return new UnpinResultAction(this, position);
                } else {
                    return null;
                }
        }
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    //左滑结果处理逻辑
    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private MyDraggableSwipeableItemAdapter mAdapter;
        private final int mPosition;
        private boolean mSetPinned;
        private MyViewHolder mHolder;

        SwipeLeftResultAction(MyDraggableSwipeableItemAdapter adapter, int position,MyViewHolder holder) {
            mAdapter = adapter;
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            BaseDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
//            item.setPinned(!item.isPinned());
//            mAdapter.notifyItemChanged(mPosition);
            mSetPinned = !item.isPinned();
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();
            if (mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemPinned(mPosition,mSetPinned);

                mHolder.setSwipeItemHorizontalSlideAmount(0);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    //右滑结果处理逻辑
    private static class SwipeRightResultAction extends SwipeResultActionRemoveItem {
        private MyDraggableSwipeableItemAdapter mAdapter;
        private final int mPosition;

        SwipeRightResultAction(MyDraggableSwipeableItemAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();
            DbManager db = DbUtils.getDbManager();
            try {
                db.delete(mAdapter.mProvider.getItem(mPosition).getmEntity());
                mAdapter.mProvider.removeItem(mPosition);
                mAdapter.notifyItemRemoved(mPosition);

                mAdapter.mMainActivity.changeTipsView();

            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemRemoved(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    //没有滑动结果的处理逻辑
    private static class UnpinResultAction extends SwipeResultActionDefault {
        private MyDraggableSwipeableItemAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(MyDraggableSwipeableItemAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

//            BaseDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
//            if (item.isPinned()) {
//                item.setPinned(false);
//                mAdapter.notifyItemChanged(mPosition);
//            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }
}
