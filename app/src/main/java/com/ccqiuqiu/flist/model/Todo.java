package com.ccqiuqiu.flist.model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by cc on 2015/11/24.
 */
@Table(name = "todo")
public class Todo {

    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "priority")
    private int priority;

    @Column(name = "addTime")
    private long addTime;

    @Column(name = "doneTime")
    private long doneTime;

    @Column(name = "categoryId")
    private int categoryId;

    @Column(name = "status")
    private int status;  //0-未完成    1-已完成

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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(long doneTime) {
        this.doneTime = doneTime;
    }
}
