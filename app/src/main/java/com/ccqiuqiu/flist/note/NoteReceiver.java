package com.ccqiuqiu.flist.note;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ccqiuqiu.flist.MainActivity;

public class NoteReceiver extends BroadcastReceiver {

    private MainActivity mMainActivity;

    public NoteReceiver(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        int cateGoryId = intent.getIntExtra("categoryId",0);

        mMainActivity.reNote(cateGoryId);
    }
}
