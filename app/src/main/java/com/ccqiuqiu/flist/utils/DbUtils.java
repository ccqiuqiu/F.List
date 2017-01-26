package com.ccqiuqiu.flist.utils;

import android.text.TextUtils;

import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.model.PassNote;
import com.ccqiuqiu.flist.model.Setting;

import org.w3c.dom.Text;
import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.List;

/**
 * Created by cc on 2015/11/24.
 */
public class DbUtils {

    public static DbManager.DaoConfig getDaoConfig() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("flist.db")
                //.setDbDir()
                .setDbVersion(2)
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        if (oldVersion == 1) {
                            try {
                                List<PassNote> passNotes = db.selector(PassNote.class).findAll();
                                if (passNotes != null) {
                                    Setting mSetting = db.selector(Setting.class).findFirst();
                                    if(mSetting == null || TextUtils.isEmpty(mSetting.getPassword()))return;
                                    for (PassNote passNote : passNotes) {
                                        App.setKey(mSetting.getPassword());
                                        passNote.setTitle(AESUtils.decode(passNote.getTitle(), App.key));
                                        db.update(passNote);
                                    }
                                }
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        return daoConfig;
    }

    public static DbManager getDbManager() {
        DbManager db = x.getDb(DbUtils.getDaoConfig());
        return db;
    }

}
