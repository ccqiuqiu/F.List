package com.ccqiuqiu.flist;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ccqiuqiu.flist.dao.BaseDataProvider;
import com.ccqiuqiu.flist.dao.BaseDataProviderFragment;
import com.ccqiuqiu.flist.dao.NoteDataProviderFragment;
import com.ccqiuqiu.flist.dao.PassNoteDataProviderFragment;
import com.ccqiuqiu.flist.dao.TodoDataProviderFragment;
import com.ccqiuqiu.flist.model.Category;
import com.ccqiuqiu.flist.model.Note;
import com.ccqiuqiu.flist.model.PassNote;
import com.ccqiuqiu.flist.model.Todo;
import com.ccqiuqiu.flist.note.NoteFragment;
import com.ccqiuqiu.flist.note.NoteReceiver;
import com.ccqiuqiu.flist.passnote.PassNoteFragment;
import com.ccqiuqiu.flist.settings.SettingsActivity;
import com.ccqiuqiu.flist.todo.MyPagerAdapter;
import com.ccqiuqiu.flist.todo.RecyclerListViewPageFragment;
import com.ccqiuqiu.flist.todo.TodoFragment;
import com.ccqiuqiu.flist.utils.AESUtils;
import com.ccqiuqiu.flist.utils.ColorUtils;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.ccqiuqiu.flist.utils.ViewUtils;
import com.ccqiuqiu.flist.view.BaseFragment;
import com.ccqiuqiu.flist.view.SampleSuggestionsBuilder;
import com.ccqiuqiu.flist.view.SimpleAnimationListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.cryse.widget.persistentsearch.PersistentSearchView;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.List;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String FRAGMENT_TAG_NOTE_DATA = "data provider";
    public static final String FRAGMENT_TAG_PASSNOTE_DATA = "pass data provider";
    public static final String FRAGMENT_TAG_NOTE = "list view_note";
    public static final String FRAGMENT_TAG_PASSNOTE = "list view_passnote";
    public static final String FRAGMENT_TAG_TODO = "list view_page";
    public static final String FRAGMENT_TAG_TODO_DATA1 = "data provider 1";
    public static final String FRAGMENT_TAG_TODO_DATA2 = "data provider 2";

    public int mFlg;//nav标识  0-todo  1-note
    private int mTodoCateGoryId;
    public int mNoteCateGoryId;
    private int mViewPagerPosition;
    private SharedPreferences mySharedPreferences;

    private BaseFragment mCurFragment = new BaseFragment();//当前显示的Fragment
    private TodoFragment mTodoFragment = new TodoFragment();
    private NoteFragment mNoteFragment = new NoteFragment();
    private PassNoteFragment mPassNoteFragment = new PassNoteFragment();
    private Toolbar mToolbar;
    public FloatingActionButton mFab;
    private NavigationView navigationView;
    private DrawerLayout mDrawer;
    private MaterialEditText met_title, met_username, met_password, met_desc;
    private MaterialEditText met_content;
    private RadioGroup mPriority;
    private List<Category> categories;
    private NoteReceiver noteReceiver;
    private LinearLayout mTipsView;
    private ImageView mTipImg;
    private TextView mTipsTv;
    public View mPassView;
    public boolean unLock;

    public PersistentSearchView mSearchView;
    private View mSearchTintView;
    public View mSearchMenuItem;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar相关代码
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        //fab按钮相关
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mPassView = findViewById(R.id.view_password);

        mFab.setOnClickListener(new fabClickListener());

        mFab.setDrawingCacheBackgroundColor(App.colorAccent);

        mFab.setBackgroundTintList(ViewUtils.getFabStateList(App.colorAccent));
        //抽屉
        mDrawer = (DrawerLayout) findViewById(R.id.root);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mTipsView = (LinearLayout) findViewById(R.id.tips);
        mTipsTv = (TextView) findViewById(R.id.tv_tips);
        mTipImg = (ImageView) findViewById(R.id.img_tips);

        mySharedPreferences = getSharedPreferences("config", Activity.MODE_PRIVATE);

        mTodoCateGoryId = 1;
        mNoteCateGoryId = 2;

        int sett_default_run = mySharedPreferences.getInt("sett_default_run", 0);

        //创建Fragment
        mNoteCateGoryId = mySharedPreferences.getInt("sett_category_1_id", 1);
        mTodoCateGoryId = mySharedPreferences.getInt("sett_category_0_id", 1);
        if (App.mLockFlg == 2) {
            unLock = true;
        }
        if (savedInstanceState == null) {
            if (sett_default_run == 3) {
                sett_default_run = mySharedPreferences.getInt("sett_last_flg", 0);
            }
            if (sett_default_run == 0) {
                openToDo();
            } else if (sett_default_run == 1) {
                openNote();
            } else if (sett_default_run == 2) {
                openPassNote();
            }
            navigationView.getMenu().getItem(mFlg).setChecked(true);
        }
        //注册广播
        noteReceiver = new NoteReceiver(this);
        registerReceiver(noteReceiver, new IntentFilter("com.cccqiuqiu.flist.note"));

        //设置状态栏颜色和4.4下的透明
        ininStatusBar(mToolbar, true);
        //修改抽屉选中菜单的颜色
        navigationView.setItemTextColor(ViewUtils.getNavStateList(App.colorPrimary, Color.parseColor("#FF1f1f1f")));
        navigationView.setItemIconTintList(ViewUtils.getNavStateList(App.colorPrimary, Color.parseColor("#ff6d6d6d")));

//        //修改toolbar文字颜色
//        mToolbar.setTitleTextColor(App.textColor);
//        mToolbar.setSubtitleTextColor(App.textColor2);
//        //toolbar图标;
//        mToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);

        mSearchView = (PersistentSearchView) findViewById(R.id.searchview);
        mSearchTintView = findViewById(R.id.view_search_tint);
        initSearch();
    }

    private void initSearch() {
        mSearchTintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.cancelEditing();
            }
        });
        mSearchView.setStartPositionFromMenuItem(mSearchMenuItem);
        mSearchView.setSuggestionBuilder(new SampleSuggestionsBuilder(this));
        mSearchView.setSearchListener(new PersistentSearchView.SearchListener() {
            @Override
            public void onSearchEditOpened() {
                mSearchTintView.setVisibility(View.VISIBLE);
                mSearchTintView.animate().alpha(1.0f).setDuration(300)
                        .setListener(new SimpleAnimationListener()).start();
            }

            @Override
            public void onSearchEditClosed() {
                mSearchTintView.animate().alpha(0.0f).setDuration(300)
                        .setListener(new SimpleAnimationListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mSearchTintView.setVisibility(View.GONE);
                            }
                        })
                        .start();
            }

            @Override
            public boolean onSearchEditBackPressed() {
                if (mSearchView.isEditing()) {
                    mSearchView.cancelEditing();
                    return true;
                }
                return false;
            }

            @Override
            public void onSearchExit() {
                mCurFragment.onSearchExit();
            }

            @Override
            public void onSearchTermChanged(String term) {
                mCurFragment.onSearchTermChanged(term);
            }

            @Override
            public void onSearch(String string) {
                mCurFragment.onSearch(string);
            }

            @Override
            public void onSearchCleared() {
                mCurFragment.onSearchCleared();
            }
        });
    }

    private void openToDo() {
        mPassView.setVisibility(View.GONE);
        mFlg = 0;
        changeFab(mViewPagerPosition);
        mToolbar.setTitle(R.string.drawer_menu_todo);
        //Toolbar.setSubtitle(mToolbar.getMenu().getItem(0).getTitle());
        //初始化todo页面的2个数据提供者Fragmen
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TODO_DATA1);
        if (fragment == null || !fragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(new TodoDataProviderFragment(mTodoCateGoryId, 0), FRAGMENT_TAG_TODO_DATA1)
                    .add(new TodoDataProviderFragment(mTodoCateGoryId, 1), FRAGMENT_TAG_TODO_DATA2)
                    .commit();
        }
        switchContent(mTodoFragment, FRAGMENT_TAG_TODO);
    }

    private void openNote() {
        mPassView.setVisibility(View.GONE);
        mFlg = 1;
        changeFab(0);
        mToolbar.setTitle(R.string.drawer_menu_note);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_NOTE_DATA);
        if (fragment == null || !fragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(new NoteDataProviderFragment(mNoteCateGoryId), FRAGMENT_TAG_NOTE_DATA)
                    .commit();
        }
        switchContent(mNoteFragment, FRAGMENT_TAG_NOTE);
    }

    private void openPassNote() {
        if (!unLock) {
            mPassView.setVisibility(View.VISIBLE);
        }
        mFlg = 2;
        changeFab(0);
        mToolbar.setTitle(R.string.drawer_menu_passnote);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_PASSNOTE_DATA);
        if (fragment == null || !fragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(new PassNoteDataProviderFragment(), FRAGMENT_TAG_PASSNOTE_DATA)
                    .commit();
        }
        switchContent(mPassNoteFragment, FRAGMENT_TAG_PASSNOTE);
    }

    //获取数据提供者
    public BaseDataProvider getDataProvider(String tag) {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return ((BaseDataProviderFragment) fragment).getDataProvider();
    }

    //以下是菜单、toolbar等的回调
    //返回按钮回调
    long[] mHits = new long[2];

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.root);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = System.currentTimeMillis();
            if (mHits[0] >= System.currentTimeMillis() - 1000) {
                super.onBackPressed();
            } else {
                Toast.makeText(this, getResources().getString(R.string.exit_qr), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //toolbar菜单创建回调
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        changeOptMenu(menu);

        return true;
    }

    private void changeOptMenu(Menu menu) {
        menu.clear();
        if (mFlg != 0) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        if (mFlg == 2) {
            mToolbar.setSubtitle("");
            return;
        }
        DbManager db = DbUtils.getDbManager();
        int selectIndex = 0;
        try {
            categories = db.selector(Category.class).where("flg", "=", mFlg).findAll();
            for (int i = 0; i < categories.size(); i++) {
                Category c = categories.get(i);
                menu.add(0, c.getId(), i, c.getName());
                if (mFlg == 0 && c.getId() == mTodoCateGoryId) {
                    selectIndex = i;
                }
                if (mFlg == 1 && c.getId() == mNoteCateGoryId) {
                    selectIndex = i;
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        mToolbar.setSubtitle(menu.getItem(selectIndex).getTitle());
    }

    //toolbar菜单选择回调
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            if (mFlg == 1 || mFlg == 2 && unLock == true){
                mSearchView.openSearch();
            }
            return true;
        } else {
            if (mFlg == 0) {
                mTodoCateGoryId = id;
                reTodoData(id);
            } else {
                mNoteCateGoryId = id;
                reNoteData(id);
            }
            mToolbar.setSubtitle(item.getTitle());

            saveCategoryId();
            changeTipsView();
        }
        return super.onOptionsItemSelected(item);
    }

    private void reTodoData(int cateGoryId) {
        TodoFragment todoFragment = (TodoFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TODO);
        RecyclerListViewPageFragment fragment1 = ((MyPagerAdapter) todoFragment.mViewPager.getAdapter())
                .getRecyclerListViewPageFragments().get(0);
        ((TodoDataProviderFragment.TodoDataProvider) fragment1.getDataProvider()).reload(cateGoryId, 0);
        fragment1.mAdapter.notifyDataSetChanged();

        RecyclerListViewPageFragment fragment2 = ((MyPagerAdapter) todoFragment.mViewPager.getAdapter())
                .getRecyclerListViewPageFragments().get(1);
        ((TodoDataProviderFragment.TodoDataProvider) fragment2.getDataProvider()).reload(cateGoryId, 1);
        fragment2.mAdapter.notifyDataSetChanged();
    }

    private void reNoteData(int cateGoryId) {
        NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_NOTE);
        ((NoteDataProviderFragment.NoteDataProvider) noteFragment.getDataProvider()).reload(cateGoryId);
        noteFragment.mAdapter.notifyDataSetChanged();
    }

    //左侧菜单选择回调
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_todo) {
            openToDo();
        } else if (id == R.id.nav_note) {
            openNote();
        } else if (id == R.id.nav_passnote) {
            openPassNote();
        } else if (id == R.id.nav_setting) {
            //startActivity(new Intent(this,SettingsActivity.class));
            startActivityForResult(new Intent(this, SettingsActivity.class), 0);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getText(R.string.drawer_menu_share));
            intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));

        } else if (id == R.id.nav_donate) {
            donate();
        }
        mDrawer.closeDrawer(GravityCompat.START);

        //更新OptionsMenu
        invalidateOptionsMenu();
        return true;
    }

    private void donate() {
        new MaterialDialog.Builder(this)
                .title(R.string.drawer_menu_donate)
                .content(R.string.donate)
                .positiveText(R.string.copy)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ViewUtils.copyToClipboard(MainActivity.this, "ccqiuqiu@vip.qq.com");
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.copyed), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    //切换显示的Fragment
    public void switchContent(BaseFragment to, String tab) {
        if (to != mCurFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            .setCustomAnimations(
//                    R.anim.fragment_slide_left_enter,
//                    R.anim.fragment_slide_left_exit,
//                    R.anim.fragment_slide_right_enter,
//                    R.anim.fragment_slide_right_exit);
            if (!to.isAdded()) {    // 先判断是否被add过
                transaction.hide(mCurFragment).add(R.id.container, to, tab).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(mCurFragment).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
            mCurFragment = to;

            changeTipsView();

            mySharedPreferences.edit().putInt("sett_last_flg", mFlg).commit();
        }
    }

    public RecyclerListViewPageFragment getRecyclerListViewPageFragment(int position) {
        TodoFragment todoFragment = (TodoFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TODO);
        RecyclerListViewPageFragment fragment = ((MyPagerAdapter) todoFragment.mViewPager.getAdapter())
                .getRecyclerListViewPageFragments().get(position);
        return fragment;
    }

    public void changeFab(int position) {
        float n = mFab.getHeight() + getResources().getDimension(R.dimen.fab_margin);
        if (mFlg == 0) {//todo页面
            if (position == 1) {
                mFab.animate().translationY(n).setDuration(200);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFab.setImageResource(R.drawable.ic_delete_black_24dp);
                        mFab.animate().translationY(0);
                    }
                }, 200);
            } else {
                if (mViewPagerPosition == 1) {
                    mFab.animate().translationY(n).setDuration(200);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mFab.setImageResource(R.drawable.ic_add_black_24dp);
                            mFab.animate().translationY(0);
                        }
                    }, 200);
                }
            }
            mViewPagerPosition = position;
        } else {//noto页面
            if (mViewPagerPosition == 1) {
                mFab.animate().translationY(n).setDuration(200);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFab.setImageResource(R.drawable.ic_add_black_24dp);
                        mFab.animate().translationY(0);
                    }
                }, 200);
            }
        }

    }

    //浮动按钮点击事件
    private class fabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            if (mFlg == 0 && mViewPagerPosition == 0) {
                addTodo();
            } else if (mFlg == 0 && mViewPagerPosition == 1) {
                delAllTodo();
            } else if (mFlg == 1) {
                addNote();
            } else {
                addPassNote();
            }
        }
    }

    private void addPassNote() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.add_passnote)
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
                            return;
                        }
                        savePassNote();
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
    }

    private void savePassNote() {
        DbManager db = DbUtils.getDbManager();
        try {
            PassNote maxOrderNote = db.selector(PassNote.class).orderBy("order", true).findFirst();
            int maxOrder = 0;
            if (maxOrderNote != null) {
                maxOrder = maxOrderNote.getOrder() + 1;
            }
            PassNote passnote = new PassNote();
            //passnote.setTitle(AESUtils.encode(met_title.getText().toString(), App.key));
            passnote.setTitle(met_title.getText().toString());
            passnote.setUserName(AESUtils.encode(met_username.getText().toString(), App.key));
            passnote.setPassWord(AESUtils.encode(met_password.getText().toString(), App.key));
            if (!TextUtils.isEmpty(met_desc.getText())) {
                passnote.setDesc(AESUtils.encode(met_desc.getText().toString().trim(), App.key));
            }
            passnote.setOrder(maxOrder);
            passnote.setIsPinned(false);
            passnote.setAddTime(System.currentTimeMillis());
            db.saveBindingId(passnote);

            PassNoteDataProviderFragment noteDataProviderFragment = (PassNoteDataProviderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_PASSNOTE_DATA);
            noteDataProviderFragment.addItem(passnote);
            PassNoteFragment passNoteFragment = (PassNoteFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_PASSNOTE);
            passNoteFragment.notifyItemInserted(0);
            passNoteFragment.mRecyclerView.scrollToPosition(0);

            changeTipsView();

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void addNote() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.add_note)
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
                        saveNote();
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
        met_title.setBaseColor(App.colorPrimary);
        met_content.setBaseColor(App.colorPrimary);
    }

    private void saveNote() {
        DbManager db = DbUtils.getDbManager();
        try {
            Note maxOrderNote = db.selector(Note.class).orderBy("order", true).findFirst();
            int maxOrder = 0;
            if (maxOrderNote != null) {
                maxOrder = maxOrderNote.getOrder() + 1;
            }
            Note note = new Note();
            note.setTitle(met_title.getText().toString());
            if (!TextUtils.isEmpty(met_content.getText())) {
                note.setDesc(met_content.getText().toString().trim());
            }
            note.setCategoryId(mNoteCateGoryId);
            note.setOrder(maxOrder);
            note.setIsPinned(false);
            note.setAddTime(System.currentTimeMillis());
            db.saveBindingId(note);

            NoteDataProviderFragment noteDataProviderFragment = (NoteDataProviderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_NOTE_DATA);
            noteDataProviderFragment.addItem(note);
            NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_NOTE);
            noteFragment.notifyItemInserted(0);
            noteFragment.mRecyclerView.scrollToPosition(0);

            changeTipsView();

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void delAllTodo() {
        new MaterialDialog.Builder(this)
                .title(R.string.del_all_title)
                .content(R.string.del_all_content)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final MaterialDialog progressDialog = new MaterialDialog.Builder(MainActivity.this)
                                .title(R.string.progress_title)
                                .content(R.string.please_wait)
                                .cancelable(false)
                                .progress(true, 0)
                                .show();
                        new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object[] params) {
                                DbManager db = DbUtils.getDbManager();
                                try {
                                    db.execNonQuery("delete from todo where categoryId = " + mTodoCateGoryId + " and status = 1");
                                } catch (DbException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                RecyclerListViewPageFragment recyclerListViewPageFragment = getRecyclerListViewPageFragment(1);
                                recyclerListViewPageFragment.getDataProvider().mData.clear();
                                recyclerListViewPageFragment.mAdapter.notifyDataSetChanged();
                                progressDialog.dismiss();
                                changeTipsView();
                            }
                        }.execute();
                    }
                })
                .show();
    }

    //添加todo
    private void addTodo() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.add_todo)
                .customView(R.layout.dialog_add_todo, true)
                .positiveText(R.string.save_close)
                .negativeText(R.string.save_goon)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (TextUtils.isEmpty(met_title.getText())) {
                            met_title.setError(getResources().getString(R.string.err_null));
                            return;
                        }
                        saveTodo();
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //InputUtils.HideKeyboard(met_title);
                        if (TextUtils.isEmpty(met_title.getText())) {
                            met_title.setError(getResources().getString(R.string.err_null));
                            return;
                        }
                        saveTodo();
                    }
                })
                .show();
        mPriority = (RadioGroup) dialog.getCustomView().findViewById(R.id.rg_priority);
        met_title = (MaterialEditText) dialog.getCustomView().findViewById(R.id.met_title);
        met_title.setBaseColor(App.colorPrimary);
        mPriority.check(R.id.rb_priority_0);
        //InputUtils.KeyBoard(met_title,true);

//        RadioButton rb = (RadioButton) dialog.getCustomView().findViewById(R.id.rb_priority_0);
//        rb.setButtonDrawable(R.drawable.settings_bg_selector);
    }

    private int saveTodo() {
        DbManager db = DbUtils.getDbManager();
        Todo todo = new Todo();
        todo.setStatus(0);
        todo.setTitle(met_title.getText().toString());
        todo.setCategoryId(mTodoCateGoryId);
        todo.setAddTime(System.currentTimeMillis());
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
        try {
            db.saveBindingId(todo);
            TodoDataProviderFragment todoDataProviderFragment = (TodoDataProviderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TODO_DATA1);
            int position = todoDataProviderFragment.addItem(todo);
            RecyclerListViewPageFragment recyclerListViewPageFragment = getRecyclerListViewPageFragment(0);
            recyclerListViewPageFragment.notifyItemInserted(position);
            recyclerListViewPageFragment.mRecyclerView.scrollToPosition(position);

            changeTipsView();

//            RecyclerListViewPageFragment recyclerListViewPageFragment = getRecyclerListViewPageFragment(0);
//            TodoDataProviderFragment.TodoDataProvider dataProvider = (TodoDataProviderFragment.TodoDataProvider) recyclerListViewPageFragment.getDataProvider();
//            dataProvider.reload(todo.getCategoryId(), 0);
//            recyclerListViewPageFragment.mAdapter.notifyDataSetChanged();
//            int position = dataProvider.getPosition(todo);
//            recyclerListViewPageFragment.mRecyclerView.scrollToPosition(position);
        } catch (DbException e) {
            e.printStackTrace();
        }
        met_title.setText("");
        return todo.getId();
    }

    private void saveCategoryId() {
        Boolean sett_save_category = mySharedPreferences.getBoolean("sett_save_category", true);
        if (sett_save_category) {
            SharedPreferences.Editor edit = mySharedPreferences.edit();
            edit.putInt("sett_category_0_id", mTodoCateGoryId);
            edit.putInt("sett_category_1_id", mNoteCateGoryId);
            edit.commit();
        }
    }

    public void reNote(int cateGoryId) {
        if (cateGoryId == mNoteCateGoryId) {
            NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_NOTE);
            if (noteFragment != null) {
                ((NoteDataProviderFragment.NoteDataProvider) noteFragment.getDataProvider()).reload(cateGoryId);
                noteFragment.mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(noteReceiver);
        super.onDestroy();
    }


    //    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//        navigationView.getMenu().getItem(mFlg).setChecked(true);
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (App.mLockFlg == 2) {
            unLock = true;
            mPassView.setVisibility(View.GONE);
        }
        navigationView.getMenu().getItem(mFlg).setChecked(true);
        changeStausBarColor();
        //修改抽屉选中菜单的颜色
        navigationView.setItemTextColor(ViewUtils.getNavStateList(App.colorPrimary, Color.parseColor("#FF1f1f1f")));
        navigationView.setItemIconTintList(ViewUtils.getNavStateList(App.colorPrimary, Color.parseColor("#ff6d6d6d")));
//        //修改toolbar文字颜色
//        mToolbar.setTitleTextColor(App.textColor);
//        mToolbar.setSubtitleTextColor(App.textColor2);
        if (mTodoFragment != null && mTodoFragment.mTabLayout != null) {
            mTodoFragment.mTabLayout.setSelectedTabIndicatorColor(App.colorAccent);
        }
        mFab.setBackgroundTintList(ViewUtils.getFabStateList(App.colorAccent));

        List<Integer> delCategoryIds = data.getIntegerArrayListExtra("delCategoryIds");
        if (delCategoryIds != null && delCategoryIds.size() > 0) {
            if (mFlg == 0 && delCategoryIds.contains(mTodoCateGoryId)) {
                reTodoData(1);
            }
            if (mFlg == 1 && delCategoryIds.contains(mNoteCateGoryId)) {
                reNoteData(2);
            }
        }
        //刷新分类
        changeOptMenu(mToolbar.getMenu());

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void changeTipsView() {
        BaseDataProviderFragment fragment;
        int imgId;
        String[] tips;
        if (mFlg == 0) {
            if (mViewPagerPosition == 0) {
                fragment = (BaseDataProviderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TODO_DATA1);
                tips = getResources().getStringArray(R.array.tips_todo_0);
                imgId = R.drawable.ic_xiaolian;
            } else {
                fragment = (BaseDataProviderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TODO_DATA2);
                tips = getResources().getStringArray(R.array.tips_todo_1);
                imgId = R.drawable.ic_info;
            }
        } else if (mFlg == 1) {
            fragment = (BaseDataProviderFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG_NOTE_DATA);
            tips = getResources().getStringArray(R.array.tips_note);
            imgId = R.drawable.ic_note;

        } else {
            fragment = (BaseDataProviderFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG_PASSNOTE_DATA);
            tips = getResources().getStringArray(R.array.tips_passnote);
            imgId = R.drawable.ic_dunpai;
        }
        if (fragment != null && fragment.getDataProvider().getCount() == 0) {
            int i = (int) (Math.random() * tips.length);

            int color = ColorUtils.modifyAlpha(App.colorPrimary, 100);
            mTipsTv.setTextColor(color);
            mTipImg.setColorFilter(color);
            String tip = tips[i];
            mTipImg.setImageResource(imgId);
            mTipsView.setVisibility(View.VISIBLE);
            mTipsTv.setText(tip);
        } else {
            mTipsView.setVisibility(View.GONE);
        }
    }
}
