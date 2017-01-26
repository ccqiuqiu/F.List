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

import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ccqiuqiu.flist.MainActivity;
import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.dao.BaseDataProvider;
import com.ccqiuqiu.flist.model.Todo;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

public class RecyclerListViewPageFragment extends Fragment {
    private static final String ARG_DATA_PROVIDER = "";
    private static final String ARG_CAN_SWIPE_LEFT = "can swipe left";

    private RadioGroup mPriority;
    private MaterialEditText met_title;

    //创建viewpage需要的Fragment
    public static RecyclerListViewPageFragment newInstance(String dataProvider, boolean canSwipeLeft) {
        RecyclerListViewPageFragment fragment = new RecyclerListViewPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA_PROVIDER, dataProvider);
        args.putBoolean(ARG_CAN_SWIPE_LEFT, canSwipeLeft);
        fragment.setArguments(args);
        return fragment;
    }

    private String mDataProvider;
    private boolean mCanSwipeLeft;

    public RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    public RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    public RecyclerListViewPageFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataProvider = getArguments().getString(ARG_DATA_PROVIDER);
        mCanSwipeLeft = getArguments().getBoolean(ARG_CAN_SWIPE_LEFT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //填充Fragment的视图
        return inflater.inflate(R.layout.fragment_recycler_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final MyViewPagerSwipeableItemAdapter myItemAdapter =
                new MyViewPagerSwipeableItemAdapter(getDataProvider(), mCanSwipeLeft, (MainActivity) getActivity());

        myItemAdapter.setEventListener(new MyViewPagerSwipeableItemAdapter.EventListener() {
            @Override
            public void onLongClick(int position, BaseDataProvider.Data data) {
                delTodo(position, data);
            }

            @Override
            public void onClick(int position, BaseDataProvider.Data data) {
                editTodo(position, data);
            }
        });
        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);

        // for debugging
//        animator.setDebug(true);
//        animator.setMoveDuration(2000);
//        animator.setRemoveDuration(2000);
//        mRecyclerViewSwipeManager.setMoveToOutsideWindowAnimationDuration(2000);
//        mRecyclerViewSwipeManager.setReturnToDefaultPositionAnimationDuration(2000);
    }

    private void editTodo(final int position, BaseDataProvider.Data data) {
        final Todo todo = (Todo) data.getmEntity();
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.edit_todo)
                .customView(R.layout.dialog_add_todo, true)
                .positiveText(R.string.save_close)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (TextUtils.isEmpty(met_title.getText())) {
                            met_title.setError(getResources().getString(R.string.err_null));
                            return;
                        }
                        saveTodo(todo, position);
                        dialog.dismiss();
                    }
                })
                .show();
        mPriority = (RadioGroup) dialog.getCustomView().findViewById(R.id.rg_priority);
        met_title = (MaterialEditText) dialog.getCustomView().findViewById(R.id.met_title);
        met_title.setBaseColor(App.colorPrimary);
        if (todo.getPriority() == 0) {
            mPriority.check(R.id.rb_priority_0);
        } else if (todo.getPriority() == 1) {
            mPriority.check(R.id.rb_priority_1);
        } else {
            mPriority.check(R.id.rb_priority_2);
        }

        met_title.setText(todo.getTitle());
        met_title.setBaseColor(App.colorPrimary);

    }

    private void saveTodo(Todo todo, int position) {
        todo.setTitle(met_title.getText().toString());
        int id = mPriority.getCheckedRadioButtonId();
        switch (id) {
            case R.id.rb_priority_0:
                todo.setPriority(0);
                break;
            case R.id.rb_priority_1:
                todo.setPriority(1);
                break;
            case R.id.rb_priority_2:
                todo.setPriority(2);
                break;
        }
        DbManager db = DbUtils.getDbManager();
        try {
            db.update(todo);
            mAdapter.notifyItemChanged(position);

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public BaseDataProvider getDataProvider() {
        return ((MainActivity) getActivity()).getDataProvider(mDataProvider);
    }

    public void notifyItemChanged(int position) {
        mAdapter.notifyItemChanged(position);
    }

    public void notifyItemInserted(int position) {
        mAdapter.notifyItemInserted(position);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(position);
    }

    private void delTodo(final int position, final BaseDataProvider.Data data) {
        if (getDataProvider() == null || getDataProvider().mData == null || getDataProvider().mData.size() == 0)
            return;
        new MaterialDialog.Builder(getActivity())
                .title(R.string.del_qr)
                .content(R.string.del_qr_content)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DbManager db = DbUtils.getDbManager();
                        Todo todo = (Todo) data.getmEntity();
                        try {
                            db.delete(todo);
                            getDataProvider().removeItem(position);
                            mAdapter.notifyItemRemoved(position);

                            ((MainActivity) getActivity()).changeTipsView();
                            //mAdapter.notifyDataSetChanged();
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }
}