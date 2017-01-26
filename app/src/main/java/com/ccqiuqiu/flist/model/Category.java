package com.ccqiuqiu.flist.model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by cc on 2015/11/24.
 */
@Table(name = "category")
public class Category {

    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "allowDel")
    private Boolean allowDel;
    @Column(name = "flg")
    private int flg; //0-todo   1-note

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAllowDel() {
        return allowDel;
    }

    public void setAllowDel(Boolean allowDel) {
        this.allowDel = allowDel;
    }

    public int getFlg() {
        return flg;
    }

    public void setFlg(int flg) {
        this.flg = flg;
    }
}
