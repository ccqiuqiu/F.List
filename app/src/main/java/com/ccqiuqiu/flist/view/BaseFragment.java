package com.ccqiuqiu.flist.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.ccqiuqiu.flist.MainActivity;

/**
 * Created by cc on 2016/4/21.
 */
public class BaseFragment extends Fragment {

    public MainActivity mMainActivity;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
    }

    public void onSearchExit() {
    }

    public void onSearchTermChanged(String string) {
        onSearch(string);
    }

    public void onSearch(String string) {
    }

    public void onSearchCleared() {
    }
}
