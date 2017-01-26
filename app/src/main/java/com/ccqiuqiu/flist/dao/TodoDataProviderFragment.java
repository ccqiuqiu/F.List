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

package com.ccqiuqiu.flist.dao;


import android.annotation.SuppressLint;
import android.os.Bundle;

import com.ccqiuqiu.flist.MainActivity;
import com.ccqiuqiu.flist.model.Todo;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.LinkedList;
import java.util.List;

@SuppressLint("ValidFragment")
public class TodoDataProviderFragment extends BaseDataProviderFragment {
    private BaseDataProvider mDataProvider;
    private int mCategoryId;
    private int mStatus;
    final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

    public TodoDataProviderFragment(int categoryId, int status) {
        this.mCategoryId = categoryId;
        this.mStatus = status;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);  // keep the mDataProvider instance
        mDataProvider = new TodoDataProvider();
    }

    public BaseDataProvider getDataProvider() {
        return mDataProvider;
    }

    public class TodoDataProvider extends BaseDataProvider {

        public TodoDataProvider() {
            loadData(mCategoryId, mStatus);
        }

        public void reload() {
            loadData(mCategoryId, mStatus);
        }

        public void reload(int categoryId, int status) {
            loadData(categoryId, status);
        }

        private void loadData(int categoryId, int status) {
            priority_1 = 0;
            priority_2 = 0;
            mData = new LinkedList<>();
            DbManager db = DbUtils.getDbManager();
            try {
                List<Todo> todos;
                if (status == 0) {
                    todos = db.selector(Todo.class).where(WhereBuilder.b("categoryId", "=", categoryId)
                            .and("status", "=", status)).orderBy("priority", true).orderBy("addTime", true).findAll();
                } else {
                    todos = db.selector(Todo.class).where(WhereBuilder.b("categoryId", "=", categoryId)
                            .and("status", "=", status)).orderBy("priority", true).orderBy("doneTime", true).findAll();
                }
                if (todos != null) {
                    for (Todo todo : todos) {
                        mData.add(new Data(todo, todo.getId(), 0, todo.getTitle(), swipeReaction, false));
                        if (todo.getStatus() == 0 && todo.getPriority() == 1) priority_1++;
                        if (todo.getStatus() == 0 && todo.getPriority() == 2) priority_2++;
                    }
                }

            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        public int getPosition(Todo todo) {
            List<BaseDataProvider.Data> datas = mDataProvider.mData;
            for (int i = 0; i < datas.size(); i++) {
                Todo t = (Todo) datas.get(i).getmEntity();
                if (t.getId() == todo.getId()) {
                    return i;
                }
            }
            return -1;
        }
    }

    public int addItem(Todo todo) {
        int position = 0;
        switch (todo.getPriority()) {
            case 0:
                position = mDataProvider.priority_1 + mDataProvider.priority_2;
                break;
            case 1:
                position = mDataProvider.priority_2;
                mDataProvider.priority_1++;
                break;
            case 2:
                mDataProvider.priority_2++;
                break;
        }
        mDataProvider.addItem(position, new BaseDataProvider.Data(todo, todo.getId(), 0, todo.getTitle(), swipeReaction, false));
        return position;
    }

}
