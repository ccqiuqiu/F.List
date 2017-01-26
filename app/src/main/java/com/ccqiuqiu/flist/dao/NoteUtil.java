package com.ccqiuqiu.flist.dao;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.model.Note;
import com.ccqiuqiu.flist.note.NoteShowActivity;
import com.ccqiuqiu.flist.utils.DbUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.List;

/**
 * Created by cc on 2015/12/7.
 */
public class NoteUtil {

    public static void NotePinned(Context context,Note note,boolean mute){
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(context.NOTIFICATION_SERVICE);
        String title = note.getTitle();
        String content = note.getDesc();
        int id = note.getId();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Intent intent = new Intent(context, NoteShowActivity.class);
        intent.putExtra("id", id);
        PendingIntent pendingIntent= PendingIntent.getActivity(context, id,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_note_w))
                .setSmallIcon(R.drawable.ic_menu_note_w);//设置通知小ICON

        if(!mute) mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(id,notification);
    }
    public static void NotesPinned(Context context){
        DbManager db = DbUtils.getDbManager();
        try {
            List<Note> notes = db.selector(Note.class).where("isPinned","=","1").findAll();
            if(notes == null)return;
            for (Note note : notes){
                NoteUtil.NotePinned(context, note,true);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
