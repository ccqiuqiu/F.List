package com.ccqiuqiu.flist.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.BaseActivity;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.model.Category;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.ccqiuqiu.flist.utils.ViewUtils;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.kyleduo.switchbutton.SwitchButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class SettingsActivity extends BaseActivity implements ColorChooserDialog.ColorCallback {

    private String FRAGMENT_TAG_SETTING = "setting";
    private String FRAGMENT_TAG_CATEGORY_EDIT = "category edit";
    private Fragment mSettingFragment;
    private Fragment mCategoryEditFragment;

    private SharedPreferences mySharedPreferences;
    private Toolbar mToolbar;
    private View mSett_default_run;
    private TextView mSett_default_run_desc;

    private View mSett_pass;
    private TextView mSett_pass_desc;

//    private View mSett_save_category;
//    private SwitchButton mSett_save_category_md;
//    private TextView mSett_save_category_desc;

    private TextView mSett_category_manage_todo;
    private TextView mSett_category_manage_note;
    private RecyclerView mRecycler_view;
    private MaterialEditText mTitle;
    private Button mBtnAdd;

    private TextView mSett_about_ver;
    private CategoryEditAdapter mAdapter;
    private int mFlg;

    private View mSett_color_primary;
    private ImageView mSett_color_primary_flg;

    private View mSett_about_layout;
    private View mSett_color_accent;
    private ImageView mSett_color_accent_flg;

    private TextView mSett_help;

    public ArrayList<Integer> mDelCategoryIds;

    private LocationManager locationManager;
    private TextView mTv_jd_gps, mTv_wd_gps, mTv_gps, mTv_jd_network, mTv_wd_network, mTv_network, mTv_jz, mTv_jz_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        mySharedPreferences = getSharedPreferences("config", Activity.MODE_PRIVATE);

        mDelCategoryIds = new ArrayList<>();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            mSettingFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mSettingFragment, FRAGMENT_TAG_SETTING)
                    .commit();
        }

        //设置状态栏颜色和4.4下的透明
        ininStatusBar(mToolbar, false);
//        //修改toolbar文字颜色
//        mToolbar.setTitleTextColor(App.textColor);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //mySetResult();
            //finish();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            //移除监听器
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(gpsListener);
            locationManager.removeUpdates(networkListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationManager != null) {
            //移除监听器
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(gpsListener);
            locationManager.removeUpdates(networkListener);
        }
    }

    @Override
    public void onBackPressed() {
        mySetResult();
        super.onBackPressed();
    }

    public void mySetResult() {
        Intent resultData = new Intent();
        resultData.putIntegerArrayListExtra("delCategoryIds", mDelCategoryIds);
        setResult(0, resultData);
    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */
    LocationListener networkListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            //如果位置发生变化,重新显示
            mTv_jd_network.setText(location.getLongitude() + "");
            mTv_wd_network.setText(location.getLatitude() + "");
            mTv_network.setText("网络定位");
            if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(networkListener);

        }
    };

    LocationListener gpsListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            //如果位置发生变化,重新显示
            mTv_jd_gps.setText(location.getLongitude() + "");
            mTv_wd_gps.setText(location.getLatitude() + "");
            mTv_gps.setText("GPS定位");
            if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(gpsListener);

        }
    };


    public class SettingsFragment extends Fragment implements View.OnClickListener {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_settings, container, false);
            mSett_default_run = v.findViewById(R.id.sett_default_run);
            mSett_default_run_desc = (TextView) v.findViewById(R.id.sett_default_run_desc);
            mSett_default_run.setOnClickListener(this);

            mSett_pass = v.findViewById(R.id.sett_pass);
            mSett_pass_desc = (TextView) v.findViewById(R.id.sett_pass_desc);
            mSett_pass.setOnClickListener(this);

//            mSett_save_category = v.findViewById(R.id.sett_save_category);
//            mSett_save_category_desc = (TextView) v.findViewById(R.id.sett_save_category_desc);
//            mSett_save_category_md = (SwitchButton) v.findViewById(R.id.sett_save_category_md);
//            mSett_save_category_md.setTintColor(App.colorAccent);
//            mSett_save_category.setOnClickListener(this);
//            mSett_save_category_md.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    mySharedPreferences.edit().putBoolean("sett_save_category", mSett_save_category_md.isChecked()).commit();
//                    mSett_save_category_desc.setText(isChecked ? getResources().getString(R.string.sett_save_category_desc)
//                            : getResources().getString(R.string.sett_no_save_category_desc));
//                }
//            });

            mSett_category_manage_todo = (TextView) v.findViewById(R.id.sett_category_manage_todo);
            mSett_category_manage_todo.setOnClickListener(this);

            mSett_category_manage_note = (TextView) v.findViewById(R.id.sett_category_manage_note);
            mSett_category_manage_note.setOnClickListener(this);

            mSett_about_ver = (TextView) v.findViewById(R.id.sett_about_ver);

            mSett_help = (TextView) v.findViewById(R.id.sett_help);
            mSett_help.setOnClickListener(this);

            mSett_color_primary = v.findViewById(R.id.sett_color_primary);
            mSett_color_primary_flg = (ImageView) v.findViewById(R.id.sett_color_primary_flg);
            mSett_color_primary_flg.setColorFilter(App.colorPrimary);
            mSett_color_primary.setOnClickListener(this);

            mSett_color_accent = v.findViewById(R.id.sett_color_accent);
            mSett_color_accent_flg = (ImageView) v.findViewById(R.id.sett_color_accent_flg);
            mSett_color_accent_flg.setColorFilter(App.colorAccent);
            mSett_color_accent.setOnClickListener(this);

            mSett_about_layout = v.findViewById(R.id.sett_about_layout);

            mSett_about_layout.setOnClickListener(this);

            initData();
            return v;
        }

        private void initData() {
            int sett_default_run = mySharedPreferences.getInt("sett_default_run", 0);
            String text = getResources().getStringArray(R.array.category)[sett_default_run];
            mSett_default_run_desc.setText(text);

            int sett_pass = mySharedPreferences.getInt("sett_pass_flg", 0);
            String text2 = getResources().getStringArray(R.array.pass_flg)[sett_pass];
            mSett_pass_desc.setText(text2);

//            Boolean sett_save_category = mySharedPreferences.getBoolean("sett_save_category", true);
//            mSett_save_category_md.setCheckedImmediately(sett_save_category);
//            mSett_save_category_desc.setText(sett_save_category ? getResources().getString(R.string.sett_save_category_desc)
//                    : getResources().getString(R.string.sett_no_save_category_desc));

            mSett_about_ver.setText(getResources().getString(R.string.sett_about_ver) + getVerName());

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sett_default_run:
                    setDefaultRun();
                    break;
                case R.id.sett_pass:
                    setPassFlg();
                    break;
//                case R.id.sett_save_category:
//                    setSSaveCategory();
//                    break;
                case R.id.sett_category_manage_todo:
                    mFlg = 0;
                    showCategoryFragment();
                    break;
                case R.id.sett_category_manage_note:
                    mFlg = 1;
                    showCategoryFragment();
                    break;
                case R.id.sett_help:
                    showHelp();
                    break;
                case R.id.sett_color_primary:
                    showColorPrimary();
                    break;
                case R.id.sett_color_accent:
                    showColorAccent();
                    break;
                case R.id.sett_about_layout:
                    showAbout();
            }
        }

        long[] mHits = new long[5];

        private void showAbout() {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = System.currentTimeMillis();
            if (mHits[0] >= System.currentTimeMillis() - 1000) {
                MaterialDialog dialog = new MaterialDialog.Builder(SettingsActivity.this)
                        .title(R.string.sett_about)
                        .customView(R.layout.layout_about, true)
                        .show();
                mTv_jd_gps = (TextView) dialog.getCustomView().findViewById(R.id.tv_jd_gps);
                mTv_wd_gps = (TextView) dialog.getCustomView().findViewById(R.id.tv_wd_gps);
                mTv_gps = (TextView) dialog.getCustomView().findViewById(R.id.tv_gps);

                mTv_jd_network = (TextView) dialog.getCustomView().findViewById(R.id.tv_jd_network);
                mTv_wd_network = (TextView) dialog.getCustomView().findViewById(R.id.tv_wd_network);
                mTv_network = (TextView) dialog.getCustomView().findViewById(R.id.tv_network);

                mTv_jz = (TextView) dialog.getCustomView().findViewById(R.id.tv_jz);
                mTv_jz_info = (TextView) dialog.getCustomView().findViewById(R.id.tv_jz_info);
                //获取地理位置管理器
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                mTv_network.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //定位
                        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mTv_network.setText("定位中...");
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);
                    }
                });
                mTv_gps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //定位
                        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mTv_gps.setText("定位中...");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 2, 50, gpsListener);

                    }
                });
                //
                mTv_jz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                        String operator = manager.getNetworkOperator();
                        /**通过operator获取 MCC 和MNC */
                        int mcc = Integer.parseInt(operator.substring(0, 3));
                        int mnc = Integer.parseInt(operator.substring(3));

                        /**通过GsmCellLocation获取中国移动和联通 LAC 和cellID */
                        GsmCellLocation location = (GsmCellLocation) manager.getCellLocation();
                        int lac = location.getLac();
                        int cid = location.getCid();
                        mTv_jz_info.setText(mcc + "-" + mnc + "-" + lac + "-" + cid);
                    }
                });
            }
        }

        private void showColorAccent() {
            new ColorChooserDialog.Builder(SettingsActivity.this, R.string.sett_color_accent_title)
                    .titleSub(R.string.sett_color_accent_title)
                    .backButton(R.string.back)
                    .cancelButton(R.string.cancel)
                    .customButton(R.string.custom)
                    .doneButton(R.string.done)
                    .presetsButton(R.string.presets)
                    .accentMode(true)
                    .preselect(App.colorAccent)
                    .show();
        }

        private void showColorPrimary() {
            new ColorChooserDialog.Builder(SettingsActivity.this, R.string.sett_color_primary_desc)
                    .titleSub(R.string.sett_color_primary_desc)
                    .backButton(R.string.back)
                    .cancelButton(R.string.cancel)
                    .customButton(R.string.custom)
                    .doneButton(R.string.done)
                    .presetsButton(R.string.presets)
                    .preselect(App.colorPrimary)
                    .show();
        }

        private void showHelp() {
            new MaterialDialog.Builder(SettingsActivity.this)
                    .title(R.string.sett_help)
                    .customView(R.layout.layout_help, true)
                    .show();
        }

        private void showCategoryFragment() {
            if (mCategoryEditFragment == null) {
                mCategoryEditFragment = new CategoryEditFragment();
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (mCategoryEditFragment.isAdded()) {
                transaction.hide(mSettingFragment).show(mCategoryEditFragment)
                        .addToBackStack(null).commit();
            } else {
                transaction.hide(mSettingFragment).add(R.id.container, mCategoryEditFragment, FRAGMENT_TAG_CATEGORY_EDIT)
                        .addToBackStack(null).commit();
            }
        }
//
//        private void setSSaveCategory() {
//            mSett_save_category_md.toggle();
//        }

        private void setDefaultRun() {
            new MaterialDialog.Builder(SettingsActivity.this)
                    .title(R.string.default_nav)
                    .items(R.array.category)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            mSett_default_run_desc.setText(text);
                            mySharedPreferences.edit().putInt("sett_default_run", which).commit();

                        }
                    })
                    .show();
        }

        private void setPassFlg() {
            new MaterialDialog.Builder(SettingsActivity.this)
                    .title(R.string.sett_pass_lock)
                    .items(R.array.pass_flg)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            mSett_pass_desc.setText(text);
                            mySharedPreferences.edit().putInt("sett_pass_flg", which).commit();
                            App.mLockFlg = which;
                        }
                    })
                    .show();
        }
    }

    public class CategoryEditFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_categoryedit, container, false);
            mTitle = (MaterialEditText) v.findViewById(R.id.tv_title);
            mTitle.setBaseColor(App.colorPrimary);
            mBtnAdd = (Button) v.findViewById(R.id.btn_add);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                mBtnAdd.setBackgroundTintList(ViewUtils.getNavStateList(App.colorAccent, App.colorAccent));
            } else {
                mBtnAdd.setBackgroundResource(R.drawable.btn_selector_accent);
            }
            mRecycler_view = (RecyclerView) v.findViewById(R.id.recycler_view);
            mRecycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecycler_view.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
            mAdapter = new CategoryEditAdapter(mFlg, this);
            mRecycler_view.setAdapter(mAdapter);

            mBtnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = mTitle.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        return;
                    }
                    if (name.length() > 16) {
                        mTitle.setError(getResources().getString(R.string.err_length));
                        return;
                    }
                    DbManager db = DbUtils.getDbManager();
                    Category category = new Category();
                    category.setFlg(mFlg);
                    category.setAllowDel(true);
                    category.setName(name);
                    try {
                        db.saveBindingId(category);
                        mAdapter.getCategories().add(category);
                        mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
                        mRecycler_view.scrollToPosition(mAdapter.getItemCount() - 1);
                        mTitle.setText("");
                        Toast.makeText(getContext(), getResources().getString(R.string.add_success), Toast.LENGTH_SHORT);
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            });
            return v;
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }

    @Override
    public void onColorSelection(ColorChooserDialog dialog, int selectedColor) {

        if (dialog.isAccentMode()) {
            changeColorAccent(selectedColor);
            mSett_color_accent_flg.setColorFilter(App.colorAccent);
            //mSett_save_category_md.setTintColor(App.colorAccent);
        } else {
            changeColorPrimary(selectedColor);
            mSett_color_primary_flg.setColorFilter(App.colorPrimary);
            //修改toolbar文字颜色
            //mToolbar.setTitleTextColor(App.textColor);
        }
    }
}
