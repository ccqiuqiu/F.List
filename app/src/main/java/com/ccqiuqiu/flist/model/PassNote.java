package com.ccqiuqiu.flist.model;

import android.text.TextUtils;

import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.utils.AESUtils;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by cc on 2016/3/4.
 */
@Table(name = "passnote")
public class PassNote {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "title")
    private String title;
    @Column(name = "userName")
    private String userName;
    @Column(name = "passWord")
    private String passWord;
    @Column(name = "desc")
    private String desc;

    @Column(name = "addTime")
    private long addTime;
    @Column(name = "isPinned")
    private boolean isPinned;
    @Column(name = "order")
    private int order;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }


    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setIsPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        //String s = App.getContext().getString(R.string.title) + ":" + AESUtils.decode(title, App.key) + "\n"
        String s = App.getContext().getString(R.string.title) + ":" + title + "\n"
                + App.getContext().getString(R.string.username) + ":" + AESUtils.decode(userName, App.key) + "\n"
                + App.getContext().getString(R.string.password) + ":" + AESUtils.decode(passWord, App.key) + "\n";
        if(!TextUtils.isEmpty(desc)){
            s += App.getContext().getString(R.string.desc) + ":" + AESUtils.decode(desc, App.key);
        }
        return s;
    }

    public void upDate(String newKey) {
        //String title = AESUtils.decode(getTitle(), App.key);
        //title = AESUtils.encode(title, newKey);
        //setTitle(title);
        setUserName(AESUtils.encode(AESUtils.decode(getUserName(), App.key), newKey));
        setPassWord(AESUtils.encode(AESUtils.decode(getPassWord(), App.key), newKey));
        if(!TextUtils.isEmpty(getDesc())){
            setDesc(AESUtils.encode(AESUtils.decode(getDesc(), App.key), newKey));
        }
    }
}
