package com.ccqiuqiu.flist;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.ccqiuqiu.flist.dao.NoteUtil;
import com.ccqiuqiu.flist.model.Category;
import com.ccqiuqiu.flist.model.Setting;
import com.ccqiuqiu.flist.utils.AESUtils;
import com.ccqiuqiu.flist.utils.DbUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.security.Key;

public class App extends Application {

    private static Context context;

    public static int colorPrimary;
    public static int colorPrimaryDark;
    public static int colorAccent;
    public static int textColor;
    public static int textColor2;
    public static String key;
    public static int mLockFlg;


    @Override
    public void onCreate() {
        super.onCreate();
        //
        x.Ext.init(this);
        //x.Ext.setDebug(BuildConfig.DEBUG);

        context = this;

        //第一次运行初始化数据库
        SharedPreferences mySharedPreferences = getSharedPreferences("config", Activity.MODE_PRIVATE);
        Boolean firstRun = mySharedPreferences.getBoolean("first_run", true);
        mLockFlg = mySharedPreferences.getInt("sett_pass_flg", 0);
        if (firstRun) {
            try {
                DbManager db = DbUtils.getDbManager();
                //新建todo的默认分类
                Category todoCategory = new Category();
                todoCategory.setAllowDel(false);
                todoCategory.setFlg(0);
                todoCategory.setName("工作安排");
                db.saveBindingId(todoCategory);

                //新建note默认分类
                Category noteCategory = new Category();
                noteCategory.setAllowDel(false);
                noteCategory.setFlg(1);
                noteCategory.setName("我的备忘");
                db.saveBindingId(noteCategory);

            } catch (DbException e) {
                e.printStackTrace();
            }

            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putBoolean("first_run", false);
            editor.commit();
        }

        //将pinned的note发送到通知栏
        NoteUtil.NotesPinned(context);

    }

    public static Context getContext() {
        return context;
    }

    public static void setKey(String s) {
        key = (AESUtils.decode(s) + AESUtils.keyBytes).substring(0, 24);
    }
    public static String cerateKey(String s) {
       return (s + AESUtils.keyBytes).substring(0,24);
    }
}
