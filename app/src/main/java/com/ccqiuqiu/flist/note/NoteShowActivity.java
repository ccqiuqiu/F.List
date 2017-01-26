package com.ccqiuqiu.flist.note;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.model.Note;
import com.ccqiuqiu.flist.utils.DbUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

public class NoteShowActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_show);

        final NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        try {
            Intent intent = getIntent();
            final int id = intent.getIntExtra("id", 0);
            final DbManager db = DbUtils.getDbManager();
            final Note note = db.selector(Note.class).where("id", "=", id).findFirst();
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(note.getTitle())
                .content(note.getDesc())
                .positiveText(R.string.unpinned)
                .neutralText(R.string.delete)
                .titleColorRes(android.R.color.white)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .btnSelector(R.drawable.btn_selector_accent, DialogAction.POSITIVE)
                .positiveColor(Color.WHITE)
                .negativeColor(Color.GRAY)
                .theme(Theme.DARK)
                .theme(Theme.LIGHT)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        finish();
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        note.setIsPinned(false);
                        try {

                            Intent bi = new Intent("com.cccqiuqiu.flist.note");
                            bi.putExtra("id",id);
                            bi.putExtra("categoryId", note.getCategoryId());
                            sendBroadcast(bi);

                            db.update(note);
                            mNotificationManager.cancel(id);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        try {
                            Intent bi = new Intent("com.cccqiuqiu.flist.note");
                            bi.putExtra("id",id);
                            bi.putExtra("categoryId", note.getCategoryId());
                            sendBroadcast(bi);

                            db.delete(note);
                            mNotificationManager.cancel(id);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
            dialog.getWindow().setDimAmount(0);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

}
