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

package com.ccqiuqiu.flist.passnote;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.MainActivity;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.dao.BaseDataProvider;
import com.ccqiuqiu.flist.dao.NoteDataProviderFragment;
import com.ccqiuqiu.flist.dao.PassNoteDataProviderFragment;
import com.ccqiuqiu.flist.model.Note;
import com.ccqiuqiu.flist.model.PassNote;
import com.ccqiuqiu.flist.model.Setting;
import com.ccqiuqiu.flist.note.NoteFragment;
import com.ccqiuqiu.flist.utils.AESUtils;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.ccqiuqiu.flist.utils.ViewUtils;
import com.ccqiuqiu.flist.view.BaseFragment;
import com.github.florent37.viewanimator.ViewAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.takwolf.android.lock9.Lock9View;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.List;

/**
 * 显示数据的 Fragment
 */
public class PassNoteFragment extends BaseFragment {
    public RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    public RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    public PassNoteFragment() {
        super();
    }

    private MaterialEditText met_title, met_username, met_password, met_desc;
    private TextView tv_username, tv_password, tv_desc;
    private View layout_username, layout_password, layout_desc;
    private MainActivity mainActivity;
    public com.takwolf.android.lock9.Lock9View mLock9View;
    private TextView mLockTitle;
    private Button mLockTitle2;
    private boolean isSetPass, isChangePass;
    private String mPassword;
    private int mTimes = 0;
    private DbManager db;
    private Setting mSetting;

    //Fragment创建时执行
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).changeTipsView();
        //填充布局文件，这个布局文件就一个RecyclerView
        return inflater.inflate(R.layout.fragment_recycler_list_view, container, false);
    }

    //Fragment所在的view创建完成后执行
    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();

        db = DbUtils.getDbManager();
        try {
            mSetting = db.selector(Setting.class).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        }
        mLock9View = (Lock9View) mainActivity.findViewById(R.id.lock_9_view);
        mLockTitle = (TextView) mainActivity.findViewById(R.id.tv_lock_title);
        mLockTitle2 = (Button) mainActivity.findViewById(R.id.tv_lock_title_2);
        mLockTitle.setTextColor(App.colorPrimary);
        if (mSetting == null) mSetting = new Setting();
        if (TextUtils.isEmpty(mSetting.getPassword())) {
            isSetPass = true;
            mLockTitle.setText(getString(R.string.set_pass_f));
            mLockTitle2.setText(getString(R.string.set_pass));
            mLockTitle2.setTextColor(Color.parseColor("#80000000"));
            mLockTitle2.setBackgroundColor(Color.TRANSPARENT);
            mLockTitle2.setEnabled(false);
        } else {
            isSetPass = false;
            App.setKey(mSetting.getPassword());
            mLockTitle.setText(getString(R.string.unlock));
        }
        mLockTitle2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChangePass = true;
                mTimes = 0;
                mLockTitle.setText(getString(R.string.change_pass));
                mLockTitle2.setText(getString(R.string.change_pass_1));
                ViewAnimator.animate(mLockTitle2).bounce().duration(500).start();
                mLockTitle2.setTextColor(Color.parseColor("#80000000"));
                mLockTitle2.setBackgroundColor(Color.TRANSPARENT);
                mLockTitle2.setEnabled(false);
            }
        });
        mLock9View = (Lock9View) mainActivity.findViewById(R.id.lock_9_view);
        mLock9View.setCallBack(new Lock9View.CallBack() {
            @Override
            public void onFinish(String password) {
                if (password.length() < 4) {
                    ViewUtils.toast(getString(R.string.err_pass_length));
                    return;
                }
                if (isSetPass) {//设置密码
                    if (mTimes == 0) {
                        mTimes = 1;
                        mPassword = password;
                        mLockTitle2.setText(getString(R.string.set_pass_0));
                        ViewAnimator.animate(mLockTitle2).bounce().duration(500).start();
                        //ViewUtils.toast(getString(R.string.set_pass_0));
                    } else {
                        if (!mPassword.equals(password)) {
                            mTimes = 0;
                            mPassword = "";
                            ViewUtils.toast(getString(R.string.set_pass_no_eq));
                            if (isChangePass) {
                                isChangePass = false;
                                isSetPass = false;
                                mTimes = 0;
                                mLockTitle.setText(getString(R.string.unlock));
                                mLockTitle2.setText(getString(R.string.change_pass));
                                mLockTitle2.setTextColor(Color.parseColor("#FFFFFF"));
                                mLockTitle2.setBackgroundResource(R.drawable.btn_selector_accent);
                                mLockTitle2.setEnabled(true);
                            } else {
                                mLockTitle2.setText(getString(R.string.set_pass));
                            }
                        } else {
                            try {
                                if (mSetting.getId() == 0) {
                                    mSetting.setPassword(AESUtils.encode(password));
                                    db.saveBindingId(mSetting);
                                    ViewUtils.toast(getString(R.string.set_pass_success));
                                } else {
                                    String newKey = App.cerateKey(password);
                                    db.getDatabase().beginTransaction();
                                    List<PassNote> passNotes = db.selector(PassNote.class).findAll();
                                    if (passNotes != null || passNotes.size() > 0) {
                                        for (PassNote passnote : passNotes) {
                                            passnote.upDate(newKey);
                                            db.update(passnote);
                                        }
                                    }
                                    mSetting.setPassword(AESUtils.encode(password));
                                    db.update(mSetting);
                                    db.getDatabase().setTransactionSuccessful();
                                    db.getDatabase().endTransaction();
                                    isChangePass = false;
                                    ViewUtils.toast(getString(R.string.change_pass_success));
                                    ((PassNoteDataProviderFragment.PassNoteDataProvider) getDataProvider()).reload();
                                    mAdapter.notifyDataSetChanged();
                                }
                                App.setKey(mSetting.getPassword());
                                mTimes = 0;
                                isSetPass = false;
                                mLockTitle.setText(getString(R.string.unlock));
                                mLockTitle2.setText(getString(R.string.change_pass));
                                mLockTitle2.setTextColor(Color.parseColor("#FFFFFF"));
                                mLockTitle2.setBackgroundResource(R.drawable.btn_selector_accent);
                                mLockTitle2.setEnabled(true);
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (isChangePass) {//修改密码
                    if (mSetting != null && AESUtils.encode(password).equals(mSetting.getPassword())) {
                        isSetPass = true;
                        mTimes = 0;
                        mLockTitle2.setText(getString(R.string.change_pass_2));
                        ViewAnimator.animate(mLockTitle2).bounce().duration(500).start();
                    } else {
                        mLockTitle.setText(getString(R.string.unlock));
                        mLockTitle2.setText(getString(R.string.change_pass));
                        mLockTitle2.setTextColor(Color.parseColor("#FFFFFF"));
                        mLockTitle2.setBackgroundResource(R.drawable.btn_selector_accent);
                        mLockTitle2.setEnabled(true);
                        ViewUtils.toast(getString(R.string.err_pass));
                    }
                } else {
                    if (mSetting != null && AESUtils.encode(password).equals(mSetting.getPassword())) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(mainActivity.mPassView, View.ALPHA, 1f, 0f);
                        animator.setDuration(300);
                        animator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mainActivity.mPassView.setVisibility(View.GONE);
                                mainActivity.unLock = true;
                                mainActivity.mPassView.setAlpha(1f);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        animator.start();
                    } else {
                        ViewUtils.toast(getString(R.string.err_pass));
                    }
                }
            }
        });

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
        final PassNoteAdapter myItemAdapter = new PassNoteAdapter(getDataProvider(), (MainActivity) getActivity());
        //设置监听，并实现条目的完成、固定和点击事件，直接调用Activity里面的相应事件
        myItemAdapter.setEventListener(new PassNoteAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                onItemRemoved_f(position);
            }

            @Override
            public void onItemPinned(int position, boolean isPinned) {
                if (mainActivity.unLock) {
                    onItemPinned_f(position, isPinned);
                }
            }

            @Override
            public void onItemViewClicked(View v, boolean pinned) {
                if (mainActivity.unLock) {
                    onItemViewClick_f(v, pinned);
                }
            }

            @Override
            public void onItemViewLongClicked(View v) {
                if (mainActivity.unLock) {
                    onItemViewLongClick_f(v);
                }
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
        if (isVisible() && App.mLockFlg == 0) {
            mainActivity.mPassView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.unLock = false;
                    mainActivity.mPassView.setVisibility(View.VISIBLE);
                }
            }, 500);
        }
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
//            Toast.makeText(getContext(),
//                    getResources().getString(R.string.message_item_pinned),Toast.LENGTH_SHORT).show();
        } else {
            NotificationManager mNotificationManager = (NotificationManager) getActivity()
                    .getSystemService(getActivity().NOTIFICATION_SERVICE);
            mNotificationManager.cancel(id);
//            Toast.makeText(getContext(),
//                    getResources().getString(R.string.message_item_unpinned),Toast.LENGTH_SHORT).show();
        }

        final Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(((MainActivity) getActivity()).FRAGMENT_TAG_PASSNOTE);
        data.setPinned(isPinned);
        ((PassNoteFragment) fragment).notifyItemChanged(position);
        DbManager db = DbUtils.getDbManager();
        note.setIsPinned(isPinned);
        try {
            db.update(note);
        } catch (DbException e) {
            e.printStackTrace();
        }
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
                    .findFragmentByTag(((MainActivity) getActivity()).FRAGMENT_TAG_PASSNOTE);
            ((PassNoteFragment) fragment).notifyItemInserted(position);
        }
        ((MainActivity) getActivity()).changeTipsView();
    }

    //条目被点击时触发
    private void onItemViewClick_f(View v, boolean pinned) {
        int position = mRecyclerView.getChildAdapterPosition(v);
        if (position != RecyclerView.NO_POSITION) {
            //final Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(((MainActivity) getActivity()).FRAGMENT_TAG_NOTE);
            BaseDataProvider.Data data = getDataProvider().getItem(position);
            final PassNote passNote = (PassNote) data.getmEntity();
            //if (TextUtils.isEmpty(note.getDesc())) return;
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    //.title(AESUtils.decode(passNote.getTitle(), App.key))
                    .title(passNote.getTitle())
                    .customView(R.layout.dialog_view_passnote, true)
                    .neutralText(getString(R.string.drawer_menu_share))
                    .positiveText(R.string.close)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getText(R.string.drawer_menu_share));
                            intent.putExtra(Intent.EXTRA_TEXT, passNote.toString());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
                        }
                    })
                    .show();
            tv_username = (TextView) dialog.getCustomView().findViewById(R.id.tv_username);
            tv_password = (TextView) dialog.getCustomView().findViewById(R.id.tv_password);
            tv_desc = (TextView) dialog.getCustomView().findViewById(R.id.tv_desc);
            layout_username = dialog.getCustomView().findViewById(R.id.layout_username);
            layout_password = dialog.getCustomView().findViewById(R.id.layout_password);
            layout_desc = dialog.getCustomView().findViewById(R.id.layout_desc);

            tv_username.setText(AESUtils.decode(passNote.getUserName(), App.key));
            tv_password.setText(AESUtils.decode(passNote.getPassWord(), App.key));

            if (!TextUtils.isEmpty(passNote.getDesc())) {
                layout_desc.setVisibility(View.VISIBLE);
                tv_desc.setText(AESUtils.decode(passNote.getDesc(), App.key));
                layout_desc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewUtils.copyToClipboard(getContext(), passNote.getDesc());
                        ViewUtils.toast(getString(R.string.desc) + getString(R.string.copyed));
                    }
                });
            } else {
                layout_desc.setVisibility(View.GONE);
            }
            layout_username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewUtils.copyToClipboard(getContext(), passNote.getUserName());
                    ViewUtils.toast(getString(R.string.username) + getString(R.string.copyed));
                }
            });
            layout_password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewUtils.copyToClipboard(getContext(), passNote.getPassWord());
                    ViewUtils.toast(getString(R.string.password) + getString(R.string.copyed));
                }
            });
        }
    }

    //条目长按时触发
    private void onItemViewLongClick_f(View v) {
        final int position = mRecyclerView.getChildAdapterPosition(v);
        final BaseDataProvider.Data data = getDataProvider().getItem(position);
        final PassNote passNote = (PassNote) data.getmEntity();
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.edit_passnote)
                .customView(R.layout.dialog_add_passnote, true)
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
                        if (TextUtils.isEmpty(met_username.getText())) {
                            met_username.setError(getResources().getString(R.string.err_null));
                            return;
                        }
                        if (TextUtils.isEmpty(met_password.getText())) {
                            met_password.setError(getResources().getString(R.string.err_null));
                        }
                        DbManager db = DbUtils.getDbManager();
                        try {
                            //passNote.setTitle(AESUtils.encode(met_title.getText().toString(), App.key));
                            passNote.setTitle(met_title.getText().toString());
                            passNote.setUserName(AESUtils.encode(met_username.getText().toString(), App.key));
                            passNote.setPassWord(AESUtils.encode(met_password.getText().toString(), App.key));
                            if (!TextUtils.isEmpty(met_desc.getText())) {
                                passNote.setDesc(AESUtils.encode(met_desc.getText().toString().trim(), App.key));
                            }
                            db.update(passNote);
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
        met_username = (MaterialEditText) dialog.getCustomView().findViewById(R.id.met_username);
        met_password = (MaterialEditText) dialog.getCustomView().findViewById(R.id.met_password);
        met_desc = (MaterialEditText) dialog.getCustomView().findViewById(R.id.met_desc);
        met_title.setBaseColor(App.colorPrimary);
        met_username.setBaseColor(App.colorPrimary);
        met_password.setBaseColor(App.colorPrimary);
        met_desc.setBaseColor(App.colorPrimary);

        //met_title.setText(AESUtils.decode(passNote.getTitle(), App.key));
        met_title.setText(passNote.getTitle());
        met_username.setText(AESUtils.decode(passNote.getUserName(), App.key));
        met_password.setText(AESUtils.decode(passNote.getPassWord(), App.key));
        if (!TextUtils.isEmpty(passNote.getDesc())) {
            met_desc.setText(AESUtils.decode(passNote.getDesc(), App.key));
        }
    }

    //参看当前版本是否支持海拔Elevation，海拔是5.0后支持
    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    //获取数据提供者
    public BaseDataProvider getDataProvider() {
        return ((MainActivity) getActivity()).getDataProvider(((MainActivity) getActivity()).FRAGMENT_TAG_PASSNOTE_DATA);
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
        PassNoteFragment passNoteFragment = (PassNoteFragment)(mMainActivity.getSupportFragmentManager().findFragmentByTag(mMainActivity.FRAGMENT_TAG_PASSNOTE));
        ((PassNoteDataProviderFragment.PassNoteDataProvider) passNoteFragment.getDataProvider()).reload(string);
        passNoteFragment.mAdapter.notifyDataSetChanged();
    }

}
