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

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.MainActivity;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.dao.BaseDataProvider;
import com.ccqiuqiu.flist.dao.NoteDataProviderFragment;
import com.ccqiuqiu.flist.dao.NoteUtil;
import com.ccqiuqiu.flist.model.Note;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.ccqiuqiu.flist.utils.ViewUtils;
import com.ccqiuqiu.flist.view.BaseFragment;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

/**
 * 显示数据的 Fragment
 */
public class NoteFragment extends BaseFragment {
    public RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    public RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    public NoteFragment() {
        super();
    }

    private MaterialEditText met_title;
    private MaterialEditText met_content;
    private TextView tv_title,tv_content;
    private View view_title,view_content;

    //Fragment创建时执行
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).changeTipsView();
        //填充布局文件，这个布局文件就一个RecyclerView
        return inflater.inflate(R.layout.fragment_recycler_list_view, container, false);
    }

    //Fragment所在的view创建完成后执行
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //noinspection ConstantConditions
        //取到RecyclerView对象
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        //新建一个布局管理器
        mLayoutManager = new LinearLayoutManager(getContext());

        //创建触摸管理器
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        //创建拖动管理器
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        //设置条目拖动时候的阴影
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z3));

        //滑动管理器
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //创建填充数据的adapter，并将数据传入adapter
        final MyDraggableSwipeableItemAdapter myItemAdapter = new MyDraggableSwipeableItemAdapter(getDataProvider(), (MainActivity) getActivity());
        //设置监听，并实现条目的完成、固定和点击事件，直接调用Activity里面的相应事件
        myItemAdapter.setEventListener(new MyDraggableSwipeableItemAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                onItemRemoved_f(position);
            }

            @Override
            public void onItemPinned(int position, boolean isPinned) {
                onItemPinned_f(position, isPinned);
            }

            @Override
            public void onItemViewClicked(View v, boolean pinned) {
                onItemViewClick_f(v, pinned);
            }

            @Override
            public void onItemViewLongClicked(View v) {
                onItemViewLongClick_f(v);
            }
        });

        //将adapter复制给全局变量
        mAdapter = myItemAdapter;

        //给adapter包装拖动的逻辑
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);      // wrap for dragging
        //给adapter包装滑动的逻辑
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping

        //新建一个滑动动画
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        //将布局管理器给RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        //将adapter设置给RecyclerView，此时adapter的生命周期方法将执行
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            //给条目添加阴影艺术效果
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        //给条目添加分割效果艺术效果
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));

        //优先级: TouchActionGuard > Swipe > DragAndDrop
        //将触摸管理器附加到列表
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        // for debugging
//        animator.setDebug(true);
//        animator.setMoveDuration(2000);
//        animator.setRemoveDuration(2000);
//        mRecyclerViewSwipeManager.setMoveToOutsideWindowAnimationDuration(2000);
//        mRecyclerViewSwipeManager.setReturnToDefaultPositionAnimationDuration(2000);
    }

    //生命周期方法
    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    //生命周期方法
    @Override
    //fragment销毁时执行
    public void onDestroyView() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

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

    //条目Pinned时执行，弹出提示框
    public void onItemPinned_f(final int position, final boolean isPinned) {
        NoteDataProviderFragment.NoteDataProvider.Data data = getDataProvider().getItem(position);
        Note note = (Note) data.getmEntity();
        int id = note.getId();
        if (isPinned) {
            NoteUtil.NotePinned(getContext(), note, false);
            Toast.makeText(getContext(),
                    getResources().getString(R.string.message_item_pinned), Toast.LENGTH_SHORT).show();
        } else {
            NotificationManager mNotificationManager = (NotificationManager) getActivity()
                    .getSystemService(getActivity().NOTIFICATION_SERVICE);
            mNotificationManager.cancel(id);
            Toast.makeText(getContext(),
                    getResources().getString(R.string.message_item_unpinned), Toast.LENGTH_SHORT).show();
        }

        final Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(((MainActivity) getActivity()).FRAGMENT_TAG_NOTE);
        data.setPinned(isPinned);
        ((NoteFragment) fragment).notifyItemChanged(position);
        DbManager db = DbUtils.getDbManager();
        note.setIsPinned(isPinned);
        try {
            db.update(note);
        } catch (DbException e) {
            e.printStackTrace();
        }
//        String title = getResources().getString(R.string.dialog_message_item_pinned_title);
//        String content = getResources().getString(R.string.dialog_message_item_pinned);
//        if(!isPinned){
//            title = getResources().getString(R.string.dialog_message_item_unpinned_title);
//            content = getResources().getString(R.string.dialog_message_item_unpinned);
//        }
//        new MaterialDialog.Builder(getActivity())
//                .title(title)
//                .content(content)
//                .positiveText(R.string.confirm)
//                .negativeText(R.string.cancel)
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        onNotifyItemPinnedDialogDismissed(position, isPinned);
//                    }
//                })
//                .onNegative(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        onNotifyItemPinnedDialogDismissed(position, !isPinned);
//                    }
//                })
//                .cancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        onNotifyItemPinnedDialogDismissed(position, !isPinned);
//                    }
//                })
//                .show();
    }

    //条目完成时执行，弹出撤销提示
    public void onItemRemoved_f(int position) {
        Snackbar snackbar = Snackbar.make(
                getActivity().findViewById(R.id.container),
                R.string.snack_bar_text_item_removed,
                Snackbar.LENGTH_LONG);

        snackbar.setAction(R.string.snack_bar_action_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemUndoActionClicked();
            }
        });
        //snackbar.getView().setBackground();
        //snackbar.setActionTextColor(App.colorAccent);
        snackbar.show();
    }

    //当撤销完成动作时执行
    private void onItemUndoActionClicked() {
        int position = getDataProvider().undoLastRemoval();
        if (position >= 0) {
            final Fragment fragment = getActivity().getSupportFragmentManager()
                    .findFragmentByTag(((MainActivity) getActivity()).FRAGMENT_TAG_NOTE);
            ((NoteFragment) fragment).notifyItemInserted(position);
        }
        ((MainActivity) getActivity()).changeTipsView();
    }

    //条目被点击时触发
    private void onItemViewClick_f(View v, boolean pinned) {
        int position = mRecyclerView.getChildAdapterPosition(v);
        if (position != RecyclerView.NO_POSITION) {
            //final Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(((MainActivity) getActivity()).FRAGMENT_TAG_NOTE);
            BaseDataProvider.Data data = getDataProvider().getItem(position);
            final Note note = (Note) data.getmEntity();
            //if(TextUtils.isEmpty(note.getDesc()))return;
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .customView(R.layout.dialog_view_note, true)
                    .neutralText(getString(R.string.drawer_menu_share))
                    .positiveText(R.string.close)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getText(R.string.drawer_menu_share));
                            intent.putExtra(Intent.EXTRA_TEXT, note.toString());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
                        }
                    })
                    .show();
            tv_title = (TextView) dialog.getCustomView().findViewById(R.id.tv_title);
            tv_content = (TextView) dialog.getCustomView().findViewById(R.id.tv_content);
            view_title = dialog.getCustomView().findViewById(R.id.layout_title);
            view_content = dialog.getCustomView().findViewById(R.id.layout_content);
            tv_title.setText(note.getTitle());
            if(!TextUtils.isEmpty(note.getDesc())){
                tv_content.setText(note.getDesc());
                tv_content.setVisibility(View.VISIBLE);
            }else{
                tv_content.setVisibility(View.GONE);
            }
            view_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewUtils.copyToClipboard(getContext(), note.getTitle());
                    ViewUtils.toast(getString(R.string.title) + getString(R.string.copyed));
                }
            });
            view_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewUtils.copyToClipboard(getContext(), note.getDesc());
                    ViewUtils.toast(getString(R.string.content) + getString(R.string.copyed));
                }
            });

        }
    }

    //条目长按时触发
    private void onItemViewLongClick_f(View v) {
        final int position = mRecyclerView.getChildAdapterPosition(v);
        final BaseDataProvider.Data data = getDataProvider().getItem(position);
        final Note note = (Note) data.getmEntity();
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.edit_note)
                .customView(R.layout.dialog_add_note, true)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (TextUtils.isEmpty(met_title.getText())) {
                            met_title.setError(getResources().getString(R.string.err_null));
                            return;
                        }
                        DbManager db = DbUtils.getDbManager();
                        try {
                            note.setTitle(met_title.getText().toString());
                            if (!TextUtils.isEmpty(met_content.getText())) {
                                note.setDesc(met_content.getText().toString().trim());
                            }
                            db.update(note);
                            notifyItemChanged(position);

                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
        met_title = (MaterialEditText) dialog.getCustomView().findViewById(R.id.met_title);
        met_content = (MaterialEditText) dialog.getCustomView().findViewById(R.id.met_content);
        met_title.setText(note.getTitle());
        met_content.setText(note.getDesc());
        met_title.setBaseColor(App.colorPrimary);
        met_content.setBaseColor(App.colorPrimary);
    }

    //参看当前版本是否支持海拔Elevation，海拔是5.0后支持
    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    //获取数据提供者
    public BaseDataProvider getDataProvider() {
        return ((MainActivity) getActivity()).getDataProvider(((MainActivity) getActivity()).FRAGMENT_TAG_NOTE_DATA);
    }

    //条目状态改变时触发，如完成、固定
    public void notifyItemChanged(int position) {
        mAdapter.notifyItemChanged(position);
    }

    //新增一个条目时触发
    public void notifyItemInserted(int position) {
        mAdapter.notifyItemInserted(position);
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void onSearch(String string) {
        NoteFragment noteFragment = (NoteFragment)(mMainActivity.getSupportFragmentManager().findFragmentByTag(mMainActivity.FRAGMENT_TAG_NOTE));
        ((NoteDataProviderFragment.NoteDataProvider) noteFragment.getDataProvider()).reload(mMainActivity.mNoteCateGoryId,string);
        noteFragment.mAdapter.notifyDataSetChanged();
    }
}
