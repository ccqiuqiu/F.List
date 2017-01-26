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

import com.ccqiuqiu.flist.utils.DbUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.List;

public class BaseDataProvider {

    public List<Data> mData;
    public Data mLastRemovedData;
    public int mLastRemovedPosition = -1;

    public int priority_1;
    public int priority_2;


    public static class Data {
        private Object mEntity;
        private final long mId;
        private final String mText;
        private final int mViewType;
        private boolean mPinned;

        public Data(Object entity,long id, int viewType, String text, int swipeReaction,Boolean isPinned) {
            mEntity = entity;
            mId = id;
            mViewType = viewType;
            mText = text;
            mPinned = isPinned;
        }


        public boolean isSectionHeader() {
            return false;
        }

        public int getViewType() {
            return mViewType;
        }

        public long getId() {
            return mId;
        }

        public String toString() {
            return mText;
        }

        public String getText() {
            return mText;
        }

        public boolean isPinned() {
            return mPinned;
        }

        public void setPinned(boolean pinned) {
            mPinned = pinned;
        }

        public Object getmEntity() {
            return mEntity;
        }

        public void setmEntity(Object mEntity) {
            this.mEntity = mEntity;
        }
    }

    public int getCount() {
        return mData.size();
    }

    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return mData.get(index);
    }

    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedData);
            //
            DbManager db = DbUtils.getDbManager();
            try {
                db.saveBindingId(mLastRemovedData.getmEntity());
            } catch (DbException e) {
                e.printStackTrace();
            }

            mLastRemovedData = null;
            mLastRemovedPosition = -1;
            return insertedPosition;
        } else {
            return -1;
        }
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final Data item = mData.remove(fromPosition);

        mData.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    public void removeItem(int position) {
        //noinspection UnnecessaryLocalVariable
        final Data removedItem = mData.remove(position);

        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }
    public void addItem(Data data){
        mData.add(0, data);
    }
    public void addItem(int position, Data data){
        if(mData.size() == 0){
            mData.add(data);
        }else{
            mData.add(position,data);
        }
    }
//    public void reload(List<Data> data){
//        mData = data;
//    }
}
