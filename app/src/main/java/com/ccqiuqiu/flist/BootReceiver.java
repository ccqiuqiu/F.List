package com.ccqiuqiu.flist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ccqiuqiu.flist.dao.NoteUtil;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NoteUtil.NotesPinned(context);
    }
}
