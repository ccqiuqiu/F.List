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

import com.ccqiuqiu.flist.model.Note;
import com.ccqiuqiu.flist.model.PassNote;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.LinkedList;
import java.util.List;

@SuppressLint("ValidFragment")
public class PassNoteDataProviderFragment extends BaseDataProviderFragment {
    private BaseDataProvider mDataProvider;
    final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;


    public PassNoteDataProviderFragment() {
        mDataProvider = new PassNoteDataProvider();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);  // keep the mDataProvider instance
    }

    public BaseDataProvider getDataProvider() {
        return mDataProvider;
    }

    public class PassNoteDataProvider extends BaseDataProvider {


        public void reload() {
            loadData(null);
        }

        public void reload(String searchKey) {
            loadData(searchKey);
        }

        public PassNoteDataProvider() {
            loadData(null);
        }

        private void loadData(String searchKey) {
            mData = new LinkedList<>();
            DbManager db = DbUtils.getDbManager();
            try {
                List<PassNote> notes;
                if (searchKey == null) {
                    notes = db.selector(PassNote.class).orderBy("order", true).findAll();
                } else {
                    notes = db.selector(PassNote.class).where("title", "like", "%" + searchKey + "%")
                            .orderBy("order", true).findAll();
                }
                if (notes != null) {
                    for (PassNote note : notes) {
                        mData.add(new Data(note, note.getId(), 0, note.getTitle(), swipeReaction, note.isPinned()));
                    }
                }

            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void moveItem(int fromPosition, int toPosition) {
            super.moveItem(fromPosition, toPosition);

            try {
                final PassNote note = (PassNote) mData.get(toPosition).getmEntity();
                note.setOrder(note.getOrder() + fromPosition - toPosition);
                DbManager db = DbUtils.getDbManager();
                db.update(note);
                //更新排序
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        PassNote notetrmp = (PassNote) mData.get(i).getmEntity();
                        notetrmp.setOrder(notetrmp.getOrder() + 1);
                        db.update(notetrmp);
                    }
                } else {
                    for (int i = toPosition + 1; i <= fromPosition; i++) {
                        PassNote notetrmp = (PassNote) mData.get(i).getmEntity();
                        notetrmp.setOrder(notetrmp.getOrder() - 1);
                        db.update(notetrmp);
                    }
                }
            } catch (DbException e) {
                e.printStackTrace();
            }

        }
    }

    public void addItem(PassNote note) {
        mDataProvider.addItem(new BaseDataProvider.Data(note, note.getId(), 0, note.getTitle(), swipeReaction, note.isPinned()));
    }
}
