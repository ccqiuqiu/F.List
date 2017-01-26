/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ccqiuqiu.flist.todo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.MainActivity;
import com.ccqiuqiu.flist.R;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends FragmentPagerAdapter{

    private List<RecyclerListViewPageFragment> recyclerListViewPageFragments = new ArrayList();

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    //给viewpager初始化2个页面，Fragment
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                RecyclerListViewPageFragment fragment1 = RecyclerListViewPageFragment.newInstance(
                        MainActivity.FRAGMENT_TAG_TODO_DATA1, false);
                recyclerListViewPageFragments.add(fragment1);
                return fragment1;
            case 1:
                RecyclerListViewPageFragment fragment2 = RecyclerListViewPageFragment.newInstance(
                        MainActivity.FRAGMENT_TAG_TODO_DATA2, true);
                recyclerListViewPageFragments.add(fragment2);
                return fragment2;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = App.getContext().getResources().getString(R.string.todo_un_done);
                break;
            case 1:
                title = App.getContext().getResources().getString(R.string.todo_done);
        }

        return title;
    }

    public List<RecyclerListViewPageFragment> getRecyclerListViewPageFragments() {
        return recyclerListViewPageFragments;
    }
}
