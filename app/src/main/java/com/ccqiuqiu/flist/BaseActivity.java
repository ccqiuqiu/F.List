package com.ccqiuqiu.flist;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.ccqiuqiu.flist.utils.ViewUtils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by cc on 2015/11/30.
 */
public class BaseActivity extends AppCompatActivity {

    private SharedPreferences mySharedPreferences;
    private View mToolbarTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initColor();
    }

    private void initColor() {
        mySharedPreferences= getSharedPreferences("config", Activity.MODE_PRIVATE);
        App.colorPrimary = mySharedPreferences.getInt("colorPrimary",Color.parseColor("#3F51B5"));
        App.colorPrimaryDark = mySharedPreferences.getInt("colorPrimaryDark", Color.parseColor("#303F9F"));
        App.colorAccent = mySharedPreferences.getInt("colorAccent",Color.parseColor("#FF4081"));
        //更改toolbar文字颜色，避免在亮色背景上看不见文字
//        App.textColor = ColorUtils.getTextColorForBackground(App.colorPrimary, 10);
//        App.textColor2 = CircleView.shiftColor(App.textColor, 0.9f);


        ThemeSingleton.get().positiveColor = DialogUtils.getActionTextStateList(this, App.colorAccent);
        ThemeSingleton.get().neutralColor = DialogUtils.getActionTextStateList(this, App.colorAccent);
        ThemeSingleton.get().negativeColor = DialogUtils.getActionTextStateList(this, App.colorAccent);
        ThemeSingleton.get().widgetColor = App.colorAccent;
    }

    @TargetApi(19)
    public void ininStatusBar(Toolbar toolbar,boolean sitsSystemWindows) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
            findViewById(R.id.root).setFitsSystemWindows(sitsSystemWindows);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            //tintManager.setStatusBarTintColor(colorPrimary);//通知栏颜色
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


            mToolbarTop = findViewById(R.id.toolbar_top);
            if(mToolbarTop != null){
                mToolbarTop.setBackgroundColor(App.colorPrimary);
                AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) mToolbarTop.getLayoutParams();
                layoutParams.height = ViewUtils.getStatusHeight(this);
                mToolbarTop.setLayoutParams(layoutParams);
            }

//            AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
//            layoutParams.height = ViewUtils.getStatusHeight(this) + ViewUtils.dp2px(this,56);
//            toolbar.setLayoutParams(layoutParams);
//            toolbar.setPadding(0,ViewUtils.getStatusHeight(this),0,0);
        }

        changeStausBarColor();
    }
    public String getVerName(){
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void changeColorPrimary(int color){
        App.colorPrimary = color;
        App.colorPrimaryDark = CircleView.shiftColorDown(color);

        //更改toolbar文字颜色，避免在亮色背景上看不见文字
//        App.textColor = ColorUtils.getTextColorForBackground(App.colorPrimary, 10);
//        App.textColor2 = CircleView.shiftColor(App.textColor, 0.9f);
        changeStausBarColor();

        mySharedPreferences.edit()
                .putInt("colorPrimary", App.colorPrimary)
                .putInt("colorPrimaryDark", App.colorPrimaryDark)
                .commit();
    }
    public void changeColorAccent(int color){
        App.colorAccent = color;
        ThemeSingleton.get().positiveColor = DialogUtils.getActionTextStateList(this, App.colorAccent);
        ThemeSingleton.get().neutralColor = DialogUtils.getActionTextStateList(this, App.colorAccent);
        ThemeSingleton.get().negativeColor = DialogUtils.getActionTextStateList(this, App.colorAccent);
        ThemeSingleton.get().widgetColor = App.colorAccent;
        mySharedPreferences.edit()
                .putInt("colorAccent", App.colorAccent)
                .commit();
    }
    public void changeStausBarColor(){
        if (getSupportActionBar() != null){
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(App.colorPrimary));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(CircleView.shiftColorDown(App.colorPrimary));
            getWindow().setNavigationBarColor(App.colorPrimary);
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
            if(mToolbarTop != null){
                mToolbarTop.setBackgroundColor(App.colorPrimary);
            }
        }
    }

}
